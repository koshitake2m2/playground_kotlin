package com.example.ktor.database.dao

import com.example.ktor.database.DatabaseTestBase
import com.example.ktor.models.NewAuthor
import com.example.ktor.models.NewPost
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.*

@Execution(ExecutionMode.CONCURRENT)
class PostDaoIntegrationTest : DatabaseTestBase() {

    private val postDao = TestPostDao()
    private val authorDao = TestAuthorDao()

    @Test
    fun `should create and retrieve post with author`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Post Author", "postauthor.${uuid}@example.com"))!!
            val newPost = NewPost(
                title = "Test Post ${uuid}",
                content = "This is a test post content",
                authorId = author.id
            )

            // When
            val createdPost = postDao.addPost(newPost)

            // Then
            assertNotNull(createdPost)
            assertEquals("Test Post ${uuid}", createdPost!!.title)
            assertEquals("This is a test post content", createdPost.content)
            assertEquals(author.id, createdPost.authorId)
            assertTrue(createdPost.id > 0)

            // Test retrieval with author information
            val postWithAuthor = postDao.postWithAuthor(createdPost.id)
            assertNotNull(postWithAuthor)
            assertEquals(createdPost.id, postWithAuthor!!.id)
            assertEquals("Post Author", postWithAuthor.authorName)
            assertEquals("postauthor.${uuid}@example.com", postWithAuthor.authorEmail)
        }
    }

    @Test
    fun `should list all posts with authors`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author1 = authorDao.addAuthor(NewAuthor("Author 1", "author1.${uuid}@example.com"))!!
            val author2 = authorDao.addAuthor(NewAuthor("Author 2", "author2.${uuid}@example.com"))!!
            
            val post1 = NewPost("Post 1 ${uuid}", "Content 1", author1.id)
            val post2 = NewPost("Post 2 ${uuid}", "Content 2", author2.id)

            // When
            val createdPost1 = postDao.addPost(post1)
            val createdPost2 = postDao.addPost(post2)

            // Then
            assertNotNull(createdPost1)
            assertNotNull(createdPost2)

            val allPosts = postDao.allPostsWithAuthors()
            assertTrue(allPosts.size >= 2)
            
            // Find our specific posts
            val ourPosts = allPosts.filter { it.title.contains(uuid) }
            assertEquals(2, ourPosts.size)
            
            val titles = ourPosts.map { it.title }
            assertTrue(titles.contains("Post 1 ${uuid}"))
            assertTrue(titles.contains("Post 2 ${uuid}"))
            
            val authorNames = ourPosts.map { it.authorName }
            assertTrue(authorNames.contains("Author 1"))
            assertTrue(authorNames.contains("Author 2"))
        }
    }

    @Test
    fun `should update post`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Update Author", "updateauthor.${uuid}@example.com"))!!
            val originalPost = NewPost("Original Title ${uuid}", "Original Content", author.id)
            val createdPost = postDao.addPost(originalPost)!!

            // When
            val updatedPost = NewPost("Updated Title ${uuid}", "Updated Content", author.id)
            val updateResult = postDao.updatePost(createdPost.id, updatedPost)

            // Then
            assertTrue(updateResult)

            val retrievedPost = postDao.post(createdPost.id)
            assertNotNull(retrievedPost)
            assertEquals("Updated Title ${uuid}", retrievedPost!!.title)
            assertEquals("Updated Content", retrievedPost.content)
            assertEquals(createdPost.id, retrievedPost.id)
        }
    }

    @Test
    fun `should delete post`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Delete Author", "deleteauthor.${uuid}@example.com"))!!
            val newPost = NewPost("To Delete ${uuid}", "Delete Content", author.id)
            val createdPost = postDao.addPost(newPost)!!

            // When
            val deleteResult = postDao.deletePost(createdPost.id)

            // Then
            assertTrue(deleteResult)

            val retrievedPost = postDao.post(createdPost.id)
            assertNull(retrievedPost)
        }
    }

    @Test
    fun `should find posts by author`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Multi Post Author", "multipost.${uuid}@example.com"))!!
            val otherAuthor = authorDao.addAuthor(NewAuthor("Other Author", "other.${uuid}@example.com"))!!
            
            val post1 = NewPost("Author Post 1 ${uuid}", "Content 1", author.id)
            val post2 = NewPost("Author Post 2 ${uuid}", "Content 2", author.id)
            val otherPost = NewPost("Other Post ${uuid}", "Other Content", otherAuthor.id)

            // When
            postDao.addPost(post1)
            postDao.addPost(post2)
            postDao.addPost(otherPost)

            val authorPosts = postDao.postsByAuthor(author.id)

            // Then
            assertEquals(2, authorPosts.size)
            val titles = authorPosts.map { it.title }
            assertTrue(titles.contains("Author Post 1 ${uuid}"))
            assertTrue(titles.contains("Author Post 2 ${uuid}"))
            assertFalse(titles.contains("Other Post ${uuid}"))
        }
    }
}