package com.example.springboot.presentation.utils

sealed class Validated<E : Semigroup<E>, A> {
  data class Valid<E : Semigroup<E>, A>(val a: A) : Validated<E, A>()
  data class InValid<E : Semigroup<E>, A>(val e: E) : Validated<E, A>()

  companion object {
    fun <A1, A2, B, E : Semigroup<E>> map2(
      v1: Validated<E, A1>,
      v2: Validated<E, A2>,
      f: (A1, A2) -> B
    ): Validated<E, B> {
      return when (v1) {
        is Valid ->
          when (v2) {
            is Valid -> Valid(f(v1.a, v2.a))
            is InValid -> InValid(v2.e)
          }
        is InValid -> {
          when (v2) {
            is Valid -> InValid(v1.e)
            is InValid -> InValid(v1.e.combine(v2.e))
          }
        }
      }
    }

  }
}


