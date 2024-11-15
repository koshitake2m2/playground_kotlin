package com.sample.kotlin

import kotlin.Result

class MyException(override val message: String) : Throwable()

inline fun <R, T> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> {
    return when {
        isSuccess -> transform(getOrNull()!!)
        else -> Result.failure(exceptionOrNull()!!)
    }
}


fun main(args: Array<String>) {
    val res = Result.success("hello1")
    val res2 = Result.success("hello2")
    val resE = Result.failure<String>(MyException("error1"))
    val resE2 = Result.failure<String>(MyException("error2"))

    res
        .flatMap { res2 }
        .let { println(it) }

    res
        .flatMap { resE }
        .flatMap { resE2 }
        .let { println(it) }
}
