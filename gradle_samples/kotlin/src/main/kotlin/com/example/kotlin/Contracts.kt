package com.example.kotlin

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun getStr(): String? {
    return "hello"
}

@ExperimentalContracts
fun isNull(str: String?): Boolean {
    contract {
        returns(true) implies (str == null)
        returns(false) implies (str != null)
    }
    return str == null
}


@OptIn(ExperimentalContracts::class)
fun main() {

    val str: String? = getStr()

    if (isNull(str)) {
        println(str)
    } else {
        println(str.length)
    }

}