package org.example.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.server.plugins.statuspages.*

fun Application.configureRouting() {
    install(StatusPages) {
        // catch errors
        exception<RuntimeException> { call, cause ->
            call.respondText("RuntimeException as ${cause.message}")
        }
    }
    routing {
        staticResources("/content", "mycontent")

        get("/") {
            println("Hello World in console!")
            call.respondText("Hello World!")
        }

        get("/html") {
            val text = """
                |<h1>Hello World</h1>
                |<p>I like apples</p>
            """.trimMargin()
            val type = ContentType.parse("text/html")
            call.respondText(text, type)
        }

        get("/error") {
            throw RuntimeException("Too Busy")
        }
    }
}