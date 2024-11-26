package com.example.ktor


import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class UserName(val value: String)

data class UserUpsertRequest(
    val id: String?,
    val name: UserName,
)

data class UserResponse(
    val id: String,
    val name: UserName,
)

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

        /**
         * curl -X GET -H "Content-Type: application/json" "http://localhost:10000/users/123" -i
         */
        get("/users/{userId}") {
            val userId = call.parameters["userId"]
            if (userId == null)  {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val userName = UserName("Bob")
            val user = UserResponse(id = userId, name = userName)
            call.respond(listOf(user))
        }

        /**
         * curl -X POST -H "Content-Type: application/json" -d '{"id": "null", "name":"Bob"}' "http://localhost:10000/users" -i
         */
        post("/users") {
            val req = call.receive<UserUpsertRequest>()
            println(req.toString())
            call.respond(req)
        }

        get("/error") {
            throw RuntimeException("Too Busy")
        }
    }
}