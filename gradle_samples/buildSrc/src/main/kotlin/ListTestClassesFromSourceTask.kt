import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import java.io.File

open class ListTestClassesFromSourceTask : DefaultTask() {
    init {
        group = "verification"
        description = "Lists all test classes in the module"
    }
    
    @TaskAction
    fun listTestClasses() {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val testSourceSet = sourceSets.getByName("test")
        val testSrcDirs = testSourceSet.allSource.srcDirs
        
        val testClasses = findTestClassesFromSourceFiles(testSrcDirs)
        printTestClasses(testClasses)
    }
    
    /**
     * Finds test classes from Kotlin source files in the given directories.
     * This method is separated for testability.
     */
    internal fun findTestClassesFromSourceFiles(srcDirs: Set<File>): List<String> {
        val testClasses = mutableListOf<String>()
        
        srcDirs.forEach { srcDir ->
            if (srcDir.exists()) {
                srcDir.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .forEach { file ->
                        testClasses.addAll(extractClassesFromFile(file))
                    }
            }
        }
        
        return testClasses
    }
    
    /**
     * Extracts class names from a Kotlin source file.
     * This method is separated for testability.
     */
    internal fun extractClassesFromFile(file: File): List<String> {
        val classes = mutableListOf<String>()
        val content = file.readText()
        
        // Extract package name
        val packageName = content.lines()
            .firstOrNull { it.startsWith("package ") }
            ?.removePrefix("package ")
            ?.trim()
        
        // Match class declarations including those that extend other classes
        // The pattern looks for "class ClassName" optionally followed by inheritance and then a brace
        val classPattern = """class\s+(\w+)\s*(?::\s*([\w<>,\s()]+))?""".toRegex()
        val matches = classPattern.findAll(content)
        
        matches.forEach { match ->
            val className = match.groupValues[1]
            val extendsClause = match.groupValues.getOrNull(2) ?: ""
            
            // Check if this is likely a test class
            if (isTestClass(className, extendsClause)) {
                val fullClassName = if (packageName != null) {
                    "$packageName.$className"
                } else {
                    className
                }
                classes.add(fullClassName)
            }
        }
        
        return classes
    }
    
    /**
     * Determines if a class is likely a test class based on its name and what it extends
     */
    private fun isTestClass(className: String, extendsClause: String): Boolean {
        // Check if class name indicates it's a test
        // Note: We check for exact suffixes to avoid matching classes like "SampleTestData"
        val testNamePatterns = listOf("Test", "Tests", "Spec", "Should", "TestClass")
        if (testNamePatterns.any { pattern -> 
            className.endsWith(pattern) && 
            // Exclude cases where "Test" is followed by other words like "TestData"
            (pattern != "Test" || !className.endsWith("TestData"))
        }) {
            return true
        }
        
        // Check if it extends a known test base class
        val testBaseClasses = listOf(
            "DescribeSpec", "FunSpec", "StringSpec", "ShouldSpec", "BehaviorSpec",
            "WordSpec", "FreeSpec", "FeatureSpec", "ExpectSpec", "AnnotationSpec"
        )
        if (testBaseClasses.any { extendsClause.contains(it) }) {
            return true
        }
        
        return false
    }
    
    private fun printTestClasses(testClasses: List<String>) {
        println("\n========== Test Classes in :${project.name} module ==========")
        
        if (testClasses.isEmpty()) {
            println("  No test classes found")
        } else {
            testClasses.forEachIndexed { index, className ->
                println("  ${index + 1}. $className")
            }
        }
        
        println("\nTotal test classes found: ${testClasses.size}")
        println("==============================================================")
    }
}