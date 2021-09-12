package com.example.springboot.presentation.form

import com.example.springboot.domain.`object`.todo.TodoId
import com.example.springboot.domain.`object`.todo.TodoStatus
import com.example.springboot.domain.`object`.todo.TodoTitle
import com.example.springboot.domain.`object`.todo.UpdatedTodo
import com.example.springboot.presentation.handler.ValidationErrorMessages
import com.example.springboot.presentation.utils.Validated

data class TodoUpdatedForm(
  val title: String,
  val status: String
) {
  fun toUpdatedTodoWithValidation(id: Int): Validated<ValidationErrorMessages, UpdatedTodo> {
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
      UpdatedTodo(TodoId(id), t, s)
    }
  }
}
