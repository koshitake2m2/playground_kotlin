import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ListTestClassesFromCompiledTaskTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var task: ListTestClassesFromCompiledTask
    
    @BeforeEach
    fun setup() {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("testTask", ListTestClassesFromCompiledTask::class.java)
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles returns empty list when directory does not exist`() {
        val nonExistentDir = File(tempDir, "non-existent")
        
        val result = task.findTestClassesFromCompiledFiles(nonExistentDir)
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles returns empty list when directory is empty`() {
        val emptyDir = File(tempDir, "empty")
        emptyDir.mkdirs()
        
        val result = task.findTestClassesFromCompiledFiles(emptyDir)
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles finds class files and converts to class names`() {
        val classesDir = File(tempDir, "classes")
        
        // Create test class files
        createClassFile(classesDir, "com/example/TestClass.class")
        createClassFile(classesDir, "com/example/AnotherTest.class")
        createClassFile(classesDir, "org/sample/SampleTest.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.TestClass",
            "com.example.AnotherTest",
            "org.sample.SampleTest"
        )
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles ignores inner classes`() {
        val classesDir = File(tempDir, "classes")
        
        // Create test class files
        createClassFile(classesDir, "com/example/TestClass.class")
        createClassFile(classesDir, "com/example/TestClass\$InnerClass.class")
        createClassFile(classesDir, "com/example/TestClass\$1.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactly("com.example.TestClass")
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles ignores non-class files`() {
        val classesDir = File(tempDir, "classes")
        
        // Create mixed files
        createClassFile(classesDir, "com/example/TestClass.class")
        createFile(classesDir, "com/example/README.txt", "readme content")
        createFile(classesDir, "com/example/config.properties", "key=value")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactly("com.example.TestClass")
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles handles nested packages correctly`() {
        val classesDir = File(tempDir, "classes")
        
        // Create deeply nested class files
        createClassFile(classesDir, "com/example/deep/nested/package/DeepTest.class")
        createClassFile(classesDir, "SimpleTest.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.deep.nested.package.DeepTest",
            "SimpleTest"
        )
    }
    
    private fun createClassFile(baseDir: File, relativePath: String) {
        createFile(baseDir, relativePath, "dummy class content")
    }
    
    private fun createFile(baseDir: File, relativePath: String, content: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}