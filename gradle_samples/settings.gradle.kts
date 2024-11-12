
plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "gradle_samples"
include("app", "list", "utilities", "ktor")

project(":app").projectDir = file("app")
project(":ktor").projectDir = file("ktor")
