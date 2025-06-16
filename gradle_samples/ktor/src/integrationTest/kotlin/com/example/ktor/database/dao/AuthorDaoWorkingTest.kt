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
class AuthorDaoWorkingTest : DatabaseTestBase() {

    private val authorDao = TestAuthorDao()

    @Test
    fun `should create and retrieve author successfully`() = runTest {
        dbQuery {
            // Given
            val uniqueEmail = "working.test.${UUID.randomUUID()}@example.com"
            val newAuthor = NewAuthor(
                name = "Working Test Author",
                email = uniqueEmail
            )

            // When
            val createdAuthor = authorDao.addAuthor(newAuthor)

            // Then
            assertNotNull(createdAuthor)
            assertEquals("Working Test Author", createdAuthor!!.name)
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
    fun `should handle basic CRUD operations`() = runTest {
        dbQuery {
            // Create
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val originalAuthor = NewAuthor("CRUD Test", "crud.${uuid}@example.com")
            val createdAuthor = authorDao.addAuthor(originalAuthor)!!

            // Read
            val retrievedAuthor = authorDao.author(createdAuthor.id)
            assertNotNull(retrievedAuthor)
            assertEquals("CRUD Test", retrievedAuthor!!.name)

            // Update
            val updatedAuthor = NewAuthor("CRUD Updated", "crud.updated.${uuid}@example.com")
            val updateResult = authorDao.updateAuthor(createdAuthor.id, updatedAuthor)
            assertTrue(updateResult)

            val updatedRetrievedAuthor = authorDao.author(createdAuthor.id)
            assertEquals("CRUD Updated", updatedRetrievedAuthor!!.name)

            // Delete
            val deleteResult = authorDao.deleteAuthor(createdAuthor.id)
            assertTrue(deleteResult)

            val deletedAuthor = authorDao.author(createdAuthor.id)
            assertNull(deletedAuthor)
        }
    }

    @Test
    fun `should list authors correctly`() = runTest {
        dbQuery {
            // Given
            val uuid = UUID.randomUUID().toString().substring(0, 8)
            val author1 = NewAuthor("List Test 1", "list1.${uuid}@example.com")
            val author2 = NewAuthor("List Test 2", "list2.${uuid}@example.com")

            // When
            val createdAuthor1 = authorDao.addAuthor(author1)
            val createdAuthor2 = authorDao.addAuthor(author2)

            // Then
            assertNotNull(createdAuthor1)
            assertNotNull(createdAuthor2)

            val allAuthors = authorDao.allAuthors()
            assertTrue(allAuthors.size >= 2)

            val ourAuthors = allAuthors.filter { it.email.contains(uuid) }
            assertEquals(2, ourAuthors.size)
        }
    }
}