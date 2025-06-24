package com.example.kotlinx

import com.example.utils.measureTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)

/**
 * Shared utilities for async Flow samples
 */
object FlowAsync {
    
    /**
     * Simulates async work with random delay
     */
    suspend fun simulateAsyncWork(item: Int): String {
        val delaySeconds = Random.nextInt(1, 5) // 1-4秒のランダム遅延
        println("Starting async work for item $item (will take ${delaySeconds}s)")
        delay(delaySeconds * 1000L)
        println("Completed async work for item $item")
        return "Processed-$item"
    }
}

object SequentialProcessingMain {
    
    /**
     * Sequential processing using map - each item is processed one by one
     */
    private fun sequentialProcessing(): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }.map { item ->
        FlowAsync.simulateAsyncWork(item) // Sequential processing
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Sequential Processing ===")
            val sequentialTime = measureTime {
                sequentialProcessing().collect { result ->
                    println("Sequential result: $result")
                }
            }
            println("Sequential processing took: ${sequentialTime}ms")
        }
    }
}

object ConcurrentProcessingMain {
    
    /**
     * Concurrent processing using flatMapMerge - multiple items processed concurrently
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun concurrentProcessing(concurrency: Int = 3): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }.flatMapMerge(concurrency) { item ->
        flow { emit(FlowAsync.simulateAsyncWork(item)) } // Concurrent processing
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Concurrent Processing ===")
            val concurrentTime = measureTime {
                concurrentProcessing(concurrency = 3).collect { result ->
                    println("Concurrent result: $result")
                }
            }
            println("Concurrent processing took: ${concurrentTime}ms")
        }
    }
}

object AsyncSideEffectsMain {
    
    /**
     * Using onEach for side effects with async operations
     */
    private suspend fun asyncSideEffects(): Flow<Int> = coroutineScope {
        flow {
            repeat(5) { emit(it + 1) }
        }.onEach { item ->
            // onEach で非同期処理を行う（副作用として）
            launch {
                FlowAsync.simulateAsyncWork(item)
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Async Side Effects ===")
            val sideEffectJob = launch {
                asyncSideEffects().collect { item ->
                    println("Processed item: $item")
                }
            }
            sideEffectJob.join()
            delay(6000) // サイドエフェクトの完了を待つ
        }
    }
}

object CollectAllWithCompletionMain {
    
    /**
     * Collecting all async results and waiting for completion
     */
    private suspend fun collectAllWithCompletion() = coroutineScope {
        val sourceFlow = flow {
            repeat(5) { emit(it + 1) }
        }
        
        // すべての非同期処理を開始し、Jobを収集
        val jobs = mutableListOf<Job>()
        
        sourceFlow.collect { item ->
            val job = launch {
                FlowAsync.simulateAsyncWork(item)
            }
            jobs.add(job)
        }
        
        // すべてのJobの完了を待つ
        println("Waiting for all async operations to complete...")
        jobs.joinAll()
        println("All async operations completed!")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Collect All with Completion ===")
            collectAllWithCompletion()
        }
    }
}

object BufferedAsyncProcessingMain {
    
    /**
     * Using transform with async processing inside coroutineScope
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun bufferedAsyncProcessing(): Flow<String> = flow {
        repeat(5) { emit(it + 1) }
    }
    .flatMapMerge { item ->
        flow {
            coroutineScope {
                val deferred = async { FlowAsync.simulateAsyncWork(item) }
                emit(deferred.await())
            }
        }
    }
    .buffer(10) // バッファリングでパフォーマンス向上

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Buffered Async Processing ===")
            bufferedAsyncProcessing().collect { result ->
                println("Buffered result: $result")
            }
        }
    }
}

object ParallelWindowProcessingMain {
    
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

    /**
     * Parallel processing with fixed window size
     */
    private fun parallelWindowProcessing(windowSize: Int = 3): Flow<List<String>> = flow {
        repeat(10) { emit(it + 1) }
    }
        .chunked(windowSize) // 指定したサイズでグループ化
        .map { chunk ->
            coroutineScope {
                // 各チャンク内で並列処理
                chunk.map { item ->
                    async { FlowAsync.simulateAsyncWork(item) }
                }.awaitAll() // すべての結果を待つ
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Parallel Window Processing ===")
            parallelWindowProcessing(windowSize = 3).collect { results ->
                println("Window results: $results")
            }
            println("All windows processed!")
        }
    }
}

object ParallelProcessingFlatMapMergeMain {

    private fun streamChunks(): Flow<List<Int>> = flow {
        repeat(5) { emit((it * 5 + 1..it * 5 + 5).toList()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Parallel Processing ===")
            streamChunks()
                .flatMapMerge(concurrency = 3) { chunk ->
                    flow {
                        chunk.asFlow().map { it ->
                            async { FlowAsync.simulateAsyncWork(it) }
                        }.toList().awaitAll()
                        emit(null)
                        println("Chunk processed: $chunk")
                    }
                }.collect()

            println("All chunks processed!")
        }
    }
}

object ParallelProcessingChannelFlowMain {

    private fun streamChunks(): Flow<List<Int>> = flow {
        repeat(10) { emit((it * 5 + 1..it * 5 + 5).toList()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            println("=== Parallel Processing with Channel Flow ===")
            streamChunks()
                .flatMapMerge(concurrency = 3) { chunk ->
                    channelFlow {
                        chunk.forEach {
                            launch {
                                send(FlowAsync.simulateAsyncWork(it))
                            }
                        }
                        println("Chunk processed: $chunk")
                    }
                }.collect { result ->
                    println("Processed result: $result")
                }

            println("All chunks processed!")
        }
    }
}

// WARNING: You should use Semaphore to avoid explodes memory.
object ParallelAsyncAsyncMain {

    private fun streamChunks(): Flow<List<Int>> = flow {
        repeat(10) { emit((it * 5 + 1..it * 5 + 5).toList()) }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val semaphore = Semaphore(10)
        runBlocking {
            streamChunks()
                .map { chunk ->
                    async {
                        println("Start chunk: $chunk")
                        coroutineScope {
                            chunk.map { item ->
                                async {
                                    semaphore.withPermit {
                                        FlowAsync.simulateAsyncWork(item)
                                    }
                                }
                            }.awaitAll()
                        }
                        println("End chunk: $chunk")
                    }
                }.toList().awaitAll()
            println("All chunks processed!")
        }
    }
}
