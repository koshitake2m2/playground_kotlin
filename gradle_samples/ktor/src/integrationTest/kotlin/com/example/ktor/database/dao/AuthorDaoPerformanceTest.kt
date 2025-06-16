package com.example.ktor.database.dao

import com.example.ktor.database.DatabaseTestBase
import com.example.ktor.models.NewAuthor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.system.measureTimeMillis
import java.util.*

@Execution(ExecutionMode.CONCURRENT)
@Disabled("Performance tests disabled for basic integration test run")
class AuthorDaoPerformanceTest : DatabaseTestBase() {

    private val authorDao = AuthorDao()

    @Test
    @ResourceLock("database")
    fun `should handle bulk author insertion efficiently`() = runTest {
        // Given
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        val numberOfAuthors = 50 // Reduced for faster execution
        val authors = (1..numberOfAuthors).map { index ->
            NewAuthor("Bulk Author $index", "bulk${index}.${uuid}@example.com")
        }

        // When
        val executionTime = measureTimeMillis {
            val createdAuthors = authors.map { author ->
                async { authorDao.addAuthor(author) }
            }.awaitAll()

            // Then
            val successfulCreations = createdAuthors.filterNotNull()
            assertEquals(numberOfAuthors, successfulCreations.size)
        }

        // Performance assertion (should complete within reasonable time)
        assertTrue(executionTime < 5000, "Bulk insertion took too long: ${executionTime}ms")
        
        // Verify all authors are in database
        val allAuthors = authorDao.allAuthors()
        val ourAuthors = allAuthors.filter { it.email.contains(uuid) }
        assertEquals(numberOfAuthors, ourAuthors.size)
    }

    @Test
    @ResourceLock("database")
    fun `should handle concurrent read operations efficiently`() = runTest {
        // Given - Create some test data first
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        val numberOfAuthors = 20 // Reduced for faster execution
        val createdAuthors = (1..numberOfAuthors).map { index ->
            authorDao.addAuthor(NewAuthor("Concurrent Author $index", "concurrent${index}.${uuid}@example.com"))
        }.filterNotNull()

        // When - Perform concurrent reads
        val executionTime = measureTimeMillis {
            val readOperations = (1..100).map {
                async {
                    val randomAuthorId = createdAuthors.random().id
                    authorDao.author(randomAuthorId)
                }
            }
            
            val results = readOperations.awaitAll()
            
            // Then
            val successfulReads = results.filterNotNull()
            assertTrue(successfulReads.size > 90, "Most reads should be successful")
        }

        assertTrue(executionTime < 3000, "Concurrent reads took too long: ${executionTime}ms")
    }

    @Test
    @ResourceLock("database")
    fun `should handle mixed operations concurrently`() = runTest {
        // Given
        val initialAuthors = (1..20).map { index ->
            authorDao.addAuthor(NewAuthor("Initial Author $index", "initial$index@example.com"))
        }.filterNotNull()

        // When - Mix of create, read, update, delete operations
        val executionTime = measureTimeMillis {
            val operations = listOf(
                // Create operations
                *(1..10).map { index ->
                    async {
                        authorDao.addAuthor(NewAuthor("New Author $index", "new$index@example.com"))
                    }
                }.toTypedArray(),
                
                // Read operations
                *(1..20).map {
                    async {
                        val randomAuthor = initialAuthors.random()
                        authorDao.author(randomAuthor.id)
                    }
                }.toTypedArray(),
                
                // Update operations
                *(1..5).map { index ->
                    async {
                        val authorToUpdate = initialAuthors[index]
                        authorDao.updateAuthor(
                            authorToUpdate.id, 
                            NewAuthor("Updated Author $index", "updated$index@example.com")
                        )
                    }
                }.toTypedArray(),
                
                // List all operations
                *(1..5).map {
                    async { authorDao.allAuthors() }
                }.toTypedArray()
            )

            val results = operations.awaitAll()
            
            // Then - Verify operations completed
            assertFalse(results.isEmpty())
        }

        assertTrue(executionTime < 4000, "Mixed operations took too long: ${executionTime}ms")
        
        // Verify final state
        val finalAuthors = authorDao.allAuthors()
        assertTrue(finalAuthors.size >= 25, "Should have at least 25 authors after mixed operations")
    }

    @Test
    @ResourceLock("database")
    fun `should maintain data consistency under concurrent operations`() = runTest {
        // Given
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        val baseEmail = "consistency.test.${uuid}@example.com"
        
        // When - Try to create multiple authors with same email concurrently
        val createOperations = (1..10).map { index ->
            async {
                try {
                    authorDao.addAuthor(NewAuthor("Consistency Test $index", baseEmail))
                } catch (e: Exception) {
                    null // Expected to fail due to unique constraint
                }
            }
        }
        
        val results = createOperations.awaitAll()
        
        // Then - Only one should succeed due to unique email constraint
        val successfulCreations = results.filterNotNull()
        assertEquals(1, successfulCreations.size, "Only one author should be created with duplicate email")
        
        // Verify database state
        val allAuthors = authorDao.allAuthors()
        val authorsWithTestEmail = allAuthors.filter { it.email == baseEmail }
        assertEquals(1, authorsWithTestEmail.size, "Database should contain exactly one author with test email")
    }
}