import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CheckTestFileNamingTaskTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var task: CheckTestFileNamingTask
    
    @BeforeEach
    fun setup() {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("testTask", CheckTestFileNamingTask::class.java)
    }
    
    @Test
    fun `extractClassesFromFile extracts regular classes`() {
        val content = """
            package com.example
            
            class UserTest {
                fun test() {}
            }
            
            class Helper {
                fun help() {}
            }
        """.trimIndent()
        
        val result = task.extractClassesFromFile(content)
        
        assertThat(result).hasSize(2)
        assertThat(result.map { it.className }).containsExactlyInAnyOrder("UserTest", "Helper")
        assertThat(result.filter { it.isDataClass }).isEmpty()
    }
    
    @Test
    fun `extractClassesFromFile extracts data classes`() {
        val content = """
            package com.example
            
            class UserTest {
                fun test() {}
            }
            
            data class TestData(val id: String)
            
            data class UserData(
                val name: String,
                val age: Int
            )
        """.trimIndent()
        
        val result = task.extractClassesFromFile(content)
        
        assertThat(result).hasSize(3)
        assertThat(result.filter { it.isDataClass }.map { it.className })
            .containsExactlyInAnyOrder("TestData", "UserData")
        assertThat(result.filter { !it.isDataClass }.map { it.className })
            .containsExactly("UserTest")
    }
    
    @Test
    fun `validateTestFile passes for valid single test class file`() {
        val file = createTestFile("UserTest.kt", """
            package com.example
            
            class UserTest {
                @Test
                fun testUser() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `validateTestFile passes for test class with data classes`() {
        val file = createTestFile("UserTest.kt", """
            package com.example
            
            class UserTest {
                @Test
                fun testUser() {}
            }
            
            data class TestData(val id: String)
            
            class Helper {
                fun help() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `validateTestFile fails for multiple test classes in one file`() {
        val file = createTestFile("MultiTest.kt", """
            package com.example
            
            class FirstTest {
                @Test
                fun test1() {}
            }
            
            class SecondTest {
                @Test
                fun test2() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        // Should have violations for multiple test classes and file name mismatches
        assertThat(violations).hasSizeGreaterThanOrEqualTo(1)
        val multipleClassViolation = violations.find { it.type == CheckTestFileNamingTask.ViolationType.MULTIPLE_TEST_CLASSES }
        assertThat(multipleClassViolation).isNotNull
        assertThat(multipleClassViolation!!.message).contains("File contains 2 test classes")
    }
    
    @Test
    fun `validateTestFile fails for file-class name mismatch`() {
        val file = createTestFile("WrongFileName.kt", """
            package com.example
            
            class UserTest {
                @Test
                fun testUser() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        assertThat(violations).hasSize(1)
        assertThat(violations[0].type).isEqualTo(CheckTestFileNamingTask.ViolationType.FILE_CLASS_NAME_MISMATCH)
        assertThat(violations[0].message).contains("Test class name 'UserTest' does not match file name 'WrongFileName'")
    }
    
    @Test
    fun `validateTestFile ignores classes that don't end with Test or Spec`() {
        val file = createTestFile("UserChecker.kt", """
            package com.example
            
            class UserChecker {
                @Test
                fun checkUser() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        // UserChecker is not considered a test class since it doesn't end with Test or Spec
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `validateTestFile handles Spec suffix correctly`() {
        val file = createTestFile("UserSpec.kt", """
            package com.example
            
            class UserSpec {
                @Test
                fun testUser() {}
            }
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `validateTestFile ignores non-test classes`() {
        val file = createTestFile("Helper.kt", """
            package com.example
            
            class Helper {
                fun help() {}
            }
            
            data class Config(val value: String)
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        assertThat(violations).isEmpty()
    }
    
    @Test
    fun `validateTestFile handles complex violations`() {
        val file = createTestFile("ComplexTest.kt", """
            package com.example
            
            class WrongNameChecker {
                @Test
                fun test1() {}
            }
            
            class AnotherTest {
                @Test
                fun test2() {}
            }
            
            data class TestData(val id: String)
        """.trimIndent())
        
        val violations = task.validateTestFile(file)
        
        // Only AnotherTest is considered a test class (ends with Test)
        // WrongNameChecker is ignored since it doesn't end with Test or Spec
        // Should have 1 violation: File-class name mismatch for AnotherTest
        assertThat(violations).hasSize(1)
        assertThat(violations[0].type).isEqualTo(CheckTestFileNamingTask.ViolationType.FILE_CLASS_NAME_MISMATCH)
        assertThat(violations[0].message).contains("Test class name 'AnotherTest' does not match file name 'ComplexTest'")
    }
    
    @Test
    fun `checkTestFileNaming succeeds with valid project structure`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        
        // Set up source sets
        project.pluginManager.apply("java")
        
        // Ensure the test source set directory is configured
        val testSrcDir = File(tempDir, "src/test/kotlin")
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val testSourceSet = sourceSets.getByName("test")
        testSourceSet.java.srcDir(testSrcDir)
        
        createTestFile(testSrcDir, "com/example/UserTest.kt", """
            package com.example
            
            class UserTest {
                @Test
                fun testUser() {}
            }
        """.trimIndent())
        
        createTestFile(testSrcDir, "com/example/ServiceSpec.kt", """
            package com.example
            
            class ServiceSpec {
                @Test
                fun testService() {}
            }
        """.trimIndent())
        
        val checkTask = project.tasks.create("checkTestFileNaming", CheckTestFileNamingTask::class.java)
        
        // Should not throw
        checkTask.checkTestFileNaming()
    }
    
    @Test
    fun `checkTestFileNaming fails with invalid project structure`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
        
        // Set up source sets
        project.pluginManager.apply("java")
        
        // Ensure the test source set directory is configured
        val testSrcDir = File(tempDir, "src/test/kotlin")
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val testSourceSet = sourceSets.getByName("test")
        testSourceSet.java.srcDir(testSrcDir)
        
        createTestFile(testSrcDir, "com/example/WrongName.kt", """
            package com.example
            
            class UserTest {
                @Test
                fun testUser() {}
            }
        """.trimIndent())
        
        val checkTask = project.tasks.create("checkTestFileNaming", CheckTestFileNamingTask::class.java)
        
        assertThatThrownBy {
            checkTask.checkTestFileNaming()
        }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("test file naming convention violations found")
    }
    
    private fun createTestFile(fileName: String, content: String): File {
        return createTestFile(tempDir, fileName, content)
    }
    
    private fun createTestFile(baseDir: File, fileName: String, content: String): File {
        val file = File(baseDir, fileName)
        file.parentFile.mkdirs()
        file.writeText(content)
        return file
    }
}