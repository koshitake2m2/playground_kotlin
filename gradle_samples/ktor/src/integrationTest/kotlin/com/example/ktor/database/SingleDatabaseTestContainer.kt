package com.example.ktor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Single database container for all integration tests
 */
object SingleDatabaseTestContainer {
    private val initialized = AtomicBoolean(false)
    private var container: MySQLContainer<*>? = null
    private var database: Database? = null
    
    fun getDatabase(): Database {
        initialize()
        return database ?: throw IllegalStateException("Database not initialized")
    }
    
    @Synchronized
    private fun initialize() {
        if (initialized.compareAndSet(false, true)) {
            try {
                println("Initializing single test database container...")
                
                val testContainer = MySQLContainer(
                    DockerImageName.parse("gradle_samples-mysql-migrated:latest")
                        .asCompatibleSubstituteFor("mysql")
                )
                    .withDatabaseName("sample_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withReuse(false)
                    .withStartupTimeout(java.time.Duration.ofMinutes(5))
                
                // Start container
                try {
                    testContainer.start()
                    println("Test database container started successfully on port ${testContainer.getMappedPort(3306)}")
                } catch (e: Exception) {
                    println("Failed to start test database container: ${e.message}")
                    // Reset the initialized flag so another thread can try
                    initialized.set(false)
                    throw e
                }
                
                // Store the container reference
                container = testContainer
                
                // Create database connection with higher connection pool for concurrent tests
                val config = HikariConfig().apply {
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    jdbcUrl = testContainer.jdbcUrl + "?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true"
                    username = testContainer.username
                    password = testContainer.password
                    maximumPoolSize = 15 // Increased pool size for concurrent tests
                    minimumIdle = 5
                    isAutoCommit = false // Important for transaction-based isolation
                    transactionIsolation = "TRANSACTION_READ_COMMITTED" // Less restrictive to avoid deadlocks
                    connectionTimeout = 30000
                    idleTimeout = 600000
                    maxLifetime = 1800000
                    poolName = "IntegrationTestPool"
                }
                
                database = Database.connect(HikariDataSource(config))
                
                println("Test database connection established")
                
                // Register shutdown hook
                Runtime.getRuntime().addShutdownHook(Thread {
                    shutdown()
                })
            } catch (e: Exception) {
                // Reset the initialized flag if initialization failed
                initialized.set(false)
                throw e
            }
        } else {
            // Wait for initialization to complete if another thread is doing it
            var attempts = 0
            while (database == null && attempts < 30) {
                Thread.sleep(1000)
                attempts++
            }
            if (database == null) {
                throw IllegalStateException("Database initialization timed out")
            }
        }
    }
    
    private fun shutdown() {
        println("Shutting down test database container...")
        try {
            container?.let {
                it.stop()
            }
        } catch (e: Exception) {
            println("Error stopping test database container: ${e.message}")
        }
    }
}