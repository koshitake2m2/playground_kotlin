import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.KotlinClosure2

plugins {
    kotlin("jvm")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Test")
        includeTestsMatching("*Spec")
    }
    systemProperty("kotest.filter.specs", "*(Test|Spec)")
    ignoreFailures = false

    val testClasses = mutableSetOf<String>()
    
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        
        afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, _ ->
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

// Register the custom tasks
tasks.register<ListTestClassesFromSourceTask>("listTestClassesFromSource")

tasks.register<ListTestClassesFromCompiledTask>("listTestClassesFromCompiled") {
    dependsOn("testClasses")
}