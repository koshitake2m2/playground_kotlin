package com.example.ktor.database.dao

import com.example.ktor.database.DatabaseTestBase
import com.example.ktor.models.NewAuthor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.*

class AuthorDaoSimpleTest : DatabaseTestBase() {

    private val authorDao = TestAuthorDao()

    @Test
    fun `should create and retrieve author with unique email 1`() = runTest {
        // Execute the test within the transaction context
        dbQuery {
            // Given
            val uniqueEmail = "test1_${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor(
                name = "Test Author 1",
                email = uniqueEmail
            )

            // When
            val createdAuthor = authorDao.addAuthor(newAuthor)

            // Then
            assertNotNull(createdAuthor)
            assertEquals("Test Author 1", createdAuthor!!.name)
            assertEquals(uniqueEmail, createdAuthor.email)
            assertTrue(createdAuthor.id > 0)
        }
    }

    @Test
    fun `should create and retrieve author with unique email 2`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "test2_${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor(
                name = "Test Author 2",
                email = uniqueEmail
            )

            // When
            val createdAuthor = authorDao.addAuthor(newAuthor)

            // Then
            assertNotNull(createdAuthor)
            assertEquals("Test Author 2", createdAuthor!!.name)
            assertEquals(uniqueEmail, createdAuthor.email)
            assertTrue(createdAuthor.id > 0)
        }
    }

    @Test
    fun `should create and retrieve author with unique email 3`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "test3_${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor(
                name = "Test Author 3",
                email = uniqueEmail
            )

            // When
            val createdAuthor = authorDao.addAuthor(newAuthor)

            // Then
            assertNotNull(createdAuthor)
            assertEquals("Test Author 3", createdAuthor!!.name)
            assertEquals(uniqueEmail, createdAuthor.email)
            assertTrue(createdAuthor.id > 0)
        }
    }

    @Test
    fun `should update author with unique email`() = runTest {
        dbQuery {
            // Given
            val originalEmail = "original_${UUID.randomUUID()}@example.com"
            val updatedEmail = "updated_${UUID.randomUUID()}@example.com"
            
            val originalAuthor = NewAuthor("Original Name", originalEmail)
            val createdAuthor = authorDao.addAuthor(originalAuthor)!!

            // When
            val updatedAuthor = NewAuthor("Updated Name", updatedEmail)
            val updateResult = authorDao.updateAuthor(createdAuthor.id, updatedAuthor)

            // Then
            assertTrue(updateResult)

            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNotNull(retrievedAuthor)
            assertEquals("Updated Name", retrievedAuthor!!.name)
            assertEquals(updatedEmail, retrievedAuthor.email)
        }
    }

    @Test
    fun `should delete author with unique email`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "delete_${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor("To Delete", uniqueEmail)
            val createdAuthor = authorDao.addAuthor(newAuthor)!!

            // When
            val deleteResult = authorDao.deleteAuthor(createdAuthor.id)

            // Then
            assertTrue(deleteResult)

            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNull(retrievedAuthor)
        }
    }
}