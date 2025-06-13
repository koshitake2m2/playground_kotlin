package com.example.ktor


import com.example.ktor.database.dao.AuthorDao
import com.example.ktor.database.dao.PostDao
import com.example.ktor.models.NewPost
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
    val postDao = PostDao()
    val authorDao = AuthorDao()
    
    install(StatusPages) {
        // catch errors
        exception<RuntimeException> { call, cause ->
            println("RuntimeException as ${cause.message}")
            call.respondText("RuntimeException as ${cause.message}")
        }
        // NOTE: Throwable is parent of all exceptions. If subclasses are registered to [StatusPagesConfig.exception], they are matched first. If not, registered superclass is matched.
        // e.g. RuntimeException : Exception : Throwable
        exception<Throwable> { call, cause ->
            println("Throwable as ${cause.message}")
            call.respondText("Throwable as ${cause.message}")
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
            if (userId == null) {
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
        
        // Author endpoints
        /**
         * curl -X GET -H "Content-Type: application/json" "http://localhost:10000/authors" -i
         */
        get("/authors") {
            val authors = authorDao.allAuthors()
            call.respond(authors)
        }
        
        /**
         * curl -X POST -H "Content-Type: application/json" -d '{"name": "John Doe", "email": "john@example.com"}' "http://localhost:10000/authors" -i
         */
        post("/authors") {
            val newAuthor = call.receive<com.example.ktor.models.NewAuthor>()
            val author = authorDao.addAuthor(newAuthor)
            if (author == null) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create author")
                return@post
            }
            call.respond(HttpStatusCode.Created, author)
        }
        
        // Post endpoints
        /**
         * curl -X GET -H "Content-Type: application/json" "http://localhost:10000/posts" -i
         */
        get("/posts") {
            val posts = postDao.allPostsWithAuthors()
            call.respond(posts)
        }
        
        /**
         * curl -X GET -H "Content-Type: application/json" "http://localhost:10000/posts/1" -i
         */
        get("/posts/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid post ID")
                return@get
            }
            
            val post = postDao.postWithAuthor(id)
            if (post == null) {
                call.respond(HttpStatusCode.NotFound, "Post not found")
                return@get
            }
            
            call.respond(post)
        }
        
        /**
         * curl -X POST -H "Content-Type: application/json" -d '{"title": "My First Post", "content": "This is the content", "authorId": 1}' "http://localhost:10000/posts" -i
         */
        post("/posts") {
            val newPost = call.receive<NewPost>()
            
            // Verify author exists
            val author = authorDao.author(newPost.authorId)
            if (author == null) {
                call.respond(HttpStatusCode.BadRequest, "Author with ID ${newPost.authorId} not found")
                return@post
            }
            
            val post = postDao.addPost(newPost)
            if (post == null) {
                call.respond(HttpStatusCode.InternalServerError, "Failed to create post")
                return@post
            }
            
            call.respond(HttpStatusCode.Created, post)
        }
    }
}