package com.example.springboot.presentation

import com.example.springboot.application.TodoService
import com.example.springboot.domain.`object`.todo.TodoId
import com.example.springboot.presentation.form.TodoCreateForm
import com.example.springboot.presentation.form.TodoUpdatedForm
import com.example.springboot.presentation.handler.ValidationErrorMessages
import com.example.springboot.presentation.handler.ValidationException
import com.example.springboot.presentation.utils.Validated
import com.example.springboot.presentation.view.TodoIndexView
import com.example.springboot.presentation.view.TodoShowView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/")
class TodoController @Autowired constructor(
  private val todoService: TodoService
) {

  /**
   * curl -X POST 'http://localhost:8080/api/todos' -H 'Content-Type: application/json' -d '{"title": "hello", "status": "waiting"}'
   */
  @PostMapping("todos")
  fun create(@RequestBody body: TodoCreateForm): ResponseEntity<Int> {
    val newTodoOrError = body.toNewTodoWithValidation()

    when (newTodoOrError) {
      is Validated.InValid -> {
        throw ValidationException(newTodoOrError.e)
      }
      is Validated.Valid -> {
        val id = todoService.save(newTodoOrError.a)
        return ResponseEntity.ok(id.value)
      }
    }
  }

  /**
   * curl -X GET 'http://localhost:8080/api/todos'
   */
  @GetMapping("todos")
  fun index(): ResponseEntity<TodoIndexView> {
    return ResponseEntity.ok(TodoIndexView.of(todoService.findAll()))
  }

  /**
   * curl -X GET 'http://localhost:8080/api/todos/1'
   */
  @GetMapping("todos/{id}")
  fun show(@PathVariable id: Int): ResponseEntity<TodoShowView> {
    val maybeTodo = todoService.findBy(TodoId(id))
      ?: throw ValidationException(ValidationErrorMessages.of("Not Found. id = $id"))
    return ResponseEntity.ok(TodoShowView.of(maybeTodo))
  }

  /**
   * curl -X PUT 'http://localhost:8080/api/todos/1' -H 'Content-Type: application/json' -d '{"title": "hello", "status": "waiting"}'
   */
  @PutMapping("todos/{id}")
  fun update(@PathVariable id: Int, @RequestBody body: TodoUpdatedForm): ResponseEntity<Unit> {
    val updatedTodoOrError = body.toUpdatedTodoWithValidation(id)

    when (updatedTodoOrError) {
      is Validated.InValid -> {
        throw ValidationException(updatedTodoOrError.e)
      }
      is Validated.Valid -> {
        todoService.update(updatedTodoOrError.a)
        return ResponseEntity.ok(Unit)
      }
    }
  }

  /**
   * curl -X DELETE 'http://localhost:8080/api/todos/4'
   */
  @DeleteMapping("todos/{id}")
  fun delete(@PathVariable id: Int): ResponseEntity<Unit> {
    todoService.deleteBy(TodoId(id))
    return ResponseEntity.ok(Unit)
  }

}
