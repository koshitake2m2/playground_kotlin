package com.example.ktor.models

import java.time.LocalDateTime

data class Author(
    val id: Int,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class NewAuthor(
    val name: String,
    val email: String
)

data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class NewPost(
    val title: String,
    val content: String,
    val authorId: Int
)

data class PostWithAuthor(
    val id: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val authorName: String,
    val authorEmail: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)