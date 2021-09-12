package com.example.springboot.presentation.handler

import com.example.springboot.presentation.utils.Semigroup

data class ValidationErrorMessages(
  val messages: List<String>
) : Semigroup<ValidationErrorMessages> {
  override fun combine(that: ValidationErrorMessages) =
    ValidationErrorMessages(messages + that.messages)

  companion object {
    fun of(message: String) = ValidationErrorMessages(listOf(message))
  }
}
