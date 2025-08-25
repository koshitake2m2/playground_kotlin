package com.example.kotlinx

import kotlinx.coroutines.*
import java.io.File

object CoroutineScopeDispatchersIOLaunch {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {

            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)

                // This is not executed because runBlocking is finished before.
                val file = File("tmp/output.txt")
                file.parentFile?.mkdirs()
                file.writeText("Hello, Kotlin!\nThis is a file output example.")
                println("hello async")
            }
//            delay(3000)
            println("hello")
        }
    }
}