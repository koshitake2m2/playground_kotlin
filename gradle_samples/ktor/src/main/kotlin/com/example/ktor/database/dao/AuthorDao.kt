package com.example.ktor.database.dao

import com.example.ktor.database.AuthorRecords
import com.example.ktor.database.DatabaseFactory.dbQuery
import com.example.ktor.models.Author
import com.example.ktor.models.NewAuthor
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class AuthorRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorRecord>(AuthorRecords)
    
    var name by AuthorRecords.name
    var email by AuthorRecords.email
    var createdAt by AuthorRecords.createdAt
    var updatedAt by AuthorRecords.updatedAt
}

class AuthorDao {
    private fun resultRowToAuthor(row: ResultRow) = Author(
        id = row[AuthorRecords.id].value,
        name = row[AuthorRecords.name],
        email = row[AuthorRecords.email],
        createdAt = row[AuthorRecords.createdAt],
        updatedAt = row[AuthorRecords.updatedAt]
    )

    suspend fun allAuthors(): List<Author> = dbQuery {
        AuthorRecords.selectAll().map(::resultRowToAuthor)
    }

    suspend fun author(id: Int): Author? = dbQuery {
        AuthorRecords
            .select { AuthorRecords.id eq id }
            .map(::resultRowToAuthor)
            .singleOrNull()
    }

    suspend fun addAuthor(author: NewAuthor): Author? = dbQuery {
        val insertStatement = AuthorRecords.insert {
            it[name] = author.name
            it[email] = author.email
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToAuthor)
    }

    suspend fun updateAuthor(id: Int, author: NewAuthor): Boolean = dbQuery {
        AuthorRecords.update({ AuthorRecords.id eq id }) {
            it[name] = author.name
            it[email] = author.email
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    suspend fun deleteAuthor(id: Int): Boolean = dbQuery {
        AuthorRecords.deleteWhere { AuthorRecords.id eq id } > 0
    }
}