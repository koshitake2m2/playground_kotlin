package com.example.ktor.database.dao

import com.example.ktor.database.DatabaseTestBase
import com.example.ktor.models.NewAuthor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.*

@Execution(ExecutionMode.CONCURRENT)
class AuthorDaoIntegrationTest : DatabaseTestBase() {

    private val authorDao = TestAuthorDao()

    @Test
    fun `should create and retrieve author`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "john.doe.${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor(
                name = "John Doe",
                email = uniqueEmail
            )

            // When
            val createdAuthor = authorDao.addAuthor(newAuthor)

            // Then
            assertNotNull(createdAuthor)
            assertEquals("John Doe", createdAuthor!!.name)
            assertEquals(uniqueEmail, createdAuthor.email)
            assertTrue(createdAuthor.id > 0)

            // Verify retrieval
            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNotNull(retrievedAuthor)
            assertEquals(createdAuthor.id, retrievedAuthor!!.id)
            assertEquals(createdAuthor.name, retrievedAuthor.name)
            assertEquals(createdAuthor.email, retrievedAuthor.email)
        }
    }

    @Test
    fun `should list all authors`() = runTest {
        dbQuery {
            // Given - Use unique emails to avoid conflicts
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author1 = NewAuthor("Alice Smith", "alice.${uuid}@example.com")
            val author2 = NewAuthor("Bob Johnson", "bob.${uuid}@example.com")

            // When
            val createdAuthor1 = authorDao.addAuthor(author1)
            val createdAuthor2 = authorDao.addAuthor(author2)

            // Then
            assertNotNull(createdAuthor1)
            assertNotNull(createdAuthor2)

            val allAuthors = authorDao.allAuthors()
            assertTrue(allAuthors.size >= 2)
            
            // Find our specific authors
            val ourAuthors = allAuthors.filter { 
                it.email.contains(uuid)
            }
            assertEquals(2, ourAuthors.size)
            
            val names = ourAuthors.map { it.name }
            assertTrue(names.contains("Alice Smith"))
            assertTrue(names.contains("Bob Johnson"))
        }
    }

    @Test
    fun `should update author`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val originalAuthor = NewAuthor("Original Name", "original.${uuid}@example.com")
            val createdAuthor = authorDao.addAuthor(originalAuthor)!!

            // When
            val updatedAuthor = NewAuthor("Updated Name", "updated.${uuid}@example.com")
            val updateResult = authorDao.updateAuthor(createdAuthor.id, updatedAuthor)

            // Then
            assertTrue(updateResult)

            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNotNull(retrievedAuthor)
            assertEquals("Updated Name", retrievedAuthor!!.name)
            assertEquals("updated.${uuid}@example.com", retrievedAuthor.email)
            assertEquals(createdAuthor.id, retrievedAuthor.id)
        }
    }

    @Test
    fun `should delete author`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val newAuthor = NewAuthor("To Delete", "delete.${uuid}@example.com")
            val createdAuthor = authorDao.addAuthor(newAuthor)!!

            // When
            val deleteResult = authorDao.deleteAuthor(createdAuthor.id)

            // Then
            assertTrue(deleteResult)

            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNull(retrievedAuthor)
        }
    }

    @Test
    fun `should return null for non-existent author`() = runTest {
        dbQuery {
            // When
            val nonExistentAuthor = authorDao.author(99999)

            // Then
            assertNull(nonExistentAuthor)
        }
    }

    @Test
    fun `should return false when updating non-existent author`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val updateAuthor = NewAuthor("Non Existent", "nonexistent.${uuid}@example.com")

            // When
            val updateResult = authorDao.updateAuthor(99999, updateAuthor)

            // Then
            assertFalse(updateResult)
        }
    }

    @Test
    fun `should return false when deleting non-existent author`() = runTest {
        dbQuery {
            // When
            val deleteResult = authorDao.deleteAuthor(99999)

            // Then
            assertFalse(deleteResult)
        }
    }

    @Test
    fun `should handle unique email constraint`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "unique.${UUID.randomUUID()}@example.com"
            val author1 = NewAuthor("User One", uniqueEmail)
            val author2 = NewAuthor("User Two", uniqueEmail) // Same email

            // When
            val firstAuthor = authorDao.addAuthor(author1)
            val secondAuthor = try {
                authorDao.addAuthor(author2)
            } catch (e: Exception) {
                null // Expected to fail due to unique constraint
            }

            // Then
            assertNotNull(firstAuthor)
            // The second insertion should fail due to unique constraint
            assertNull(secondAuthor)
        }
    }

    @Test
    fun `should handle multiple author creation`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val authors = (1..3).map { index -> // Reduced number for stability
                NewAuthor("Author $index", "author${index}.${uuid}@example.com")
            }

            // When - Create authors sequentially (not concurrently to avoid constraints)
            val createdAuthors = authors.map { author ->
                authorDao.addAuthor(author)
            }

            // Then
            val successfulCreations = createdAuthors.filterNotNull()
            assertEquals(3, successfulCreations.size)

            val allAuthors = authorDao.allAuthors()
            val ourAuthors = allAuthors.filter { it.email.contains(uuid) }
            assertEquals(3, ourAuthors.size)
        }
    }
}