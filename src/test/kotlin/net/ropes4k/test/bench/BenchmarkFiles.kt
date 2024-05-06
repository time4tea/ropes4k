/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.test.bench

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class BenchmarkFiles {

    data class Insert(val location: Int, val offset: Int, val length: Int)

    companion object {
        private var seed = 342342
        val random = Random(seed.toLong())

        const val PLAN_LENGTH = 500

        private fun readpath(path: String): CharArray {
            return Files.readString(Path.of(path)).toCharArray()
        }

        val aChristmasCarolRaw = readpath("test-files/AChristmasCarol_CharlesDickens.txt")
        val bensAutoRaw = readpath("test-files/AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt")
        val aChristmasCarol = String(aChristmasCarolRaw)
        val bensAuto = String(bensAutoRaw)
    }
}
