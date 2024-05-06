/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.bench.BenchmarkFiles.Companion.aChristmasCarol
import net.ropes4k.test.bench.BenchmarkFiles.Companion.aChristmasCarolRaw
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class RegexSimpleBenchmark {
    val pattern1 = Pattern.compile("plea.*y")
    val pattern2 = Pattern.compile("Cratchit")

    val string = aChristmasCarol
    val stringBuilder = StringBuilder(aChristmasCarolRaw.size).also { it.append(aChristmasCarolRaw) }
    val rope = Rope.ofCopy(aChristmasCarolRaw)

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
