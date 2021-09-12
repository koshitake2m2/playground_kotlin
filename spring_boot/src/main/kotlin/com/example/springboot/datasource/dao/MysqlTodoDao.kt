package com.example.springboot.datasource.dao

import com.example.springboot.datasource.dto.NewTodoDto
import com.example.springboot.datasource.dto.TodoDto
import com.example.springboot.datasource.dto.UpdatedTodoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Component

@Component
class MysqlTodoDao @Autowired constructor(private val jdbcTemplate: JdbcTemplate) {
  private val titledParameterJdbcTemplate = NamedParameterJdbcTemplate(jdbcTemplate)
  private val simpleJdbcInsert = SimpleJdbcInsert(jdbcTemplate)
    .withTableName("todo")
    .usingGeneratedKeyColumns("todo_id")
    .usingColumns("todo_title", "todo_status")

  fun insert(dto: NewTodoDto): Int {
    val params = MapSqlParameterSource("todo_title", dto.todoTitle).addValue(
      "todo_status",
      dto.todoStatus
    )
    val generatedKey = simpleJdbcInsert.executeAndReturnKey(params)
    return generatedKey.toInt()
  }

  fun selectAll(): List<TodoDto> {
    val sql = """
      select * from todo
    """.trimIndent()
    return jdbcTemplate.query(sql) { rs, _ ->
      TodoDto(
        todoId = rs.getInt("todo_id"),
        todoTitle = rs.getString("todo_title"),
        todoStatus = rs.getString("todo_status")
      )
    }
  }

  fun selectBy(todoId: Int): TodoDto? {
    val sql = """
      select * from todo
      where todo_id = :todoId
    """.trimIndent()
    val params = MapSqlParameterSource("todoId", todoId)
    return try {
      titledParameterJdbcTemplate.queryForObject(sql, params) { rs, _ ->
        TodoDto(
          rs.getInt("todo_id"),
          rs.getString("todo_title"),
          rs.getString("todo_status"),
        )
      }
    } catch (e: EmptyResultDataAccessException) {
      null
    }
  }

  fun update(dto: UpdatedTodoDto) {
    val sql = """
      update todo
      set todo_title = :todoTitle,
          todo_status = :todoStatus
      where todo_id = :todoId
    """.trimIndent()
    val params =
      MapSqlParameterSource("todoId", dto.todoId).addValue("todoTitle", dto.todoTitle)
        .addValue("todoStatus", dto.todoStatus)
    titledParameterJdbcTemplate.update(sql, params)
  }

  fun deleteBy(todoId: Int) {
    val sql = """
      delete from todo
      where todo_id = :todoId
    """.trimIndent()
    val params = MapSqlParameterSource("todoId", todoId)
    titledParameterJdbcTemplate.update(sql, params)
  }

}
