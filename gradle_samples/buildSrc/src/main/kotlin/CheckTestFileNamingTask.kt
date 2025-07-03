import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

/**
 * Task that validates test file naming conventions:
 * 1. File name must match the test class name
 * 2. Only one test class per file (data classes are allowed)
 * 3. Test class names must end with "Test" or "Spec"
 */
open class CheckTestFileNamingTask : DefaultTask() {
    init {
        group = "verification"
        description = "Validates test file and class naming conventions"
    }
    
    @TaskAction
    fun checkTestFileNaming() {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val testSourceSet = sourceSets.getByName("test")
        val testSrcDirs = testSourceSet.allSource.srcDirs
        
        val violations = mutableListOf<NamingViolation>()
        
        testSrcDirs.forEach { srcDir ->
            if (srcDir.exists()) {
                srcDir.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .forEach { file ->
                        violations.addAll(validateTestFile(file))
                    }
            }
        }
        
        reportResults(violations)
    }
    
    /**
     * Validates a single test file for naming conventions.
     */
    internal fun validateTestFile(file: File): List<NamingViolation> {
        val violations = mutableListOf<NamingViolation>()
        val content = file.readText()
        val fileName = file.nameWithoutExtension
        
        // Extract all classes from the file
        val classes = extractClassesFromFile(content)
        val testClasses = classes.filter { isTestClass(it.className) }
        
        // Rule 1: Only one test class per file
        if (testClasses.size > 1) {
            violations.add(
                NamingViolation(
                    file = file,
                    type = ViolationType.MULTIPLE_TEST_CLASSES,
                    message = "File contains ${testClasses.size} test classes: ${testClasses.map { it.className }.joinToString(", ")}. Only one test class per file is allowed."
                )
            )
        }
        
        // Rule 2: Test class name must match file name
        testClasses.forEach { testClass ->
            if (testClass.className != fileName) {
                violations.add(
                    NamingViolation(
                        file = file,
                        type = ViolationType.FILE_CLASS_NAME_MISMATCH,
                        message = "Test class name '${testClass.className}' does not match file name '$fileName'"
                    )
                )
            }
        }
        
        // Rule 3: Test class names must end with Test or Spec
        testClasses.forEach { testClass ->
            if (!testClass.className.endsWith("Test") && !testClass.className.endsWith("Spec")) {
                violations.add(
                    NamingViolation(
                        file = file,
                        type = ViolationType.INVALID_TEST_CLASS_SUFFIX,
                        message = "Test class '${testClass.className}' must end with 'Test' or 'Spec'"
                    )
                )
            }
        }
        
        return violations
    }
    
    /**
     * Extracts class information from Kotlin source file.
     */
    internal fun extractClassesFromFile(content: String): List<ClassInfo> {
        val classes = mutableListOf<ClassInfo>()
        
        // Match class declarations - both regular and data classes
        val classPattern = """(?:data\s+)?class\s+(\w+)""".toRegex()
        val dataClassPattern = """data\s+class\s+(\w+)""".toRegex()
        
        classPattern.findAll(content).forEach { match ->
            val className = match.groupValues[1]
            val isDataClass = dataClassPattern.find(match.value) != null
            
            classes.add(ClassInfo(className, isDataClass))
        }
        
        return classes
    }
    
    /**
     * Determines if a class is a test class based on naming patterns.
     * Only classes ending with "Test" or "Spec" are considered test classes.
     */
    private fun isTestClass(className: String): Boolean {
        return className.endsWith("Test") || className.endsWith("Spec")
    }
    
    /**
     * Reports the validation results.
     */
    private fun reportResults(violations: List<NamingViolation>) {
        println("\n========== Test File Naming Check Results ==========")
        
        if (violations.isEmpty()) {
            println("✅ All test files follow proper naming conventions.")
        } else {
            println("❌ Found ${violations.size} naming convention violation(s):")
            println()
            
            violations.groupBy { it.file }.forEach { (file, fileViolations) ->
                val relativePath = file.relativeTo(project.projectDir).path
                println("📁 $relativePath:")
                fileViolations.forEach { violation ->
                    println("   • ${violation.message}")
                }
                println()
            }
            
            println("Naming Convention Rules:")
            println("1. File name must match the test class name")
            println("2. Only one test class per file (data classes are allowed)")
            println("3. Test class names must end with 'Test' or 'Spec'")
        }
        
        println("==================================================\n")
        
        if (violations.isNotEmpty()) {
            throw GradleException("${violations.size} test file naming convention violations found.")
        }
    }
    
    /**
     * Represents information about a class found in a source file.
     */
    data class ClassInfo(
        val className: String,
        val isDataClass: Boolean
    )
    
    /**
     * Represents a naming convention violation.
     */
    data class NamingViolation(
        val file: File,
        val type: ViolationType,
        val message: String
    )
    
    /**
     * Types of naming violations.
     */
    enum class ViolationType {
        MULTIPLE_TEST_CLASSES,
        FILE_CLASS_NAME_MISMATCH,
        INVALID_TEST_CLASS_SUFFIX
    }
}