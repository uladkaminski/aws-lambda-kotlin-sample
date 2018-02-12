package com.uladkaminski

import com.mongodb.MongoClientURI
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

object Database {
    private lateinit var database: MongoDatabase

    init {
        makeConnection()
    }

    fun makeConnection() {
        try {
            val connectionString = System.getenv(CONNECTION)
            val clientUri = MongoClientURI(connectionString)
            val client = KMongo.createClient(clientUri)
            database = client.getDatabase(DEFAULT_DB)
        } catch (ex: Exception) {
            println("Exception: ${ex.message}")
        }
    }

    fun getVehicles() = database.getCollection<Vehicle>("vehicles")
}

