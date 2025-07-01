package com.example.kotlin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * This doesn't work.
 *
 * ```bash
 * ./gradlew :kotlin:test --rerun-tasks --tests com.example.kotlin.MultiTestClass
 * ```
 */
class HelloTestSpec : DescribeSpec({
    describe("hello") {
        it("should return hello world") {
            val result = "Hello, World!"
            result shouldBe "Hello, World!"
        }
    }
})

class GreetTestSpec : DescribeSpec({
    describe("greet") {
        it("should return greeting message") {
            val result = "Greetings, Universe!"
            result shouldBe "Greetings, Universe!"
        }
        it("sample test data") {
            val sampleData = SampleTestData("Alice", 30)
            sampleData.name shouldBe "Alice"
            sampleData.age shouldBe 30
        }
    }
})

// This is not executed.
// If you comment out this, test will throw errors.
//class WorldTestClass : DescribeSpec({
//    describe("world") {
//        it("should return world message") {
//            val result = "World is beautiful!"
//            result shouldBe "World is beautiful!"
//        }
//    }
//})

class SampleTestData(
    val name: String,
    val age: Int
)