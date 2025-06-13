package com.example.ktor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserJsonModule: SimpleModule() {
    init {
        addSerializer(UserName::class.java, UserNameSerializer())
        addDeserializer(UserName::class.java, UserNameDeserializer())
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
        addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer())
    }
}

class UserNameSerializer : JsonSerializer<UserName>() {
    override fun serialize(value: UserName, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.value)
    }
}

class UserNameDeserializer : JsonDeserializer<UserName>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserName {
        return UserName(p.text)
    }
}

class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(formatter.format(value))
    }
}

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
        return LocalDateTime.parse(p.text, formatter)
    }
}