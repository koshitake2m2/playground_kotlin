package com.example.ktor.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
abstract class DatabaseTestBase {

    private lateinit var database: Database

    @BeforeEach
    fun setupDatabase() {
        database = SingleDatabaseTestContainer.getDatabase()
    }

    @AfterEach
    fun teardownDatabase() {
        // Transaction rollback happens automatically in dbQuery
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { 
            try {
                val result = block()
                rollback() // Always rollback to ensure test isolation
                result
            } catch (e: Exception) {
                rollback()
                throw e
            }
        }
}