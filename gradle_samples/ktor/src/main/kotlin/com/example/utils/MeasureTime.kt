package com.example.utils

suspend fun measureTime(block: suspend () -> Unit): Long {
    val startTime = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - startTime
}