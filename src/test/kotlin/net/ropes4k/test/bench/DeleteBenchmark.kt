/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.PerformanceTest
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class DeleteBenchmark {

    data class Delete(val offset: Int, val length: Int, val expected: Int)

    lateinit var deletes: List<Delete>

    @Setup
    fun setUp() {
        var newSize = PerformanceTest.aChristmasCarol.length

        deletes = (0 until PerformanceTest.PLAN_LENGTH).map {
            val offset = PerformanceTest.random.nextInt(newSize)
            val length = PerformanceTest.random.nextInt(
                min(100, (newSize - offset))
            )
            val expected = newSize - length
            newSize = expected
            Delete(offset, length, expected)
        }
    }

    @Benchmark
    fun string(): String {
        return deletes.fold(PerformanceTest.aChristmasCarol) { acc, it ->
            acc.substring(0, it.offset) + acc.substring(it.offset + it.length)
        }
    }

    @Benchmark
    fun stringBuilder(): StringBuilder {
        return deletes.fold(StringBuilder(PerformanceTest.aChristmasCarol)) { acc, it ->
            acc.delete(it.offset, it.offset + it.length)
        }
    }

    @Benchmark
    fun rope(): Rope? {
        return deletes.fold(Rope.BUILDER.build(PerformanceTest.aChristmasCarol)) { acc, it ->
            acc.delete(it.offset, it.offset + it.length)
        }
    }
}