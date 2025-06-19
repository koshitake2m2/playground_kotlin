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
class HelloTestClass : DescribeSpec({
    describe("hello") {
        it("should return hello world") {
            val result = "Hello, World!"
            result shouldBe "Hello, World!"
        }
    }
})

class GreetTestClass : DescribeSpec({
    describe("greet") {
        it("should return greeting message") {
            val result = "Greetings, Universe!"
            result shouldBe "Greetings, Universe!"
        }
    }
})