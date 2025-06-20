# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

### Build and Running
```bash
# Compile the project
./gradlew compileTestKotlin

# Build specific modules
./gradlew :ktor:build

# Run applications
./gradlew :ktor:run           # Start Ktor web server (port 10000)

# Hot reload development (requires 2 terminals)
# Terminal A: Watch for changes
./gradlew -t :ktor:build -x test -i
# Terminal B: Auto-restart server
./gradlew -t :ktor:run
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for specific modules
./gradlew :kotlin:test
./gradlew :ktor:test

# Run integration tests (ktor module)
./gradlew :ktor:integrationTest --rerun-tasks --info
```

### Database Setup
```bash
# Start required database infrastructure
docker compose -f docker/compose.yml up -d

# View database schema (custom task)
./gradlew :ktor:showTables
```

## Architecture Overview

### Module Structure
- **kotlin**: Core Kotlin language feature demonstrations (sorted lists, delegation patterns, contracts)
- **ktor**: Web server with REST API using Exposed ORM for MySQL database operations
- **buildSrc**: Convention plugins for standardizing build configurations across modules

### Ktor Module Architecture
The ktor module follows a layered architecture:
- **Web Layer**: `Application.kt`, `Routing.kt` - Ktor server configuration and routes
- **Data Layer**: 
  - `Tables.kt` - Database schema definitions using Exposed DSL
  - `dao/` - Data Access Objects for direct database operations
  - `repository/` - Repository pattern implementation abstracting DAOs
- **Models**: Domain entities with JSON serialization support
- **Configuration**: `DatabaseFactory.kt` - Database connection management with HikariCP pooling

### Testing Architecture
- **Unit Tests**: Kotest for business logic testing
- **Integration Tests**: TestContainers-based tests with real MySQL database
  - `SingleDatabaseTestContainer` - Shared container instance for performance optimization
  - `DatabaseTestBase` - Base class providing transaction isolation with automatic rollback
  - Separate `integrationTest` source set for organization

### Convention Plugins (buildSrc)
- `buildlogic.kotlin-common-conventions` - Base Kotlin and JUnit configuration
- `buildlogic.kotlin-application-conventions` - Application-specific setup
- `buildlogic.kotlin-library-conventions` - Library-specific setup  
- `buildlogic.test-conventions` - Enhanced test reporting with class/method counts and custom test listing tasks

## Key Features and Patterns

### Database Integration
- **Exposed ORM** with MySQL backend and HikariCP connection pooling
- **TestContainers integration** with shared container strategy for fast integration tests
- **Transaction-based test isolation** - each test runs in a transaction that's automatically rolled back
- **Dual database setup**: Standard MySQL (port 3310) and pre-migrated version (port 3311)

### Flow and Coroutines Examples
Located in `ktor/src/main/kotlin/com/example/kotlinx/`:
- **FlowSamples.kt**: Basic Flow patterns, StateFlow, SharedFlow, error handling, debouncing
- **FlowAdvancedSamples.kt**: CallbackFlow, ChannelFlow, custom operators, circuit breaker patterns
- **WaitLazyListItem.kt**: Parallel processing with `async`/`await` patterns

### Custom Gradle Tasks
- **listTestClasses**: Analyzes source files to extract test class names using regex
- **listTestClassesFromTestTask**: Lists test classes from compiled .class files
- **showTables**: Displays database schema information
- **Enhanced test reporting**: Automatic display of test class count, method count, and pass/fail/skip statistics

### Testing Best Practices
- **Parallel test execution** configured for optimal performance
- **Kotest BDD-style testing** with `DescribeSpec` for readable test organization
- **TestContainers reuse** - single shared database container across all integration tests
- **Custom test fixtures** with `TestAuthorDao` and `TestPostDao` for consistent test data

## Development Workflow

### Recommended Setup
1. Start database: `docker compose -f docker/compose.yml up -d`
2. For Ktor development: Use hot reload with dual terminal setup
3. For testing: Always use `--rerun-tasks` flag for integration tests to ensure clean state
4. IDE: Install Kotest plugin for enhanced test support

### Key Dependencies
- **Kotlin 2.0.0** with Java 17 toolchain
- **Ktor 2.3.12** for web framework
- **Exposed ORM** for database operations
- **JUnit 5 + Kotest** for testing
- **TestContainers** for integration testing
- **HikariCP** for connection pooling