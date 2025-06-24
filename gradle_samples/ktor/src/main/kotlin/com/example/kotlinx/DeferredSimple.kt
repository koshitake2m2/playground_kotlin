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
            // Using mutableList to collect results instead of a list of deferred.
            // A list of deferred takes more memory.
            val results = mutableListOf<Int>()
            List(size) { i ->
                async(Dispatchers.Default) {
                    val r = expensiveComputation(i + 1)
                    results.add(r)
                }
            }.awaitAll()
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
object DeferredSimpleMain {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val t = measureTime {
                DeferredSimple.expensiveRepeat(64)
            }
            println("Total time: $t ms") // about 7s
        }
    }
}

object DeferredSimpleFlowMain {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            val t = measureTime {
                DeferredSimple.expensiveRepeatWithFlow(64)
            }
            println("Total time: $t ms") // about 7s
        }
    }
}
