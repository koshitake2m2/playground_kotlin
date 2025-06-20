package com.example.kotlinx

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object FlowSamples {

    fun transformedFlow(): Flow<String> = flow {
        emit(1)
        emit(2)
        emit(3)
    }.map { value ->
        "Number: $value"
    }.filter { 
        it.contains("2") || it.contains("3")
    }
    
    fun flowWithErrorHandling(): Flow<Int> = flow {
        emit(1)
        emit(2)
        check(false) { "Simulated error!" }
        emit(3) // This won't be emitted
    }.catch { e ->
        println("Caught exception: ${e.message}")
        emit(-1) // Emit error value
    }

}

suspend fun main() = coroutineScope {
    // Transformed flow
    println("\n=== Transformed Flow ===")
    FlowSamples.transformedFlow().collect { value ->
        println(value)
    }
    
    // Flow with error handling
    println("\n=== Flow with Error Handling ===")
    FlowSamples.flowWithErrorHandling().collect { value ->
        println("Value: $value")
    }

}