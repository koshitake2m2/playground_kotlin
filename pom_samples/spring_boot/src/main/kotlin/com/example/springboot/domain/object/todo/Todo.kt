package com.example.springboot.domain.`object`.todo

data class Todo(
  val id: TodoId,
  val title: TodoTitle,
  val status: TodoStatus
)
