package com.example.kotlinx

import com.example.utils.measureTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * Flow samples demonstrating asynchronous processing patterns
 */
object FlowAsync {
    
    /**
     * Simulates async work with random delay
     */
    private suspend fun simulateAsyncWork(item: Int): String {
        val delaySeconds = Random.nextInt(1, 5) // 1-4秒のランダム遅延
        println("Starting async work for item $item (will take ${delaySeconds}s)")
        delay(delaySeconds * 1000L)
        println("Completed async work for item $item")
        return "Processed-$item"
    }
    
    /**
     * 1. Sequential processing using map - each item is processed one by one
     */
    fun sequentialProcessing(): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }.map { item ->
        simulateAsyncWork(item) // Sequential processing
    }
    
    /**
     * 2. Concurrent processing using flatMapMerge - multiple items processed concurrently
     */
    fun concurrentProcessing(concurrency: Int = 3): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }.flatMapMerge(concurrency) { item ->
        flow { emit(simulateAsyncWork(item)) } // Concurrent processing
    }
    
    /**
     * 3. Using onEach for side effects with async operations
     */
    suspend fun asyncSideEffects(): Flow<Int> = coroutineScope {
        flow {
            repeat(5) { emit(it + 1) }
        }.onEach { item ->
            // onEach で非同期処理を行う（副作用として）
            launch {
                simulateAsyncWork(item)
            }
        }
    }
    
    /**
     * 4. Collecting all async results and waiting for completion
     */
    suspend fun collectAllWithCompletion() = coroutineScope {
        val sourceFlow = flow {
            repeat(5) { emit(it + 1) }
        }
        
        // すべての非同期処理を開始し、Jobを収集
        val jobs = mutableListOf<Job>()
        
        sourceFlow.collect { item ->
            val job = launch {
                simulateAsyncWork(item)
            }
            jobs.add(job)
        }
        
        // すべてのJobの完了を待つ
        println("Waiting for all async operations to complete...")
        jobs.joinAll()
        println("All async operations completed!")
    }
    
    /**
     * 5. Using transform with async processing inside coroutineScope
     */
    fun bufferedAsyncProcessing(): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }
    .flatMapMerge { item ->
        flow {
            coroutineScope {
                val deferred = async { simulateAsyncWork(item) }
                emit(deferred.await())
            }
        }
    }
    .buffer(10) // バッファリングでパフォーマンス向上
    
    /**
     * 6. Parallel processing with fixed window size
     */
    fun parallelWindowProcessing(windowSize: Int = 3): Flow<List<String>> = flow {
        repeat(10) { emit(it + 1) }
    }
    .chunked(windowSize) // 指定したサイズでグループ化
    .map { chunk ->
        coroutineScope {
            // 各チャンク内で並列処理
            chunk.map { item ->
                async { simulateAsyncWork(item) }
            }.awaitAll() // すべての結果を待つ
        }
    }
    
    /**
     * Extension function to chunk flow emissions
     */
    private fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> = flow {
        val chunk = mutableListOf<T>()
        collect { value ->
            chunk.add(value)
            if (chunk.size == size) {
                emit(chunk.toList())
                chunk.clear()
            }
        }
        if (chunk.isNotEmpty()) {
            emit(chunk.toList())
        }
    }
}

/**
 * Demonstration of async Flow patterns
 */
suspend fun main() = coroutineScope {
    
    println("=== 1. Sequential Processing ===")
    val sequentialTime = measureTime {
        FlowAsync.sequentialProcessing().collect { result ->
            println("Sequential result: $result")
        }
    }
    println("Sequential processing took: ${sequentialTime}ms\n")
    
    println("=== 2. Concurrent Processing ===")
    val concurrentTime = measureTime {
        FlowAsync.concurrentProcessing(concurrency = 3).collect { result ->
            println("Concurrent result: $result")
        }
    }
    println("Concurrent processing took: ${concurrentTime}ms\n")
    
    println("=== 3. Async Side Effects ===")
    val sideEffectJob = launch {
        FlowAsync.asyncSideEffects().collect { item ->
            println("Processed item: $item")
        }
    }
    sideEffectJob.join()
    delay(6000) // サイドエフェクトの完了を待つ
    println()
    
    println("=== 4. Collect All with Completion ===")
    FlowAsync.collectAllWithCompletion()
    println()
    
    println("=== 5. Buffered Async Processing ===")
    FlowAsync.bufferedAsyncProcessing().collect { result ->
        println("Buffered result: $result")
    }
    println()
    
    println("=== 6. Parallel Window Processing ===")
    FlowAsync.parallelWindowProcessing(windowSize = 3).collect { results ->
        println("Window results: $results")
    }
}
