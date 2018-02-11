package com.uladkaminski

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.internal.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.internal.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.apache.log4j.BasicConfigurator
import org.slf4j.LoggerFactory
import spark.Spark.*
import java.time.Instant
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
    }
}