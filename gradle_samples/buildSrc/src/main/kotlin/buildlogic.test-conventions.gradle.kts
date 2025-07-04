import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.kotlin.dsl.KotlinClosure2

plugins {
    kotlin("jvm")
}


val testOutputLog = project.layout.buildDirectory.file("test-logs/test-output.log").get().asFile
val standardOutputListener = StandardOutputListener { message ->
    testOutputLog.appendText(message.toString())
}

gradle.taskGraph.whenReady {

    tasks.withType<Test>().configureEach {
        doFirst {
            println("Running tests...")
            testOutputLog.parentFile.mkdirs()
            testOutputLog.writeText("") // Clear the log file before test run.
            logging.addStandardOutputListener(standardOutputListener)
            logging.addStandardErrorListener(standardOutputListener)
        }
        doLast {
            println("Finished running tests.")
            logging.removeStandardOutputListener(standardOutputListener)
            logging.removeStandardErrorListener(standardOutputListener)
        }
    }
}
tasks.named<Test>("test") {
//    dependsOn("checkTestInclusion")
    useJUnitPlatform()
    filter {
        includeTestsMatching("*Test")
        includeTestsMatching("*Spec")
    }
    // We don't need the following if we use `testExcluded` task.
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
    doLast {
//        val checker = TestOutputChecker()
//        checker.checkForPreconditionViolation(testOutputLog)
    }
}

tasks.register<Test>("testExcluded") {
    useJUnitPlatform()
    filter {
        excludeTestsMatching("*Test")
        excludeTestsMatching("*Spec")
    }
//    systemProperty("kotest.filter.specs", "*(Test|Spec)")
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
    doLast {
    }
}

// Register the custom tasks
tasks.register<ListTestClassesFromSourceTask>("listTestClassesFromSource")

tasks.register<ListTestClassesFromCompiledTask>("listTestClassesFromCompiled") {
    dependsOn("testClasses")
}

tasks.register<CheckTestInclusionTask>("checkTestInclusion") {
    dependsOn("testClasses")
}

tasks.register<CheckTestFileNamingTask>("checkTestFileNaming")
