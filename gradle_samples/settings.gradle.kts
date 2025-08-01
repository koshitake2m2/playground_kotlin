plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "gradle_samples"
include("kotlin", "ktor")

project(":kotlin").projectDir = file("kotlin")
project(":ktor").projectDir = file("ktor")
