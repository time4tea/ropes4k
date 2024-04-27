/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope.Companion.of
import net.ropes4k.test.PerformanceTest.Companion.bensAutoRaw
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class SearchSimpleBenchmark {

    val toFind = "consumes faster than Labor wears; while the used key is always bright,"

    val string = String(bensAutoRaw)
    val stringBuilder = StringBuilder(bensAutoRaw.size).also { it.append(bensAutoRaw) }
    val rope = of(bensAutoRaw!!)

    @Benchmark
    fun string(): Int {
        return string.indexOf(toFind)
    }

    @Benchmark
    fun stringbuilder(): Int {
        return stringBuilder.indexOf(toFind)
    }

    @Benchmark
    fun rope(): Int {
        return rope.indexOf(toFind)
    }
}
