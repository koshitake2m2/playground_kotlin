package com.example.springboot.presentation.form

import com.example.springboot.domain.`object`.todo.NewTodo
import com.example.springboot.domain.`object`.todo.TodoStatus
import com.example.springboot.domain.`object`.todo.TodoTitle
import com.example.springboot.presentation.handler.ValidationErrorMessages
import com.example.springboot.presentation.utils.Validated

data class TodoCreateForm(
  val title: String,
  val status: String
) {
  fun toNewTodoWithValidation(): Validated<ValidationErrorMessages, NewTodo> {
    val titleOrError: Validated<ValidationErrorMessages, TodoTitle> = if (255 < title.length) {
      Validated.InValid(ValidationErrorMessages.of("title の長さは20以下にしてください."))
    } else Validated.Valid(TodoTitle(title))

    val statusOrError: Validated<ValidationErrorMessages, TodoStatus> = kotlin.runCatching {
      val status = TodoStatus.of(status)
      Validated.Valid<ValidationErrorMessages, TodoStatus>(status)
    }.getOrElse {
      Validated.InValid(ValidationErrorMessages.of("status の値が不正です."))
    }

    return Validated.map2(titleOrError, statusOrError) { t: TodoTitle, s: TodoStatus ->
      NewTodo(t, s)
    }
  }
}
