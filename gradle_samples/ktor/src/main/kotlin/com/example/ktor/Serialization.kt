package com.example.ktor


import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
//        kotlinx.serialization
//        json()

        jackson {
            registerModule(UserJsonModule())
        }
    }
}
