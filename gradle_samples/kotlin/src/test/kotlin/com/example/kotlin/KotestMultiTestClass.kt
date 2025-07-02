package com.example.kotlin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * This doesn't work.
 *
 * ```bash
 * ./gradlew :kotlin:test --rerun-tasks --tests com.example.kotlin.KotestMultiTestClass
 * ```
 */
class KotestHelloTestSpec : DescribeSpec({
    describe("hello") {
        it("should return hello world") {
            val result = "Hello, World!"
            result shouldBe "Hello, World!"
        }
    }
})

class KotestGreetTestSpec : DescribeSpec({
    describe("greet") {
        it("should return greeting message") {
            val result = "Greetings, Universe!"
            result shouldBe "Greetings, Universe!"
        }
        it("sample test data") {
            val sampleData = KotestSampleData("Alice", 30)
            sampleData.name shouldBe "Alice"
            sampleData.age shouldBe 30
        }
    }
})

// This is not executed.
// If you comment out this, test will throw errors.
//class KotestWorldData : DescribeSpec({
//    describe("world") {
//        it("should return world message") {
//            val result = "World is beautiful!"
//            result shouldBe "World is beautiful!"
//        }
//    }
//})

class KotestSampleData(
    val name: String,
    val age: Int
)