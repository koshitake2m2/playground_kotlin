package com.example.kotlin

data class SortedList<T : Comparable<T>>(private val unsorted: List<T>) : List<T> by unsorted.sorted() {
    companion object {
        fun <T : Comparable<T>> of(vararg elementArray: T): SortedList<T> = SortedList(elementArray.toList())
    }
}

data class Point(val x: Int, val y: Int) : Comparable<Point> {
    override fun compareTo(other: Point): Int {
        return compareValuesBy(this, other, Point::x, Point::y)
    }
}

fun main(): Unit {
    val sortedInt = SortedList.of(3, 2, 5, 4, 1)
    println("unsorted: ${sortedInt}")
    println("sorted: ${sortedInt.toList()}")

    val sortedPoints = SortedList.of(
        Point(3, 5),
        Point(2, 3),
        Point(5, 4),
        Point(4, 2),
        Point(1, 1),
    )
    println("unsorted: ${sortedPoints}")
    println("sorted: ${sortedPoints.toList()}")
}
