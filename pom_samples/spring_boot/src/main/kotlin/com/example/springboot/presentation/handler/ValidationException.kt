package com.example.springboot.presentation.handler

data class ValidationException(
  val validationErrorMessages: ValidationErrorMessages
) : Throwable()
