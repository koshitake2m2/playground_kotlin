package com.example.springboot.presentation.utils

interface Semigroup<A> {
  fun combine(that: A): A
}
