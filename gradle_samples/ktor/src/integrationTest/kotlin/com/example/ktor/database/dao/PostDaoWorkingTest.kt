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
class PostDaoWorkingTest : DatabaseTestBase() {

    private val postDao = TestPostDao()
    private val authorDao = TestAuthorDao()

    @Test
    fun `should create and retrieve post with author successfully`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Post Author", "postauthor.${uuid}@example.com"))!!
            val newPost = NewPost(
                title = "Working Post Test ${uuid}",
                content = "This is working post content",
                authorId = author.id
            )

            // When
            val createdPost = postDao.addPost(newPost)

            // Then
            assertNotNull(createdPost)
            assertEquals("Working Post Test ${uuid}", createdPost!!.title)
            assertEquals("This is working post content", createdPost.content)
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
    fun `should handle post CRUD operations`() = runTest {
        dbQuery {
            // Setup author
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("CRUD Author", "crudauthor.${uuid}@example.com"))!!

            // Create
            val originalPost = NewPost("CRUD Post ${uuid}", "Original content", author.id)
            val createdPost = postDao.addPost(originalPost)!!

            // Read
            val retrievedPost = postDao.post(createdPost.id)
            assertNotNull(retrievedPost)
            assertEquals("CRUD Post ${uuid}", retrievedPost!!.title)

            // Update
            val updatedPost = NewPost("CRUD Updated ${uuid}", "Updated content", author.id)
            val updateResult = postDao.updatePost(createdPost.id, updatedPost)
            assertTrue(updateResult)

            val updatedRetrievedPost = postDao.post(createdPost.id)
            assertEquals("CRUD Updated ${uuid}", updatedRetrievedPost!!.title)
            assertEquals("Updated content", updatedRetrievedPost.content)

            // Delete
            val deleteResult = postDao.deletePost(createdPost.id)
            assertTrue(deleteResult)

            val deletedPost = postDao.post(createdPost.id)
            assertNull(deletedPost)
        }
    }

    @Test
    fun `should find posts by author`() = runTest {
        dbQuery {
            // Setup
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author = authorDao.addAuthor(NewAuthor("Multi Post Author", "multipost.${uuid}@example.com"))!!
            val otherAuthor = authorDao.addAuthor(NewAuthor("Other Author", "other.${uuid}@example.com"))!!

            // Create posts
            val post1 = NewPost("Author Post 1 ${uuid}", "Content 1", author.id)
            val post2 = NewPost("Author Post 2 ${uuid}", "Content 2", author.id)
            val otherPost = NewPost("Other Post ${uuid}", "Other Content", otherAuthor.id)

            postDao.addPost(post1)
            postDao.addPost(post2)
            postDao.addPost(otherPost)

            // Test
            val authorPosts = postDao.postsByAuthor(author.id)
            assertEquals(2, authorPosts.size)
            
            val titles = authorPosts.map { it.title }
            assertTrue(titles.contains("Author Post 1 ${uuid}"))
            assertTrue(titles.contains("Author Post 2 ${uuid}"))
            assertFalse(titles.contains("Other Post ${uuid}"))
        }
    }
}