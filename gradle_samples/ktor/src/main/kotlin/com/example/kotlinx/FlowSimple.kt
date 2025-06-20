package com.example.kotlinx

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun simpleFlow(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(1000)
        emit(i)
    }
}

fun main() {
    println("Start")
    runBlocking {
        println("Start collecting")
        simpleFlow().collect { value ->
            println("Received: $value")
        }
    }
    println("Done")
}