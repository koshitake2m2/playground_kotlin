package com.example.kotlinx

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun main() {
    runBlocking {
        val heavyList = listOf("A", "B", "C", "D", "E")

        println("Starting processing...")
        val elapsed = measureTimeMillis {
            val results = heavyList.map { item ->
                async(Dispatchers.Default) {
                    processHeavyTask(item)
                }
            }.awaitAll()

            println("Results: $results")
        }

        println("Completed in $elapsed ms")
    }
}

// 疑似的な重い処理
suspend fun processHeavyTask(input: String): String {
    println("Processing $input...")
    delay(3000L) // 1秒かかる処理
    return "Processed $input"
}