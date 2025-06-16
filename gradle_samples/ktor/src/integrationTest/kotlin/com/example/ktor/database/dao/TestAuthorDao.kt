package com.example.ktor.database.dao

import com.example.ktor.database.AuthorRecords
import com.example.ktor.database.TestDatabaseFactory.dbQuery
import com.example.ktor.models.Author
import com.example.ktor.models.NewAuthor
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class TestAuthorDao {
    private fun resultRowToAuthor(row: ResultRow) = Author(
        id = row[AuthorRecords.id].value,
        name = row[AuthorRecords.name],
        email = row[AuthorRecords.email],
        createdAt = row[AuthorRecords.createdAt],
        updatedAt = row[AuthorRecords.updatedAt]
    )

    fun allAuthors(): List<Author> {
        return AuthorRecords.selectAll().map(::resultRowToAuthor)
    }

    fun author(id: Int): Author? {
        return AuthorRecords
            .select { AuthorRecords.id eq id }
            .map(::resultRowToAuthor)
            .singleOrNull()
    }

    fun addAuthor(author: NewAuthor): Author? {
        val insertStatement = AuthorRecords.insert {
            it[name] = author.name
            it[email] = author.email
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAuthor)
    }

    fun updateAuthor(id: Int, author: NewAuthor): Boolean {
        return AuthorRecords.update({ AuthorRecords.id eq id }) {
            it[name] = author.name
            it[email] = author.email
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun deleteAuthor(id: Int): Boolean {
        return AuthorRecords.deleteWhere { AuthorRecords.id eq id } > 0
    }
}