/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.bench.BenchmarkFiles.Companion.PLAN_LENGTH
import net.ropes4k.test.bench.BenchmarkFiles.Companion.aChristmasCarol
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class PrependBenchmark {

    data class Prepend(val offset: Int, val length: Int)

    val prepends = (0 until PLAN_LENGTH).map {
        val offset = BenchmarkFiles.random.nextInt(aChristmasCarol.length)
        Prepend(offset, BenchmarkFiles.random.nextInt(aChristmasCarol.length - offset))
    }

    val checksum: Int

    init {
        var result = aChristmasCarol
        prepends.forEach {
            result = result.substring(it.offset, it.offset + it.length) + result
        }
        checksum = result.length
    }

    @Benchmark
    fun rope(): Int {
        var result = Rope.of(aChristmasCarol)

        prepends.forEach {
            result = result.subSequence(it.offset, it.offset + it.length).append(result)
        }
        check(result.length == checksum)
        return result.length
    }

    @Benchmark
    fun stringbuffer(): Int {
        val result = StringBuilder(aChristmasCarol)

        prepends.forEach {
            result.insert(0, result.subSequence(it.offset, it.offset + it.length))
        }

        check(result.length == checksum)
        return result.length
    }

    @Benchmark
    fun string(): Int {
        var result = aChristmasCarol

        prepends.forEach {
            result = result.substring(it.offset, it.offset + it.length) + result
        }
        check(result.length == checksum)
        return result.length
    }
}
