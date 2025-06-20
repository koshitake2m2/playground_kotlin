## playground kotlin

## Idea Setup

### Build

- Build, Execution, Deployment > Compiler > Kotlin Compiler > Kotlin to JVM > Target JVM version
- Build, Execution, Deployment > Compiler > Kotlin Compiler > Gradle Projects > Gradle JVM
- Project Structure > Project Settings > Project
    - SDK
    - Language level
- Project Structure > Project Settings > Modules
    - Add next projects if needed:
        - playground_kotlin
        - gradle_samples
        - pom_samples/kotlin
        - pom_samples/springboot

### Format

- Version Control > Confirmation
    - When files are created: `Add silently`
- Tools > Actions on Save
    - Reformat code: `Changed lines`
- Editor > Code Style > Kotlin
    - If needed...
