package com.example.springboot.presentation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TodoController {
    @GetMapping("todos")
    fun index(): ResponseEntity<TodoIndexView> {
        val showView1 = TodoShowView(1, "hello")
        val indexView = TodoIndexView(listOf(showView1))
        return ResponseEntity.ok(indexView)
    }

    companion object {
        data class TodoShowView(
                val id: Int,
                val title: String
        )

        data class TodoIndexView(
                val todos: List<TodoShowView>
        )
    }
}
