import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.KotlinClosure2

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.3.12"
}

application {
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    
    val testClasses = mutableSetOf<String>()
    
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        
        afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
            // Extract test class name from the descriptor
            val className = descriptor.className
            if (className != null) {
                testClasses.add(className)
            }
        }))
        
        afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ suite, result ->
            if (suite.parent == null) { // root suite
                println("\n==========================================")
                println("Test Results Summary:")
                println("Test Classes: ${testClasses.size}")
                println("Test Methods: ${result.testCount}")
                println("Passed: ${result.successfulTestCount}")
                println("Failed: ${result.failedTestCount}")
                println("Skipped: ${result.skippedTestCount}")
                println("==========================================")
            }
        }))
    }
}

tasks.register("listTestClasses") {
    description = "Lists all test classes in the kotlin module"
    group = "verification"
    
    doLast {
        val testSourceSet = sourceSets["test"]
        val testKotlinFiles = testSourceSet.kotlin.files
        
        println("\n========== Test Classes in :kotlin module ==========")
        
        var count = 0
        testKotlinFiles
            .filter { it.extension == "kt" }
            .forEach { file ->
                val content = file.readText()
                // Match class declarations including those that extend other classes
                val classPattern = """class\s+(\w+)\s*(?::\s*[\w<>,\s()]+)?\s*\{""".toRegex()
                val matches = classPattern.findAll(content)
                
                matches.forEach { match ->
                    val className = match.groupValues[1]
                    // Include all classes in test files, not just those ending with specific suffixes
                    count++
                    val packageName = content.lines()
                        .firstOrNull { it.startsWith("package ") }
                        ?.removePrefix("package ")
                        ?.trim()
                    
                    val fullClassName = if (packageName != null) {
                        "$packageName.$className"
                    } else {
                        className
                    }
                    
                    println("  $count. $fullClassName")
                }
            }
        
        println("\nTotal test classes found: $count")
        println("====================================================")
    }
}
