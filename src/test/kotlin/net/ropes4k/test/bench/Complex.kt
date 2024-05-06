/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test.bench

import net.ropes4k.Rope
import net.ropes4k.test.PerformanceTest

class Complex {
    val inserts = (0 until PerformanceTest.PLAN_LENGTH / 10).map {
        val location = PerformanceTest.random.nextInt(PerformanceTest.aChristmasCarol.length)
        val clipFrom = PerformanceTest.random.nextInt(PerformanceTest.aChristmasCarol.length)
        PerformanceTest.Insert(
            location,
            clipFrom,
            PerformanceTest.random.nextInt(PerformanceTest.aChristmasCarol.length - clipFrom)
        )
    }

    val r = inserts.fold(Rope.of(PerformanceTest.aChristmasCarol)) { acc, i ->
        acc.insert(
            i.location,
            PerformanceTest.aChristmasCarol.subSequence(i.offset, i.offset + i.length)
        )
    }

    val sb = inserts.fold(StringBuilder(PerformanceTest.aChristmasCarol)) { acc, i ->
        acc.insert(
            i.location, PerformanceTest.aChristmasCarol.subSequence(i.offset, i.offset + i.length)
        )
    }

    val s = inserts.fold(PerformanceTest.aChristmasCarol) { acc, i ->
        acc.substring(0, i.location) + PerformanceTest.aChristmasCarol.substring(
            i.offset, i.offset + i.length
        ) + acc.substring(i.location)
    }

    val  checksum = s.map { it.code }.sum().toLong()
}