/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import org.openjdk.jmh.annotations.*
import java.io.StringWriter
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
open class WriteComplexBenchmark {

    val complex = Complex()

    val string = complex.s
    val stringBuilder = complex.sb
    val rope = complex.r

    val expected = string.length

    @Benchmark
    fun string(): Int {
        val writer = StringWriter(string.length)
        writer.use { it.write(string) }
        return writer.buffer.length.also {
            require(it == expected) { "Checksum mismatch, expected $expected, was $it" }
        }
    }

    @Benchmark
    fun stringbuilder(): Int {
        val writer = StringWriter(stringBuilder.length)
        writer.use { it.write(stringBuilder.toString()) }
        return writer.buffer.length.also {
            require(it == expected) { "Checksum mismatch, expected $expected, was $it" }
        }
    }

    @Benchmark
    fun rope_generic(): Int {
        val writer = StringWriter(rope.length)
        writer.use { it.write(rope.toString()) }
        return writer.buffer.length.also {
            require(it == expected) { "Checksum mismatch, expected $expected, was $it" }
        }
    }

    @Benchmark
    fun rope_specialised(): Int {
        val writer = StringWriter(rope.length)
        writer.use { rope.write(it) }
        return writer.buffer.length.also {
            require(it == expected) { "Checksum mismatch, expected $expected, was $it" }
        }
    }
}
