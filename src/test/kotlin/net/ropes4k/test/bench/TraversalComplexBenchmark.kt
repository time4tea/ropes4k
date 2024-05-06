/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
/** * Insert fragments of A Christmas Carol back into itself */
open class TraversalComplexBenchmark {

    val complex = Complex()
    val sb = complex.sb
    val r = complex.r
    val s = complex.s
    val checksum = complex.checksum

    @Benchmark
    fun stringbuffer(): Long {
        var result: Long = 0
        for (j in 0 until sb.length) result += sb[j].code.toLong()
        require(result == checksum) { "Checksum mismatch, expected $checksum, was $result" }
        return result
    }

    @Benchmark
    fun string(): Long {
        var result: Long = 0
        for (j in 0 until s.length) result += s[j].code.toLong()
        require(result == checksum) { "Checksum mismatch, expected $checksum, was $result" }
        return result
    }

    @Benchmark
    fun rope_indexed(): Long {
        var result: Long = 0
        for (j in 0 until r.length) result += r[j].code.toLong()
        require(result == checksum) { "Checksum mismatch, expected $checksum, was $result" }
        return result
    }

    @Benchmark
    fun rope_iterator(): Long {
        var result: Long = 0
        for (c in r) result += c.code.toLong()
        require(result == checksum) { "Checksum mismatch, expected $checksum, was $result" }
        return result
    }
}
