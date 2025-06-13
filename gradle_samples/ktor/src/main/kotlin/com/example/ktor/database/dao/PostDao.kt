package com.example.ktor.database.dao

import com.example.ktor.database.AuthorRecords
import com.example.ktor.database.DatabaseFactory.dbQuery
import com.example.ktor.database.PostRecords
import com.example.ktor.models.NewPost
import com.example.ktor.models.Post
import com.example.ktor.models.PostWithAuthor
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class PostRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PostRecord>(PostRecords)
    
    var title by PostRecords.title
    var content by PostRecords.content
    var authorId by PostRecords.authorId
    var createdAt by PostRecords.createdAt
    var updatedAt by PostRecords.updatedAt
    
    var author by AuthorRecord referencedOn PostRecords.authorId
}

class PostDao {
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

    suspend fun allPosts(): List<Post> = dbQuery {
        PostRecords.selectAll().map(::resultRowToPost)
    }
    
    suspend fun allPostsWithAuthors(): List<PostWithAuthor> = dbQuery {
        (PostRecords innerJoin AuthorRecords)
            .selectAll()
            .map(::resultRowToPostWithAuthor)
    }

    suspend fun post(id: Int): Post? = dbQuery {
        PostRecords
            .select { PostRecords.id eq id }
            .map(::resultRowToPost)
            .singleOrNull()
    }
    
    suspend fun postWithAuthor(id: Int): PostWithAuthor? = dbQuery {
        (PostRecords innerJoin AuthorRecords)
            .select { PostRecords.id eq id }
            .map(::resultRowToPostWithAuthor)
            .singleOrNull()
    }

    suspend fun addPost(post: NewPost): Post? = dbQuery {
        val insertStatement = PostRecords.insert {
            it[title] = post.title
            it[content] = post.content
            it[authorId] = post.authorId
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPost)
    }

    suspend fun updatePost(id: Int, post: NewPost): Boolean = dbQuery {
        PostRecords.update({ PostRecords.id eq id }) {
            it[title] = post.title
            it[content] = post.content
            it[authorId] = post.authorId
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    suspend fun deletePost(id: Int): Boolean = dbQuery {
        PostRecords.deleteWhere { PostRecords.id eq id } > 0
    }
    
    suspend fun postsByAuthor(authorId: Int): List<Post> = dbQuery {
        PostRecords
            .select { PostRecords.authorId eq authorId }
            .map(::resultRowToPost)
    }
}