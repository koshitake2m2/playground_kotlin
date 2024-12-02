package com.example.kotlin

interface Animal {
    fun speak()
}

class Dog : Animal {
    init {
        println("Dog init")
    }

    override fun speak() {
        println("Woof!")
    }

    fun of(): Dog {
        println("of: It will be called only once by delegating before init called.")
        return this
    }
}

class DerivedDog(private val dog: Dog) : Animal by dog.of() {
    init {
        println("DerivedDog init")
    }
}

fun main() {
    val dog = Dog()
    val derivedDog = DerivedDog(dog)
    derivedDog.speak()
    derivedDog.speak()
}
