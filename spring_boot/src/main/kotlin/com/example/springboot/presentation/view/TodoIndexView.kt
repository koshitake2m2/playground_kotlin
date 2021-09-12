package com.example.springboot.presentation.view

import com.example.springboot.domain.`object`.todo.Todo

data class TodoIndexView(
  val todos: List<TodoShowView>
) {
  companion object {
    fun of(todos: List<Todo>): TodoIndexView {
      return TodoIndexView(todos.map { TodoShowView.of(it) })
    }
  }
}
