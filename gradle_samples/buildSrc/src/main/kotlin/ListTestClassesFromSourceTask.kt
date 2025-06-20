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
        val classPattern = """class\s+(\w+)\s*(?::\s*[\w<>,\s()]+)?\s*\{""".toRegex()
        val matches = classPattern.findAll(content)
        
        matches.forEach { match ->
            val className = match.groupValues[1]
            val fullClassName = if (packageName != null) {
                "$packageName.$className"
            } else {
                className
            }
            classes.add(fullClassName)
        }
        
        return classes
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