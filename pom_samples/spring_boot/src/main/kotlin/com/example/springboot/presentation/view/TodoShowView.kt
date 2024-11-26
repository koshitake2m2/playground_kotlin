package com.example.springboot.presentation.view

import com.example.springboot.domain.`object`.todo.Todo

data class TodoShowView(
  val id: Int,
  val title: String,
  val status: String
) {
  companion object {
    fun of(todo: Todo): TodoShowView {
      return TodoShowView(
        todo.id.value,
        todo.title.value,
        todo.status.value
      )
    }
  }
}
