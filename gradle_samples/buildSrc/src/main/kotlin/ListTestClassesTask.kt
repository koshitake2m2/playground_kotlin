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
            .toList()
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