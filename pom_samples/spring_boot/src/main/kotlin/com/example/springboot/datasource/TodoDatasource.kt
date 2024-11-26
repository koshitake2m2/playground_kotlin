package com.example.springboot.datasource

import com.example.springboot.datasource.dao.MysqlTodoDao
import com.example.springboot.datasource.dto.NewTodoDto
import com.example.springboot.datasource.dto.UpdatedTodoDto
import com.example.springboot.domain.`object`.todo.*
import com.example.springboot.domain.repository.TodoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class TodoDatasource @Autowired constructor(
  private val todoDao: MysqlTodoDao
) : TodoRepository {

  override fun save(newTodo: NewTodo): TodoId {
    val dto = NewTodoDto(
      todoTitle = newTodo.title.value,
      todoStatus = newTodo.status.value
    )
    val generatedKey = todoDao.insert(dto)
    return TodoId(generatedKey)
  }

  override fun findAll(): List<Todo> {
    val dtos = todoDao.selectAll()
    return dtos.map { dto ->
      Todo(
        TodoId(dto.todoId),
        TodoTitle(dto.todoTitle),
        TodoStatus.of(dto.todoStatus)
      )
    }
  }

  override fun findBy(id: TodoId): Todo? {
    val maybeDto = todoDao.selectBy(id.value)
    return maybeDto?.let {
      Todo(
        TodoId(it.todoId),
        TodoTitle(it.todoTitle),
        TodoStatus.of(it.todoStatus)
      )
    }
  }

  override fun update(updatedTodo: UpdatedTodo) {
    val dto = UpdatedTodoDto(
      todoId = updatedTodo.id.value,
      todoTitle = updatedTodo.title.value,
      todoStatus = updatedTodo.status.value
    )
    todoDao.update(dto)
  }

  override fun deleteBy(id: TodoId) {
    todoDao.deleteBy(id.value)
  }
}
