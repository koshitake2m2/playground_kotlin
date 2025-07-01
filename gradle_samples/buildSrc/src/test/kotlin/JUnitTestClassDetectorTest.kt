import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class JUnitTestClassDetectorTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var detector: JUnitTestClassDetector
    
    @BeforeEach
    fun setup() {
        detector = JUnitTestClassDetector()
    }
    
    @Test
    fun `discoverJUnitTestClasses returns empty set when directory does not exist`() {
        val nonExistentDir = File(tempDir, "non-existent")
        
        val result = detector.discoverJUnitTestClasses(nonExistentDir)
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `discoverJUnitTestClasses returns empty set when directory is empty`() {
        val emptyDir = File(tempDir, "empty")
        emptyDir.mkdirs()
        
        val result = detector.discoverJUnitTestClasses(emptyDir)
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `discoverJUnitTestClasses finds classes with JUnit 5 Test annotation`() {
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/UserTest.class")
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/ServiceTest.class")
        createClassWithoutTestAnnotation(tempDir, "com/example/Helper.class")
        
        val result = detector.discoverJUnitTestClasses(tempDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.UserTest",
            "com.example.ServiceTest"
        )
    }
    
    @Test
    fun `discoverJUnitTestClasses finds classes with JUnit 4 Test annotation`() {
        createClassWithJUnit4TestAnnotation(tempDir, "com/example/LegacyTest.class")
        createClassWithoutTestAnnotation(tempDir, "com/example/Helper.class")
        
        val result = detector.discoverJUnitTestClasses(tempDir)
        
        assertThat(result).containsExactly("com.example.LegacyTest")
    }
    
    @Test
    fun `discoverJUnitTestClasses ignores inner classes`() {
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/OuterTest.class")
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/OuterTest\$InnerTest.class")
        
        val result = detector.discoverJUnitTestClasses(tempDir)
        
        assertThat(result).containsExactly("com.example.OuterTest")
    }
    
    @Test
    fun `discoverJUnitTestClasses ignores non-class files`() {
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/UserTest.class")
        createFile(tempDir, "com/example/README.txt", "readme content")
        createFile(tempDir, "com/example/config.properties", "key=value")
        
        val result = detector.discoverJUnitTestClasses(tempDir)
        
        assertThat(result).containsExactly("com.example.UserTest")
    }
    
    @Test
    fun `discoverJUnitTestClasses handles deeply nested packages`() {
        createClassWithJUnit5TestAnnotation(tempDir, "com/example/deep/nested/package/DeepTest.class")
        createClassWithJUnit5TestAnnotation(tempDir, "SimpleTest.class")
        
        val result = detector.discoverJUnitTestClasses(tempDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.deep.nested.package.DeepTest",
            "SimpleTest"
        )
    }
    
    private fun createClassWithJUnit5TestAnnotation(baseDir: File, relativePath: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        
        // Create bytecode that contains JUnit 5 @Test annotation signature
        val content = "dummy bytecode content with org/junit/jupiter/api/Test annotation"
        file.writeText(content, Charsets.ISO_8859_1)
    }
    
    private fun createClassWithJUnit4TestAnnotation(baseDir: File, relativePath: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        
        // Create bytecode that contains JUnit 4 @Test annotation signature
        val content = "dummy bytecode content with org/junit/Test annotation"
        file.writeText(content, Charsets.ISO_8859_1)
    }
    
    private fun createClassWithoutTestAnnotation(baseDir: File, relativePath: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        
        // Create bytecode without any test annotations
        val content = "dummy bytecode content without test annotations"
        file.writeText(content, Charsets.ISO_8859_1)
    }
    
    private fun createFile(baseDir: File, relativePath: String, content: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}