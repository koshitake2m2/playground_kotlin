import org.gradle.api.GradleException
import java.io.File

/**
 * Checks test output for specific error conditions.
 */
open class TestOutputChecker {
    
    /**
     * Checks if the test output contains PreconditionViolationException.
     * 
     * @param testOutputFile The file containing test output
     * @throws GradleException if PreconditionViolationException is detected
     */
    fun checkForPreconditionViolation(testOutputFile: File) {
        if (!testOutputFile.exists()) {
            return
        }
        
        val content = testOutputFile.readText()
        if (containsPreconditionViolation(content)) {
            throw GradleException("PreconditionViolationException detected in test results.")
        }
    }
    
    /**
     * Checks if the given content contains PreconditionViolationException.
     * This method is separated for easier testing.
     * 
     * @param content The content to check
     * @return true if PreconditionViolationException is found, false otherwise
     */
    internal fun containsPreconditionViolation(content: String): Boolean {
        return content.contains("org.junit.platform.commons.PreconditionViolationException")
    }
}