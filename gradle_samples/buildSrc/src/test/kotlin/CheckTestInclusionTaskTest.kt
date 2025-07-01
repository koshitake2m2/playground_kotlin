import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CheckTestInclusionTaskTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var task: CheckTestInclusionTask
    
    @BeforeEach
    fun setup() {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("testTask", CheckTestInclusionTask::class.java)
    }
    
    @Test
    fun `matchesPattern with exact match`() {
        assertThat(task.matchesPattern("com.example.UserTest", "com.example.UserTest")).isTrue()
        assertThat(task.matchesPattern("com.example.UserTest", "com.example.OtherTest")).isFalse()
    }
    
    @Test
    fun `matchesPattern with wildcard at end`() {
        assertThat(task.matchesPattern("com.example.UserTest", "*Test")).isTrue()
        assertThat(task.matchesPattern("com.example.UserTests", "*Test")).isFalse()
        assertThat(task.matchesPattern("com.example.TestUser", "*Test")).isFalse()
    }
    
    @Test
    fun `matchesPattern with wildcard at beginning`() {
        assertThat(task.matchesPattern("com.example.UserTest", "com.example.*")).isTrue()
        assertThat(task.matchesPattern("org.sample.UserTest", "com.example.*")).isFalse()
    }
    
    @Test
    fun `matchesPattern with multiple wildcards`() {
        assertThat(task.matchesPattern("com.example.UserTest", "*.example.*Test")).isTrue()
        assertThat(task.matchesPattern("com.example.UserSpec", "*.example.*Test")).isFalse()
        assertThat(task.matchesPattern("org.example.UserTest", "*.example.*Test")).isTrue()
    }
    
    @Test
    fun `matchesPattern with question mark`() {
        assertThat(task.matchesPattern("com.example.UserTest", "com.example.User?est")).isTrue()
        assertThat(task.matchesPattern("com.example.UserBest", "com.example.User?est")).isTrue()
        assertThat(task.matchesPattern("com.example.UserXYest", "com.example.User?est")).isFalse()
    }
    
    @Test
    fun `findExcludedTestClasses with no exclusions`() {
        val allTestClasses = setOf(
            "com.example.UserTest",
            "com.example.ServiceTest",
            "com.example.RepositoryTest"
        )
        val includePatterns = setOf("*Test")
        
        val excluded = task.findExcludedTestClasses(allTestClasses, includePatterns)
        
        assertThat(excluded).isEmpty()
    }
    
    @Test
    fun `findExcludedTestClasses with some exclusions`() {
        val allTestClasses = setOf(
            "com.example.UserTest",
            "com.example.ServiceSpec",
            "com.example.RepositoryTest",
            "com.example.ControllerSpec"
        )
        val includePatterns = setOf("*Test")
        
        val excluded = task.findExcludedTestClasses(allTestClasses, includePatterns)
        
        assertThat(excluded).containsExactlyInAnyOrder(
            "com.example.ServiceSpec",
            "com.example.ControllerSpec"
        )
    }
    
    @Test
    fun `findExcludedTestClasses with multiple patterns`() {
        val allTestClasses = setOf(
            "com.example.UserTest",
            "com.example.ServiceSpec",
            "com.example.RepositoryTest",
            "com.example.ItShould",
            "com.example.DataTestClass"
        )
        val includePatterns = setOf("*Test", "*Spec")
        
        val excluded = task.findExcludedTestClasses(allTestClasses, includePatterns)
        
        assertThat(excluded).containsExactlyInAnyOrder(
            "com.example.ItShould",
            "com.example.DataTestClass"
        )
    }
    
    @Test
    fun `findExcludedTestClasses with package-specific patterns`() {
        val allTestClasses = setOf(
            "com.example.UserTest",
            "com.example.integration.DatabaseTest",
            "com.example.unit.ServiceTest",
            "org.sample.HelperTest"
        )
        val includePatterns = setOf("com.example.*Test")
        
        val excluded = task.findExcludedTestClasses(allTestClasses, includePatterns)
        
        assertThat(excluded).containsExactly("org.sample.HelperTest")
    }
    
    @Test
    fun `checkTestInclusion succeeds when all tests match patterns`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        
        // Create and configure test task
        val testTask = project.tasks.create("test", org.gradle.api.tasks.testing.Test::class.java)
        testTask.filter.includeTestsMatching("*Test")
        
        val checkTask = project.tasks.create("checkTestInclusion", CheckTestInclusionTask::class.java)
        
        // Create test classes directory but without any test classes
        val testClassesDir = File(tempDir, "build/classes/kotlin/test")
        testClassesDir.mkdirs()
        
        // Should not throw since there are no test classes to check
        checkTask.checkTestInclusion()
    }
    
    @Test
    fun `checkTestInclusion fails when some tests don't match patterns`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        
        // Create and configure test task
        val testTask = project.tasks.create("test", org.gradle.api.tasks.testing.Test::class.java)
        testTask.filter.includeTestsMatching("*Test")
        
        val checkTask = project.tasks.create("checkTestInclusion", CheckTestInclusionTask::class.java)
        
        // Create test classes directory with a test class that doesn't match the pattern
        val testClassesDir = File(tempDir, "build/classes/kotlin/test")
        createTestClassWithJUnitAnnotation(testClassesDir, "com/example/ServiceSpec.class")
        
        assertThatThrownBy {
            checkTask.checkTestInclusion()
        }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("1 test classes don't match includeTestsMatching patterns")
    }
    
    /**
     * Creates a simple class file with JUnit @Test annotation in the bytecode.
     */
    private fun createTestClassWithJUnitAnnotation(baseDir: File, relativePath: String) {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        
        // Create a simple bytecode that contains the JUnit @Test annotation signature
        val bytecode = createSimpleClassBytecodeWithTestAnnotation()
        file.writeBytes(bytecode)
    }
    
    /**
     * Creates minimal bytecode for a class that contains JUnit @Test annotation references.
     * This is a simplified bytecode that just contains the necessary constant pool entries.
     */
    private fun createSimpleClassBytecodeWithTestAnnotation(): ByteArray {
        // This is a minimal Java class file with @Test annotation reference in constant pool
        // The actual bytecode structure is simplified but contains the key string we're looking for
        val constantPoolEntry = "org/junit/jupiter/api/Test".toByteArray(Charsets.UTF_8)
        
        // Create a minimal class file structure
        val header = byteArrayOf(
            0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte(), // Magic number
            0x00, 0x00, 0x00, 0x3E // Minor and major version
        )
        
        // Simple constant pool with our annotation
        val constantPool = byteArrayOf(0x00, 0x03) + // Constant pool count
                byteArrayOf(0x01) + // UTF8 constant
                byteArrayOf(0x00, constantPoolEntry.size.toByte()) + // Length
                constantPoolEntry + // The annotation string
                byteArrayOf(0x07, 0x00, 0x01) // Class reference
        
        // Minimal class info
        val classInfo = byteArrayOf(
            0x00, 0x21, // Access flags (public)
            0x00, 0x02, // This class
            0x00, 0x00, // Super class (none)
            0x00, 0x00, // Interfaces count
            0x00, 0x00, // Fields count
            0x00, 0x00, // Methods count
            0x00, 0x00  // Attributes count
        )
        
        return header + constantPool + classInfo
    }
}