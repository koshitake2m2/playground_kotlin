package com.example.ktor.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object TestDatabaseFactory {
    // Thread-local storage for database connection
    private val threadLocalDatabase = ThreadLocal<Database>()
    
    fun setDatabase(database: Database) {
        threadLocalDatabase.set(database)
    }
    
    fun clearDatabase() {
        threadLocalDatabase.remove()
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T {
        val database = threadLocalDatabase.get() 
            ?: throw IllegalStateException("No database set for current thread")
        
        // Create a new suspended transaction
        return newSuspendedTransaction(Dispatchers.IO, database) { block() }
    }
}