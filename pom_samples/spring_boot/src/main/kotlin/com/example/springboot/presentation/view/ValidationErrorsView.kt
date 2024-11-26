package com.example.springboot.presentation.view

import com.example.springboot.presentation.handler.ValidationErrorMessages

data class ValidationErrorsView(
  val messages: List<String>
) {
  companion object {
    fun of(errorMessages: ValidationErrorMessages): ValidationErrorsView {
      return ValidationErrorsView(errorMessages.messages)
    }
  }
}
