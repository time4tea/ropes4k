/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.PerformanceTest.Companion.bensAutoRaw
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
class SearchSimpleBenchmark {

    val toFind = "consumes faster than Labor wears; while the used key is always bright,"

    val string = String(bensAutoRaw)
    val stringBuilder = StringBuilder(bensAutoRaw.size).also { it.append(bensAutoRaw) }
    val rope = Rope.BUILDER.build(bensAutoRaw)

    @Benchmark
    fun string() {
        string.indexOf(toFind)
    }

    @Benchmark
    fun stringbuilder() {
        stringBuilder.indexOf(toFind)
    }

    @Benchmark
    fun rope() {
        rope.indexOf(toFind)
    }
}
