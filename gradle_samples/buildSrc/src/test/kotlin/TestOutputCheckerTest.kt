import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TestOutputCheckerTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private lateinit var checker: TestOutputChecker
    
    @BeforeEach
    fun setup() {
        checker = TestOutputChecker()
    }
    
    @Test
    fun `containsPreconditionViolation returns true when PreconditionViolationException is present`() {
        val content = """
            Test started
            org.junit.platform.commons.PreconditionViolationException: Test method must not be static
            at org.junit.platform.commons.util.Preconditions.condition(Preconditions.java:296)
            Test failed
        """.trimIndent()
        
        val result = checker.containsPreconditionViolation(content)
        
        assertThat(result).isTrue()
    }
    
    @Test
    fun `containsPreconditionViolation returns false when PreconditionViolationException is not present`() {
        val content = """
            Test started
            All tests passed successfully
            Test completed
        """.trimIndent()
        
        val result = checker.containsPreconditionViolation(content)
        
        assertThat(result).isFalse()
    }
    
    @Test
    fun `containsPreconditionViolation returns false for empty content`() {
        val result = checker.containsPreconditionViolation("")
        
        assertThat(result).isFalse()
    }
    
    @Test
    fun `checkForPreconditionViolation throws GradleException when exception is detected`() {
        val testFile = File(tempDir, "test-output.log")
        testFile.writeText("""
            Running test suite...
            ERROR: org.junit.platform.commons.PreconditionViolationException: Configuration error
            Test execution failed
        """.trimIndent())
        
        assertThatThrownBy {
            checker.checkForPreconditionViolation(testFile)
        }
            .isInstanceOf(GradleException::class.java)
            .hasMessage("PreconditionViolationException detected in test results.")
    }
    
    @Test
    fun `checkForPreconditionViolation does not throw when no exception is detected`() {
        val testFile = File(tempDir, "test-output.log")
        testFile.writeText("""
            Running test suite...
            All tests passed
            Build successful
        """.trimIndent())
        
        // Should not throw
        checker.checkForPreconditionViolation(testFile)
    }
    
    @Test
    fun `checkForPreconditionViolation handles non-existent file gracefully`() {
        val nonExistentFile = File(tempDir, "non-existent.log")
        
        // Should not throw
        checker.checkForPreconditionViolation(nonExistentFile)
    }
    
    @Test
    fun `checkForPreconditionViolation handles empty file`() {
        val emptyFile = File(tempDir, "empty.log")
        emptyFile.createNewFile()
        
        // Should not throw
        checker.checkForPreconditionViolation(emptyFile)
    }
    
    @Test
    fun `containsPreconditionViolation is case-sensitive`() {
        val content = "preconditionviolationexception" // lowercase
        
        val result = checker.containsPreconditionViolation(content)
        
        assertThat(result).isFalse()
    }
    
    @Test
    fun `containsPreconditionViolation detects exception in multiline stacktrace`() {
        val content = """
            2023-01-01 10:00:00 INFO Starting test execution
            2023-01-01 10:00:01 ERROR Test failed with exception:
            org.junit.platform.commons.PreconditionViolationException: 
                Configuration parameter [junit.jupiter.execution.parallel.enabled] must be set to 'true' or 'false'
                at org.junit.platform.commons.util.Preconditions.notNull(Preconditions.java:115)
                at org.junit.platform.commons.util.Preconditions.notBlank(Preconditions.java:159)
                at org.junit.jupiter.engine.config.DefaultJupiterConfiguration.<init>(DefaultJupiterConfiguration.java:67)
            2023-01-01 10:00:02 INFO Test execution completed
        """.trimIndent()
        
        val result = checker.containsPreconditionViolation(content)
        
        assertThat(result).isTrue()
    }
}