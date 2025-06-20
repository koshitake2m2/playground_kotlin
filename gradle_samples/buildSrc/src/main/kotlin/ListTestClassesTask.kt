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
        
        println("\n========== Test Classes (from compiled classes) ==========")
        
        var count = 0
        
        if (testClassesDir.exists()) {
            testClassesDir.walkTopDown()
                .filter { it.isFile && it.extension == "class" && !it.name.contains("$") }
                .forEach { classFile ->
                    val relativePath = classFile.relativeTo(testClassesDir).path
                    val className = relativePath
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
                    
                    count++
                    println("  $count. $className")
                }
        } else {
            println("  No test classes found (directory does not exist)")
        }
        
        println("\nTotal test classes found: $count")
        println("==========================================================")
    }
}