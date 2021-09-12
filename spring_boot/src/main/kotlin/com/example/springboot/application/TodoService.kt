package com.example.springboot.application

import com.example.springboot.domain.`object`.todo.NewTodo
import com.example.springboot.domain.`object`.todo.Todo
import com.example.springboot.domain.`object`.todo.TodoId
import com.example.springboot.domain.`object`.todo.UpdatedTodo
import com.example.springboot.domain.repository.TodoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TodoService @Autowired constructor(
  private val repository: TodoRepository
) {

  @Transactional
  fun save(newTodo: NewTodo): TodoId {
    return repository.save(newTodo)
  }

  fun findAll(): List<Todo> {
    return repository.findAll()
  }

  fun findBy(id: TodoId): Todo? {
    return repository.findBy(id)
  }

  @Transactional
  fun update(updatedTodo: UpdatedTodo) {
    return repository.update(updatedTodo)
  }

  @Transactional
  fun deleteBy(id: TodoId) {
    return repository.deleteBy(id)
  }
}
