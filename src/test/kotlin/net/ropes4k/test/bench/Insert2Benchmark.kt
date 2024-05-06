/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.bench.BenchmarkFiles.Companion.aChristmasCarol
import net.ropes4k.test.bench.BenchmarkFiles.Companion.bensAuto
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
open class Insert2Benchmark {

    /* Insert fragments of Benjamin Franklin's Autobiography into\n" +
                "* A Christmas Carol. */

    // string is so slow, plots show "0"
    val inserts = (0 until BenchmarkFiles.PLAN_LENGTH / 5).map {
        val clipFrom = BenchmarkFiles.random.nextInt(bensAuto.length)
        BenchmarkFiles.Insert(
            BenchmarkFiles.random.nextInt(aChristmasCarol.length),
            clipFrom,
            BenchmarkFiles.random.nextInt(bensAuto.length - clipFrom)
        )
    }


    @Benchmark
    fun rope(): Rope {
        val r = Rope.of(aChristmasCarol)

        return inserts.fold(r) { acc, i -> acc.insert(i.location, bensAuto.subSequence(i.offset, i.offset + i.length)) }
    }

    @Benchmark
    fun stringbuffer(): StringBuilder {
        val sb = StringBuilder(aChristmasCarol)

        return inserts.fold(sb) { acc, i ->
            acc.insert(
                i.location, bensAuto.subSequence(i.offset, i.offset + i.length)
            )
        }
    }

    @Benchmark
    fun string(): String {
        val s = aChristmasCarol
        return inserts.fold(s) { acc, i ->
            acc.substring(0, i.location) + bensAuto.substring(
                i.offset, i.offset + i.length
            ) + acc.substring(i.location)
        }
    }
}
