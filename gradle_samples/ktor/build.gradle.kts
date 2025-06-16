val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.3.12"
    id("buildlogic.kotlin-library-conventions")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

application {
//    mainClass.set("io.ktor.server.netty.EngineMain")
    mainClass.set("com.example.ktor.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")


    implementation("io.insert-koin:koin-core:3.5.6")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    
    // MySQL Connector
    implementation("mysql:mysql-connector-java:8.0.33")
    
    // HikariCP for connection pooling
    implementation("com.zaxxer:HikariCP:5.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.testcontainers:testcontainers:1.21.1")
    testImplementation("org.testcontainers:mysql:1.21.1")
    testImplementation("org.testcontainers:junit-jupiter:1.21.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 3
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "3")
    
    // TestContainers configuration
    systemProperty("testcontainers.reuse.enable", "true")
}

// Create integration test source set
sourceSets {
    create("integrationTest") {
        kotlin {
            srcDir("src/integrationTest/kotlin")
        }
        resources {
            srcDir("src/integrationTest/resources")
        }
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

// Configure integration test dependencies
configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

// Create integration test task
val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
    
    useJUnitPlatform()
    maxParallelForks = 3
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
    systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "3")
    
    // TestContainers configuration
    systemProperty("testcontainers.reuse.enable", "true")
}

// Fix duplicate resource handling
tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.check { dependsOn(integrationTest) }

// カスタムタスク: データベーステーブル情報を表示
tasks.register("showTables") {
    group = "database"
    description = "Show database table information"
    
    doLast {
        println("Database Tables:")
        println("- AuthorRecords: author_records")
        println("  - id: INT (PK, AUTO_INCREMENT)")
        println("  - name: VARCHAR(255)")
        println("  - email: VARCHAR(255) (UNIQUE)")
        println("  - created_at: DATETIME")
        println("  - updated_at: DATETIME")
        println()
        println("- PostRecords: post_records")
        println("  - id: INT (PK, AUTO_INCREMENT)")
        println("  - title: VARCHAR(255)")
        println("  - content: TEXT")
        println("  - author_id: INT (FK -> author_records.id)")
        println("  - created_at: DATETIME")
        println("  - updated_at: DATETIME")
    }
}