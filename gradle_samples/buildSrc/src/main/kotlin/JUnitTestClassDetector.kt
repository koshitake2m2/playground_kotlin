import java.io.File
import java.net.URLClassLoader

/**
 * Detects test classes by checking for JUnit test annotations using reflection.
 * This provides accurate detection of what JUnit actually considers as test classes.
 */
class JUnitTestClassDetector {
    
    /**
     * Discovers all test classes that contain JUnit test methods in the given directory.
     * 
     * @param testClassesDir The directory containing compiled test classes
     * @return Set of fully qualified class names that contain JUnit test methods
     */
    fun discoverJUnitTestClasses(testClassesDir: File): Set<String> {
        if (!testClassesDir.exists()) {
            return emptySet()
        }
        
        return try {
            // Find all compiled class files
            val allClassFiles = findAllClassFiles(testClassesDir)
            val discoveredTestClasses = mutableSetOf<String>()
            
            // Check each class to see if it contains JUnit test methods
            for (className in allClassFiles) {
                if (hasJUnitTestMethods(className, testClassesDir)) {
                    discoveredTestClasses.add(className)
                }
            }
            
            discoveredTestClasses
        } catch (e: Exception) {
            println("Warning: Failed to discover JUnit test classes: ${e.message}")
            emptySet()
        }
    }
    
    /**
     * Checks if a class contains methods with JUnit test annotations by analyzing the bytecode.
     */
    private fun hasJUnitTestMethods(className: String, testClassesDir: File): Boolean {
        return try {
            // Find the class file
            val classFile = File(testClassesDir, className.replace('.', File.separatorChar) + ".class")
            if (!classFile.exists()) {
                return false
            }
            
            // Read the class file and look for @Test annotations in the constant pool
            val bytes = classFile.readBytes()
            val content = String(bytes, Charsets.ISO_8859_1)
            
            // Look for JUnit test annotations in the bytecode
            content.contains("org/junit/jupiter/api/Test") || 
            content.contains("org/junit/Test") ||
            content.contains("Lorg/junit/jupiter/api/Test;") ||
            content.contains("Lorg/junit/Test;")
        } catch (e: Exception) {
            // If we can't analyze the class file, assume it's not a test class
            false
        }
    }
    
    /**
     * Finds all class files in the directory and converts them to fully qualified class names.
     */
    private fun findAllClassFiles(testClassesDir: File): Set<String> {
        return testClassesDir.walkTopDown()
            .filter { it.isFile && it.extension == "class" && !it.name.contains("$") }
            .map { classFile ->
                val relativePath = classFile.relativeTo(testClassesDir).path
                relativePath
                    .removeSuffix(".class")
                    .replace(File.separatorChar, '.')
            }
            .toSet()
    }
}