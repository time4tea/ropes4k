/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class RegexComplexBenchmark {
    val pattern1 = Pattern.compile("plea.*y")
    val pattern2 = Pattern.compile("Cratchit")

    val complex = Complex()
    val stringBuilder = complex.sb
    val rope = complex.r
    val string = complex.s

    @Benchmark
    fun string1(): Int {
        var result = 0
        val m = pattern1.matcher(string)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun string2(): Int {
        var result = 0
        val m = pattern2.matcher(string)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun stringbuffer1(): Int {
        var result = 0
        val m = pattern1.matcher(stringBuilder)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun stringbuffer2(): Int {
        var result = 0
        val m = pattern2.matcher(stringBuilder)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun rope1(): Int {
        var result = 0
        val m = pattern1.matcher(rope)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun rope2(): Int {
        var result = 0
        val m = pattern2.matcher(rope)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun ropeMatcher1(): Int {
        var result = 0
        val m = rope.matcher(pattern1)
        while (m.find()) ++result
        return result
    }

    @Benchmark
    fun ropeMatcher2(): Int {
        var result = 0
        val m = rope.matcher(pattern2)
        while (m.find()) ++result
        return result
    }
}
