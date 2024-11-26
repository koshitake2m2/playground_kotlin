package com.example.springboot.presentation.handler

import com.example.springboot.presentation.view.ValidationErrorsView
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class AppExceptionHandler {

  @ExceptionHandler(value = [ValidationException::class])
  fun handleValidationException(e: ValidationException): ResponseEntity<ValidationErrorsView> {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ValidationErrorsView.of(e.validationErrorMessages))
  }
}
