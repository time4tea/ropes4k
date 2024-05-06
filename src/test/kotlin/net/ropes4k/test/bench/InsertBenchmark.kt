/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.PerformanceTest.Companion.PLAN_LENGTH
import net.ropes4k.test.PerformanceTest.Companion.aChristmasCarol
import net.ropes4k.test.PerformanceTest.Companion.random
import net.ropes4k.test.PerformanceTest.Insert
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
/** * Insert fragments of A Christmas Carol back into itself */
open class InsertBenchmark {

    val inserts = (0 until PLAN_LENGTH / 10).map {
        val location = random.nextInt(aChristmasCarol.length)
        val clipFrom = random.nextInt(aChristmasCarol.length)
        Insert(
            location,
            clipFrom,
            random.nextInt(aChristmasCarol.length - clipFrom)
        )
    }

    @Benchmark
    fun rope(): Rope {
        val r = Rope.of(aChristmasCarol)

        return inserts.fold(r) { acc, i ->
            acc.insert(
                i.location,
                aChristmasCarol.subSequence(i.offset, i.offset + i.length)
            )
        }
    }

    @Benchmark
    fun stringbuffer(): StringBuilder {
        val sb = StringBuilder(aChristmasCarol)

        return inserts.fold(sb) { acc, i ->
            acc.insert(
                i.location, aChristmasCarol.subSequence(i.offset, i.offset + i.length)
            )
        }
    }

    @Benchmark
    fun string(): String {
        val s = aChristmasCarol
        return inserts.fold(s) { acc, i ->
            acc.substring(0, i.location) + aChristmasCarol.substring(
                i.offset, i.offset + i.length
            ) + acc.substring(i.location)
        }
    }
}
