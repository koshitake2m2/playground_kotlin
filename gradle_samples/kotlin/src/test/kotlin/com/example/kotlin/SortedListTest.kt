package com.example.kotlin

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SortedListTest : DescribeSpec({
    data class Point(val x: Int, val y: Int) : Comparable<Point> {
        override fun compareTo(other: Point): Int {
            return compareValuesBy(this, other, Point::x, Point::y)
        }
    }

    describe("equals/hashCode") {
        it("should return same if two lists are equal") {
            val x = SortedList.of(5, 3, 4, 1, 2)
            val y = SortedList.of(3, 1, 2, 5, 4)
            x.hashCode() shouldBe y.hashCode()
            x.equals(y) shouldBe true
            (x == y) shouldBe true
            x shouldBe y
        }
        it("should return different if two lists are not equal") {
            val x = SortedList.of(0, 3, 4, 1, 2)
            val y = SortedList.of(3, 1, 2, 5, 4)
            x.hashCode() shouldNotBe y.hashCode()
            x.equals(y) shouldBe false
            (x == y) shouldBe false
            x shouldNotBe y
        }
        it("should return different if other is not SortedList") {
            val x = SortedList.of(5, 3, 4, 1, 2)
            val y = listOf(1, 2, 3, 4, 5)
            x.hashCode() shouldNotBe y.hashCode()
            x.equals(y) shouldBe false
            (x == y) shouldBe false

            // This will pass, but it should fail
            // x shouldNotBe y
        }
    }
    describe("toString") {
        it("should return sorted list") {
            val sortedInt = SortedList.of(3, 2, 5, 4, 1)
            sortedInt.toString() shouldBe "SortedList(sorted=[1, 2, 3, 4, 5])"
        }
    }
    describe("of") {
        it("should return sorted list") {
            val sortedInt = SortedList.of(3, 2, 5, 4, 1)
            sortedInt.toList() shouldBe listOf(1, 2, 3, 4, 5)
        }

        it("should return sorted list of instance of comparable") {
            val sortedPoints = SortedList.of(
                Point(3, 5),
                Point(2, 3),
                Point(5, 4),
                Point(4, 2),
                Point(1, 1),
            )
            sortedPoints.toList() shouldBe listOf(
                Point(1, 1),
                Point(2, 3),
                Point(3, 5),
                Point(4, 2),
                Point(5, 4),
            )
        }
    }
    describe("reverseOf") {
        it("should return sorted list in descending order") {
            val sortedInt = SortedList.reversedOf(3, 2, 5, 4, 1)
            sortedInt.toList() shouldBe listOf(5, 4, 3, 2, 1)
        }
    }
})
