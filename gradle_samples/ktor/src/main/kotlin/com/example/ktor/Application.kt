package com.example.ktor

import com.example.ktor.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 10000, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes", "resources"))
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureRouting()
}