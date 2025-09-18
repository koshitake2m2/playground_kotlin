package com.example.kotlin

object SystemMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val a: String = System.getenv("HELLO") ?: ""
        println(a)
        println(a.toBoolean())
    }
}