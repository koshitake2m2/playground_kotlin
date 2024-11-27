package com.example.ktor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserNameSerializerTest {

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper().apply {
            val module = SimpleModule()
            module.addSerializer(UserName::class.java, UserNameSerializer())
            registerModule(module)
        }
    }

    @Test
    fun `should serialize UserName to JSON string`() {
        val userName = UserName("Bob")
        val actual = objectMapper.writeValueAsString(userName)
        assertEquals("\"Bob\"", actual)
    }

    @Test
    fun `should serialize empty UserName to empty JSON string`() {
        val userName = UserName("")
        val actual = objectMapper.writeValueAsString(userName)
        assertEquals("\"\"", actual)
    }
}
