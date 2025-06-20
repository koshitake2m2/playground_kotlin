import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

open class ListTestClassesFromSourceTask : DefaultTask() {
    init {
        group = "verification"
        description = "Lists all test classes in the module"
    }
    
    @TaskAction
    fun listTestClasses() {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val kotlinExtension = project.extensions.findByType(KotlinJvmProjectExtension::class.java)
        
        println("\n========== Test Classes in :${project.name} module ==========")
        
        var count = 0
        
        // Get test source files from Kotlin extension
        val testSourceSet = sourceSets.getByName("test")
        val testSrcDirs = testSourceSet.allSource.srcDirs
        
        testSrcDirs.forEach { srcDir ->
            if (srcDir.exists()) {
                srcDir.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .forEach { file ->
                        val content = file.readText()
                        // Match class declarations including those that extend other classes
                        val classPattern = """class\s+(\w+)\s*(?::\s*[\w<>,\s()]+)?\s*\{""".toRegex()
                        val matches = classPattern.findAll(content)
                        
                        matches.forEach { match ->
                            val className = match.groupValues[1]
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
            }
        }
        
        println("\nTotal test classes found: $count")
        println("==============================================================")
    }
}