package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Demonstrates coroutine execution order with runBlocking, launch, and async/await.
 *
 * Expected Output:
 * 1. "B" (async finishes after 100ms and is awaited)
 * 2. "D" (prints immediately after deferred.await())
 * 3. "A" (launch finishes after 200ms before runBlocking finishes waiting for children)
 */
class RunBlockingExample {

    fun executeExample() {
        runBlocking {
            println("A")
            val job = launch {
                delay(200)
                println("A")
            }

            val deferred = async {
                delay(100)
                println("B")
            }

            deferred.await()
            println("D")
        }
    }
}

fun main() {
    val example = RunBlockingExample()
    example.executeExample()
}