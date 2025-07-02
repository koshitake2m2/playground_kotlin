package com.example.kotlin

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


class JunitHelloTest {
    @Test
    fun `should return hello world`() {
        val result = "Hello, World!"
        result shouldBe "Hello, World!"
    }
}

class JunitHelloSpec {
    @Test
    fun `should return greeting message`() {
        val result = "Hello, Kotest!"
        result shouldBe "Hello, Kotest!"
    }
    @Test
    fun `should return simpleTestData`() {
        val sample = JunitSampleData("Alice", 30)
        sample.name shouldBe "Alice"
        sample.age shouldBe 30
    }
}

// This is not executed.
// If you comment out this, test will throw errors.
//class JunitWorldData {
//    @Test
//    fun `should return world message`() {
//        val result = "Hello, World!"
//        result shouldBe "Hello, World!"
//    }
//}

class JunitNestedTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    inner class JunitNestedHelloTest {
        @Test
        fun `should return nested hello world`() {
            val result = "Hello, Nested World!"
            result shouldBe "Hello, Nested World!"
        }
    }
}

class JunitSampleData(
    val name: String,
    val age: Int
)