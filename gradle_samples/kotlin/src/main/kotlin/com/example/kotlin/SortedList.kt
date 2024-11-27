package com.example.kotlin

/**
 * Requirements
 * - equals/hashCode returns same if two lists are equal
 * - toString returns sorted list
 * - It does not allow to generate SortedList with unsorted
 *   - copy should not exist
 *   - private constructor should be used
 *   - factory methods with sorting should be used
 *
 * Check [com.example.kotlin.SortedListTest] for test cases
 */
class SortedList<T : Comparable<T>> private constructor(private val sorted: List<T>) : List<T> by sorted {

    override fun equals(other: Any?): Boolean {
        return other is SortedList<*> && sorted == other.sorted
    }

    override fun hashCode(): Int {
        return sorted.hashCode()
    }

    override fun toString(): String {
        return "SortedList(sorted=$sorted)"
    }

    companion object {
        fun <T : Comparable<T>> of(vararg elementArray: T): SortedList<T> =
            SortedList(elementArray.toList().sorted())

        fun <T : Comparable<T>> reversedOf(vararg elementArray: T): SortedList<T> =
            SortedList(elementArray.toList().sortedDescending())
    }
}

fun main() {
    val sortedInt = SortedList.of(3, 2, 5, 4, 1)
    println("result: ${sortedInt}")
}
