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
import java.lang.String
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class CreationBenchmark {


    @Benchmark
    fun string(): Any {
        return String(aChristmasCarolRaw)
    }

    @Benchmark
    fun stringbuilder(): Any {
        return StringBuilder(aChristmasCarol)
    }

    @Benchmark
    fun rope_charsequence(): Any {
        return Rope.of(aChristmasCarol)
    }

    @Benchmark
    fun rope_chararray(): Any {
        return Rope.ofCopy(aChristmasCarolRaw)
    }
}
