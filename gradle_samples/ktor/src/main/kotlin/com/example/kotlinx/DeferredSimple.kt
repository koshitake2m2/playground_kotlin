package com.example.kotlinx

import com.example.utils.measureTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object DeferredSimple {
    fun expensiveComputation(i: Int): Int {
        println("Starting expensive computation for $i")
        Thread.sleep(1000L)
        return i * i
    }

    suspend fun expensiveRepeat(size: Int): Unit {
        coroutineScope {
            val tasks = List(size) { i ->
                async(Dispatchers.Default) {
                    expensiveComputation(i + 1)
                }
            }
            val results = tasks.awaitAll()
            println("Results: $results")
        }
    }

    suspend fun expensiveRepeatWithFlow(size: Int): Unit {
        coroutineScope {
            flow { repeat(size) { emit(it) } }
                .map { i ->
                    async(Dispatchers.Default) {
                        expensiveComputation(i + 1)
//                        if (i % 10 == 0) {
//                            // Simulate an error. This will cause the flow to fail.
//                            throw RuntimeException("Error at $i")
//                        }
                    }
                }
                .toList()
                .awaitAll()
                .also { results -> println("Results: $results") }
        }
    }
}

// Maybe about 10 suspended functions can run concurrently.
fun main() {
    runBlocking {
        val t = measureTime {
            DeferredSimple.expensiveRepeat(64)
        }
        println("Total time: $t ms") // about 7s

        val t2 = measureTime {
            DeferredSimple.expensiveRepeatWithFlow(64)
        }
        println("Total time: $t2 ms") // about 7s
    }
}