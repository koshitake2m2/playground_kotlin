package com.example.ktor.database

object DatabaseConfig {
    // Standard MySQL with Flyway (port 3310)
    const val MYSQL_FLYWAY_URL = "jdbc:mysql://localhost:3310/sample_db?useSSL=false&allowPublicKeyRetrieval=true"
    
    // Pre-migrated MySQL (port 3311) 
    const val MYSQL_MIGRATED_URL = "jdbc:mysql://localhost:3311/sample_db?useSSL=false&allowPublicKeyRetrieval=true"
    
    const val USERNAME = "test_user"
    const val PASSWORD = "test_password"
    
    // Use environment variable to switch between databases
    fun getJdbcUrl(): String {
        return when (System.getenv("DB_TYPE")) {
            "migrated" -> MYSQL_MIGRATED_URL
            else -> MYSQL_FLYWAY_URL
        }
    }
}