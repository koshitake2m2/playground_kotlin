import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class ListTestClassesFromCompiledTask : DefaultTask() {
    init {
        group = "verification"
        description = "Lists test classes from compiled classes"
    }
    
    @TaskAction
    fun listTestClasses() {
        val testClassesDir = project.layout.buildDirectory.dir("classes/kotlin/test").get().asFile
        val testClasses = findTestClassesFromCompiledFiles(testClassesDir)
        
        printTestClasses(testClasses)
    }
    
    /**
     * Finds test classes from compiled .class files in the given directory.
     * This method is separated for testability.
     */
    internal fun findTestClassesFromCompiledFiles(testClassesDir: File): List<String> {
        if (!testClassesDir.exists()) {
            return emptyList()
        }
        
        return testClassesDir.walkTopDown()
            .filter { it.isFile && it.extension == "class" && !it.name.contains("$") }
            .map { classFile ->
                val relativePath = classFile.relativeTo(testClassesDir).path
                relativePath
                    .removeSuffix(".class")
                    .replace(File.separatorChar, '.')
            }
            .filter { className ->
                isTestClass(className)
            }
            .toList()
    }
    
    /**
     * Determines if a class is likely a test class based on its name
     */
    private fun isTestClass(className: String): Boolean {
        val simpleClassName = className.substringAfterLast('.')
        
        // Check if class name indicates it's a test
        // Note: We check for exact suffixes to avoid matching classes like "SampleTestData"
        val testNamePatterns = listOf("Test", "Tests", "Spec", "Should", "TestClass")
        return testNamePatterns.any { pattern -> 
            simpleClassName.endsWith(pattern) && 
            // Exclude cases where "Test" is followed by other words like "TestData"
            (pattern != "Test" || !simpleClassName.endsWith("TestData"))
        }
    }
    
    private fun printTestClasses(testClasses: List<String>) {
        println("\n========== Test Classes (from compiled classes) ==========")
        
        if (testClasses.isEmpty()) {
            println("  No test classes found")
        } else {
            testClasses.forEachIndexed { index, className ->
                println("  ${index + 1}. $className")
            }
        }
        
        println("\nTotal test classes found: ${testClasses.size}")
        println("==========================================================")
    }
}