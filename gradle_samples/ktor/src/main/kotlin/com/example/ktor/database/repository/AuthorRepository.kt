package com.example.ktor.database.repository

import com.example.ktor.database.AuthorRecords
import com.example.ktor.database.DatabaseFactory.dbQuery
import com.example.ktor.models.Author
import com.example.ktor.models.NewAuthor
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

// DSLスタイルのリポジトリ例
object AuthorRepository {
    
    suspend fun findAll(): List<Author> = dbQuery {
        AuthorRecords.selectAll().map { it.toAuthor() }
    }
    
    suspend fun findById(id: Int): Author? = dbQuery {
        AuthorRecords
            .select { AuthorRecords.id eq id }
            .singleOrNull()
            ?.toAuthor()
    }
    
    suspend fun create(author: NewAuthor): Author? = dbQuery {
        val id = AuthorRecords.insertAndGetId {
            it[name] = author.name
            it[email] = author.email
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        findById(id.value)
    }
    
    suspend fun update(id: Int, author: NewAuthor): Boolean = dbQuery {
        AuthorRecords.update({ AuthorRecords.id eq id }) {
            it[name] = author.name
            it[email] = author.email
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }
    
    suspend fun delete(id: Int): Boolean = dbQuery {
        AuthorRecords.deleteWhere { AuthorRecords.id eq id } > 0
    }
    
    private fun ResultRow.toAuthor() = Author(
        id = this[AuthorRecords.id].value,
        name = this[AuthorRecords.name],
        email = this[AuthorRecords.email],
        createdAt = this[AuthorRecords.createdAt],
        updatedAt = this[AuthorRecords.updatedAt]
    )
}