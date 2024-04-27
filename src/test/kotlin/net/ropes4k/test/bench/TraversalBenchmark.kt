/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.PerformanceTest.Companion.aChristmasCarol
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
open class TraversalBenchmark {

    val checksum: Long

    val s = aChristmasCarol
    val sb = StringBuilder(aChristmasCarol)
    val r = Rope.of(aChristmasCarol)

    init {
        var result: Long = 0
        for (j in 0 until s.length) result += s[j].code.toLong()
        checksum = result
    }

    @Benchmark
    fun stringbuffer(): Long {
        var result: Long = 0
        for (j in 0 until sb.length) result += sb[j].code.toLong()
        check(result == checksum)
        return result
    }

    @Benchmark
    fun string(): Long {
        var result: Long = 0
        for (j in 0 until s.length) result += s[j].code.toLong()
        check(result == checksum)
        return result
    }

    @Benchmark
    fun rope_indexed(): Long {
        var result: Long = 0
        for (j in 0 until r.length) result += r[j].code.toLong()
        check(result == checksum)
        return result
    }

    @Benchmark
    fun rope_iterator(): Long {
        var result: Long = 0
        for (c in r) result += c.code.toLong()
        check(result == checksum)
        return result
    }
}
