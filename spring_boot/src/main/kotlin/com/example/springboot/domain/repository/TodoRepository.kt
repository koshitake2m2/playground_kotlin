package com.example.springboot.domain.repository

import com.example.springboot.domain.`object`.todo.NewTodo
import com.example.springboot.domain.`object`.todo.Todo
import com.example.springboot.domain.`object`.todo.TodoId
import com.example.springboot.domain.`object`.todo.UpdatedTodo

interface TodoRepository {
  fun save(newTodo: NewTodo): TodoId
  fun findAll(): List<Todo>
  fun findBy(id: TodoId): Todo?
  fun update(updatedTodo: UpdatedTodo): Unit
  fun deleteBy(id: TodoId): Unit
}
