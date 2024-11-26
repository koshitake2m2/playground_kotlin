package com.example.springboot.domain.`object`.todo

data class UpdatedTodo(
  val id: TodoId,
  val title: TodoTitle,
  val status: TodoStatus
)
