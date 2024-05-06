/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope

class Complex {
    val inserts = (0 until BenchmarkFiles.PLAN_LENGTH / 10).map {
        val location = BenchmarkFiles.random.nextInt(BenchmarkFiles.aChristmasCarol.length)
        val clipFrom = BenchmarkFiles.random.nextInt(BenchmarkFiles.aChristmasCarol.length)
        BenchmarkFiles.Insert(
            location,
            clipFrom,
            BenchmarkFiles.random.nextInt(BenchmarkFiles.aChristmasCarol.length - clipFrom)
        )
    }

    val r = inserts.fold(Rope.of(BenchmarkFiles.aChristmasCarol)) { acc, i ->
        acc.insert(
            i.location,
            BenchmarkFiles.aChristmasCarol.subSequence(i.offset, i.offset + i.length)
        )
    }

    val sb = inserts.fold(StringBuilder(BenchmarkFiles.aChristmasCarol)) { acc, i ->
        acc.insert(
            i.location, BenchmarkFiles.aChristmasCarol.subSequence(i.offset, i.offset + i.length)
        )
    }

    val s = inserts.fold(BenchmarkFiles.aChristmasCarol) { acc, i ->
        acc.substring(0, i.location) + BenchmarkFiles.aChristmasCarol.substring(
            i.offset, i.offset + i.length
        ) + acc.substring(i.location)
    }

    val  checksum = s.map { it.code }.sum().toLong()
}