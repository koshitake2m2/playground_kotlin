package com.example.ktor.database.dao

import com.example.ktor.database.AuthorRecords
import com.example.ktor.database.PostRecords
import com.example.ktor.database.TestDatabaseFactory.dbQuery
import com.example.ktor.models.NewPost
import com.example.ktor.models.Post
import com.example.ktor.models.PostWithAuthor
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class TestPostDao {
    private fun resultRowToPost(row: ResultRow) = Post(
        id = row[PostRecords.id].value,
        title = row[PostRecords.title],
        content = row[PostRecords.content],
        authorId = row[PostRecords.authorId].value,
        createdAt = row[PostRecords.createdAt],
        updatedAt = row[PostRecords.updatedAt]
    )

    private fun resultRowToPostWithAuthor(row: ResultRow) = PostWithAuthor(
        id = row[PostRecords.id].value,
        title = row[PostRecords.title],
        content = row[PostRecords.content],
        authorId = row[PostRecords.authorId].value,
        authorName = row[AuthorRecords.name],
        authorEmail = row[AuthorRecords.email],
        createdAt = row[PostRecords.createdAt],
        updatedAt = row[PostRecords.updatedAt]
    )

    fun allPosts(): List<Post> {
        return PostRecords.selectAll().map(::resultRowToPost)
    }

    fun post(id: Int): Post? {
        return PostRecords
            .select { PostRecords.id eq id }
            .map(::resultRowToPost)
            .singleOrNull()
    }

    fun postWithAuthor(id: Int): PostWithAuthor? {
        return (PostRecords innerJoin AuthorRecords)
            .select { PostRecords.id eq id }
            .map(::resultRowToPostWithAuthor)
            .singleOrNull()
    }

    fun allPostsWithAuthors(): List<PostWithAuthor> {
        return (PostRecords innerJoin AuthorRecords)
            .selectAll()
            .map(::resultRowToPostWithAuthor)
    }

    fun postsByAuthor(authorId: Int): List<Post> {
        return PostRecords
            .select { PostRecords.authorId eq authorId }
            .map(::resultRowToPost)
    }

    fun addPost(post: NewPost): Post? {
        val insertStatement = PostRecords.insert {
            it[title] = post.title
            it[content] = post.content
            it[authorId] = post.authorId
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        return insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPost)
    }

    fun updatePost(id: Int, post: NewPost): Boolean {
        return PostRecords.update({ PostRecords.id eq id }) {
            it[title] = post.title
            it[content] = post.content
            it[authorId] = post.authorId
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    fun deletePost(id: Int): Boolean {
        return PostRecords.deleteWhere { PostRecords.id eq id } > 0
    }
}