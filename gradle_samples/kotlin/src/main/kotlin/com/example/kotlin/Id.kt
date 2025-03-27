package com.example.kotlin

@JvmInline
value class Id<T>(val value: String)

data class User(
    val id: Id<User>,
    val name: String
)

fun main() {
    val user = User(Id("1"), "John")
    println("result: ${user}")
}
