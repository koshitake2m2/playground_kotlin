import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import java.io.File

/**
 * Task that checks if all discovered test classes match the includeTestsMatching patterns.
 * This helps identify test classes that might be accidentally excluded from test execution.
 */
open class CheckTestInclusionTask : DefaultTask() {
    init {
        group = "verification"
        description = "Detects test classes that don't match includeTestsMatching patterns"
    }
    
    @TaskAction
    fun checkTestInclusion() {
        val testTask = project.tasks.findByName("test") as? Test
        if (testTask == null) {
            logger.warn("No test task found in project ${project.name}")
            return
        }
        
        // Get the include patterns from the test task
        val includePatterns = testTask.filter.includePatterns
        println("includePatterns: $includePatterns")
        if (includePatterns.isEmpty()) {
            logger.info("No includeTestsMatching patterns configured")
            return
        }
        
        logger.lifecycle("Checking test inclusion with patterns: $includePatterns")
        
        // Use JUnit Platform to discover actual test classes
        val testClassesDir = project.layout.buildDirectory.dir("classes/kotlin/test").get().asFile
        val detector = JUnitTestClassDetector()
        val junitTestClasses = detector.discoverJUnitTestClasses(testClassesDir)
        
        // Check which JUnit-recognized test classes don't match the patterns
        val excludedClasses = findExcludedTestClasses(junitTestClasses, includePatterns)
        
        // Report results
        reportJUnitBasedResults(junitTestClasses, excludedClasses, includePatterns)
    }
    
    
    /**
     * Reports the results using JUnit Platform discovery.
     */
    private fun reportJUnitBasedResults(
        junitTestClasses: Set<String>,
        excludedClasses: Set<String>,
        includePatterns: Set<String>
    ) {
        println("\n========== Test Inclusion Check Results (JUnit-based) ==========")
        println("Include patterns: ${includePatterns.joinToString(", ")}")
        println("Total test classes discovered by JUnit: ${junitTestClasses.size}")
        println("Test classes matching patterns: ${junitTestClasses.size - excludedClasses.size}")
        println("Test classes NOT matching patterns: ${excludedClasses.size}")
        
        if (excludedClasses.isNotEmpty()) {
            println("\nThe following JUnit test classes don't match any includeTestsMatching patterns:")
            excludedClasses.sorted().forEach { className ->
                println("  ❌ $className")
            }
            println("\nThese tests will NOT be executed!")
            println("Consider updating your includeTestsMatching patterns or renaming the test classes.")
            println("==================================================================\n")
            
            throw GradleException(
                "${excludedClasses.size} test classes don't match includeTestsMatching patterns and won't be executed."
            )
        } else {
            println("\n✅ All JUnit test classes match the include patterns and will be executed.")
            println("==================================================================\n")
        }
    }
    
    
    /**
     * Finds test classes that don't match any of the include patterns.
     */
    internal fun findExcludedTestClasses(
        allTestClasses: Set<String>, 
        includePatterns: Set<String>
    ): Set<String> {
        return allTestClasses.filter { testClass ->
            !matchesAnyPattern(testClass, includePatterns)
        }.toSet()
    }
    
    /**
     * Checks if a test class matches any of the include patterns.
     */
    private fun matchesAnyPattern(testClass: String, patterns: Set<String>): Boolean {
        return patterns.any { pattern ->
            matchesPattern(testClass, pattern)
        }
    }
    
    /**
     * Checks if a test class matches a specific pattern.
     * Supports wildcard patterns like "*Test", "*Spec", etc.
     */
    internal fun matchesPattern(testClass: String, pattern: String): Boolean {
        // Convert the pattern to a regex
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        
        return testClass.matches(Regex(regexPattern))
    }
    
    /**
     * Reports the results of the test inclusion check.
     */
    private fun reportResults(
        allTestClasses: Set<String>,
        excludedClasses: Set<String>,
        includePatterns: Set<String>
    ) {
        println("\n========== Test Inclusion Check Results ==========")
        println("Include patterns: ${includePatterns.joinToString(", ")}")
        println("Total test classes found: ${allTestClasses.size}")
        println("Test classes matching patterns: ${allTestClasses.size - excludedClasses.size}")
        println("Test classes NOT matching patterns: ${excludedClasses.size}")
        
        if (excludedClasses.isNotEmpty()) {
            println("\nThe following test classes don't match any includeTestsMatching patterns:")
            excludedClasses.sorted().forEach { className ->
                println("  ❌ $className")
            }
            println("\nThese tests will NOT be executed!")
            println("Consider updating your includeTestsMatching patterns or renaming the test classes.")
            println("==================================================\n")
            
            throw GradleException(
                "${excludedClasses.size} test classes don't match includeTestsMatching patterns and won't be executed."
            )
        } else {
            println("\n✅ All test classes match the include patterns and will be executed.")
            println("==================================================\n")
        }
    }
}