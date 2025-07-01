import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ListTestClassesFromSourceTaskTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var task: ListTestClassesFromSourceTask
    
    @BeforeEach
    fun setup() {
        val project = ProjectBuilder.builder().build()
        task = project.tasks.create("testTask", ListTestClassesFromSourceTask::class.java)
    }
    
    @Test
    fun `findTestClassesFromSourceFiles returns empty list when no directories exist`() {
        val nonExistentDirs = setOf(
            File(tempDir, "non-existent1"),
            File(tempDir, "non-existent2")
        )
        
        val result = task.findTestClassesFromSourceFiles(nonExistentDirs)
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `findTestClassesFromSourceFiles returns empty list when directories are empty`() {
        val emptyDir1 = File(tempDir, "empty1")
        val emptyDir2 = File(tempDir, "empty2")
        emptyDir1.mkdirs()
        emptyDir2.mkdirs()
        
        val result = task.findTestClassesFromSourceFiles(setOf(emptyDir1, emptyDir2))
        
        assertThat(result).isEmpty()
    }
    
    @Test
    fun `extractClassesFromFile extracts single class with package`() {
        val sourceFile = createKotlinFile(tempDir, "TestClass.kt", """
            package com.example
            
            class TestClass {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactly("com.example.TestClass")
    }
    
    @Test
    fun `extractClassesFromFile extracts multiple classes from single file`() {
        val sourceFile = createKotlinFile(tempDir, "MultipleClasses.kt", """
            package com.example
            
            class FirstTest {
                fun test() {}
            }
            
            class SecondTest {
                fun test() {}
            }
            
            class ThirdTest {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactly(
            "com.example.FirstTest",
            "com.example.SecondTest",
            "com.example.ThirdTest"
        )
    }
    
    @Test
    fun `extractClassesFromFile handles class without package`() {
        val sourceFile = createKotlinFile(tempDir, "NoPackage.kt", """
            class NoPackageTest {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactly("NoPackageTest")
    }
    
    @Test
    fun `extractClassesFromFile handles class extending other classes`() {
        val sourceFile = createKotlinFile(tempDir, "ExtendingClass.kt", """
            package com.example
            
            import io.kotest.core.spec.style.DescribeSpec
            
            class MyTest : DescribeSpec({
                describe("test") {
                    it("should work") {}
                }
            })
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactly("com.example.MyTest")
    }
    
    @Test
    fun `extractClassesFromFile ignores interfaces and objects`() {
        val sourceFile = createKotlinFile(tempDir, "MixedTypes.kt", """
            package com.example
            
            interface TestInterface {
                fun test()
            }
            
            object TestObject {
                fun test() {}
            }
            
            class ActualTest {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactly("com.example.ActualTest")
    }
    
    @Test
    fun `findTestClassesFromSourceFiles finds classes from multiple directories`() {
        val dir1 = File(tempDir, "src1")
        val dir2 = File(tempDir, "src2")
        
        createKotlinFile(dir1, "com/example/UserTest.kt", """
            package com.example
            
            class UserTest {
                fun test() {}
            }
        """.trimIndent())
        
        createKotlinFile(dir2, "org/sample/ServiceTest.kt", """
            package org.sample
            
            class ServiceTest {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.findTestClassesFromSourceFiles(setOf(dir1, dir2))
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.UserTest",
            "org.sample.ServiceTest"
        )
    }
    
    @Test
    fun `findTestClassesFromSourceFiles ignores non-kotlin files`() {
        val srcDir = File(tempDir, "src")
        
        createKotlinFile(srcDir, "TestClass.kt", """
            package com.example
            
            class TestClass {
                fun test() {}
            }
        """.trimIndent())
        
        createFile(srcDir, "README.md", "# README")
        createFile(srcDir, "config.properties", "key=value")
        createFile(srcDir, "Test.java", "public class Test {}")
        
        val result = task.findTestClassesFromSourceFiles(setOf(srcDir))
        
        assertThat(result).containsExactly("com.example.TestClass")
    }
    
    @Test
    fun `extractClassesFromFile filters out non-test classes like data classes`() {
        val sourceFile = createKotlinFile(tempDir, "MultipleClasses.kt", """
            package com.example.kotlin
            
            import io.kotest.core.spec.style.DescribeSpec
            
            class HelloTestClass : DescribeSpec({
                describe("hello") {
                    it("should work") {}
                }
            })
            
            class GreetTestClass : DescribeSpec({
                describe("greet") {
                    it("should work") {}
                }
            })
            
            class SampleTestData(
                val name: String,
                val age: Int
            )
            
            data class TestConfig(val url: String)
            
            class Helper {
                fun help() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.kotlin.HelloTestClass",
            "com.example.kotlin.GreetTestClass"
        )
    }
    
    @Test
    fun `extractClassesFromFile recognizes test classes by name patterns`() {
        val sourceFile = createKotlinFile(tempDir, "TestByName.kt", """
            package com.example
            
            class UserTest {
                fun test() {}
            }
            
            class ServiceTests {
                fun test() {}
            }
            
            class BehaviorSpec {
                fun test() {}
            }
            
            class ItShould {
                fun test() {}
            }
            
            class RegularClass {
                fun test() {}
            }
            
            class DataClass {
                fun test() {}
            }
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.UserTest",
            "com.example.ServiceTests",
            "com.example.BehaviorSpec",
            "com.example.ItShould"
        )
    }
    
    @Test
    fun `extractClassesFromFile recognizes test classes by inheritance`() {
        val sourceFile = createKotlinFile(tempDir, "TestByInheritance.kt", """
            package com.example
            
            import io.kotest.core.spec.style.*
            
            class MyDescribeTest : DescribeSpec({})
            class MyFunTest : FunSpec({})
            class MyStringTest : StringSpec({})
            class MyShouldTest : ShouldSpec({})
            class MyBehaviorTest : BehaviorSpec({})
            class MyWordTest : WordSpec({})
            class MyFreeTest : FreeSpec({})
            class MyFeatureTest : FeatureSpec({})
            class MyExpectTest : ExpectSpec({})
            class MyAnnotationTest : AnnotationSpec()
            
            class RegularClassExtendingCustom : CustomBase()
            class DataClassWithConstructor(val id: Int) : BaseEntity()
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        assertThat(result).containsExactlyInAnyOrder(
            "com.example.MyDescribeTest",
            "com.example.MyFunTest",
            "com.example.MyStringTest",
            "com.example.MyShouldTest",
            "com.example.MyBehaviorTest",
            "com.example.MyWordTest",
            "com.example.MyFreeTest",
            "com.example.MyFeatureTest",
            "com.example.MyExpectTest",
            "com.example.MyAnnotationTest"
        )
    }
    
    @Test
    fun `extractClassesFromFile excludes classes ending with TestData`() {
        val sourceFile = createKotlinFile(tempDir, "TestDataClasses.kt", """
            package com.example
            
            class SampleTestData(
                val name: String,
                val age: Int
            )
            
            class UserTestData {
                var id: Long = 0
                var name: String = ""
            }
            
            class ActualTest {
                fun test() {}
            }
            
            class TestDataProvider {
                fun provide() {}
            }
            
            class ConfigTestData(val url: String)
        """.trimIndent())
        
        val result = task.extractClassesFromFile(sourceFile)
        
        // Should only find ActualTest, not the *TestData classes
        assertThat(result).containsExactly("com.example.ActualTest")
    }
    
    private fun createKotlinFile(baseDir: File, relativePath: String, content: String): File {
        return createFile(baseDir, relativePath, content)
    }
    
    private fun createFile(baseDir: File, relativePath: String, content: String): File {
        val file = File(baseDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
        return file
    }
}