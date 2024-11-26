package com.example.ktor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule

class UserJsonModule: SimpleModule() {
    init {
        addSerializer(UserName::class.java, UserNameSerializer())
        addDeserializer(UserName::class.java, UserNameDeserializer())
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