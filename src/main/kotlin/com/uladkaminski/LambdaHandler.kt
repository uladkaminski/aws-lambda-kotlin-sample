package com.uladkaminski

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.BasicConfigurator
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.json
import org.litote.kmongo.sort
import org.litote.kmongo.util.KMongoUtil
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Spark.get
import spark.Spark.initExceptionHandler
import java.time.LocalDate
import java.time.LocalTime

class LambdaHandler @Throws(ContainerInitializationException::class)
constructor() : RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    private val handler = SparkLambdaContainerHandler.getAwsProxyHandler()
    private var initialized = false
    private val log = LoggerFactory.getLogger(LambdaHandler::class.java)

    override fun handleRequest(awsProxyRequest: AwsProxyRequest, context: Context): AwsProxyResponse {
        if (!initialized) {
            defineRoutes()
            initialized = true
        }
        return handler.proxy(awsProxyRequest, context)
    }

    private fun defineRoutes() {
        BasicConfigurator.configure()
        initExceptionHandler { e ->
            log.error("Spark init a failure", e)
            System.exit(100)
        }
        get("/time") { _, _ -> "Current time is ${LocalTime.now()}" }
        get("/date") { _, _ -> "Current date is ${LocalDate.now()}" }
        get("/cars") { request, response -> getAllVehicles(request, response) }
        get("/cars/:id") { request, response -> getSpecificVehicle(request, response) }
    }

    private fun getSpecificVehicle(request: Request, response: Response): String {
        val id = request.params("id") ?: ""
        val parseId: Int? = id.toIntOrNull()
        if (parseId != null) {
            val collection = Database.getVehicles()
            val vehicle = collection.findOne("{id: $parseId}")
            if (vehicle != null) {
                return vehicle.json
            }
        }
        return "Unable to find vehicle $parseId"
    }

    private fun getAllVehicles(request: Request, response: Response): String {
        val collection = Database.getVehicles()
        val vehicles = collection.find(KMongoUtil.EMPTY_JSON)
                .sort("{year: -1, make: 1, model: 1}")
                .take(PAGE_COUNT)
        if (vehicles.count() > 0) {
            return vehicles.json
        }

        return "Unable to load all vehicles"
    }
}