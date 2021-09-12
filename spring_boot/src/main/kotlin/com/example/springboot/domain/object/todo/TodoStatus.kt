package com.example.springboot.domain.`object`.todo

enum class TodoStatus(val value: String) {
  Waiting("waiting"),
  Doing("doing"),
  Done("done");

  companion object {
    fun of(value: String): TodoStatus {
      return values().first { value == it.value }
    }
  }
}
