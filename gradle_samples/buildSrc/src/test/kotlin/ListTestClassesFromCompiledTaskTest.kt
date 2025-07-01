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
    
    @Test
    fun `findTestClassesFromCompiledFiles filters out non-test classes`() {
        val classesDir = File(tempDir, "classes")
        
        // Create test and non-test class files
        createClassFile(classesDir, "com/example/HelloTestClass.class")
        createClassFile(classesDir, "com/example/GreetTestClass.class")
        createClassFile(classesDir, "com/example/SampleTestData.class")
        createClassFile(classesDir, "com/example/TestConfig.class")
        createClassFile(classesDir, "com/example/Helper.class")
        createClassFile(classesDir, "com/example/UserService.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.HelloTestClass",
            "com.example.GreetTestClass"
        )
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles recognizes various test naming patterns`() {
        val classesDir = File(tempDir, "classes")
        
        // Create various test pattern class files
        createClassFile(classesDir, "com/example/UserTest.class")
        createClassFile(classesDir, "com/example/ServiceTests.class")
        createClassFile(classesDir, "com/example/BehaviorSpec.class")
        createClassFile(classesDir, "com/example/ItShould.class")
        createClassFile(classesDir, "com/example/RegularClass.class")
        createClassFile(classesDir, "com/example/DataClass.class")
        createClassFile(classesDir, "com/example/TestRunner.class") // Not a test class
        createClassFile(classesDir, "com/example/SpecialTest.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.UserTest",
            "com.example.ServiceTests",
            "com.example.BehaviorSpec",
            "com.example.ItShould",
            "com.example.SpecialTest"
        )
    }
    
    @Test
    fun `findTestClassesFromCompiledFiles excludes classes ending with TestData`() {
        val classesDir = File(tempDir, "classes")
        
        // Create test and non-test class files including TestData classes
        createClassFile(classesDir, "com/example/SampleTestData.class")
        createClassFile(classesDir, "com/example/UserTestData.class")
        createClassFile(classesDir, "com/example/ConfigTestData.class")
        createClassFile(classesDir, "com/example/ActualTest.class")
        createClassFile(classesDir, "com/example/TestDataProvider.class")
        createClassFile(classesDir, "com/example/MyTestData.class")
        
        val result = task.findTestClassesFromCompiledFiles(classesDir)
        
        // Should only find ActualTest, not the *TestData classes
        assertThat(result).containsExactly("com.example.ActualTest")
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