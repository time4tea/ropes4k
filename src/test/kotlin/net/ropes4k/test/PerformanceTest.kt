/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.test

import net.ropes4k.Rope
import net.ropes4k.impl.AbstractRope
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.StringWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern
import kotlin.math.min

/**
 * Performs an extensive performance test comparing Ropes, Strings, and
 * StringBuffers.
 */
@Tag("Performance")
class PerformanceTest {

    @Test
    fun `inserts and things that depend on them`() {
        insertPlan()
        traversal2()
        regexComplex()
        search2()
        write()
    }

    data class Insert(val location: Int, val offset: Int, val length: Int)

    private fun insertPlan() {
        println()
        println("**** INSERT PLAN TEST ****")
        println("* Insert fragments of A Christmas Carol back into itself.\n")

        val length = aChristmasCarol.length
        val inserts = (0 until PLAN_LENGTH).map {
            val clipFrom = random.nextInt(length)
            Insert(
                random.nextInt(length),
                clipFrom,
                random.nextInt(length - clipFrom)
            )
        }

        (0..inserts.size step 20).forEach {
            println("Insert plan length: ${inserts.size}")
            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            for (j in 0 until 2) { // 7 takes too long!
                stats0[j] = stringInsertTest(aChristmasCarolRaw, inserts)
                stats1[j] = stringBufferInsertTest(aChristmasCarolRaw, inserts)
                stats2[j] = ropeInsertTest(aChristmasCarolRaw, inserts)
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope]")
        }
    }


    @Test
    fun prependPlan() {
        println()
        println("**** PREPEND PLAN TEST ****")
        println()

        val prependPlan = Array(PLAN_LENGTH) { IntArray(2) }
        for (j in prependPlan.indices) {
            prependPlan[j][0] = random.nextInt(aChristmasCarol.length)
            prependPlan[j][1] = random.nextInt(aChristmasCarol.length - prependPlan[j][0])
        }

        var k = 20
        while (k <= prependPlan.size) {
            println("Prepend plan length: $k")
            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            for (j in 0 until ITERATION_COUNT) {
                stats0[j] = stringPrependTest(aChristmasCarol, prependPlan, k)
                stats1[j] = stringBufferPrependTest(aChristmasCarol, prependPlan, k)
                stats2[j] = ropePrependTest(aChristmasCarol, prependPlan, k)
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope]")
            k += 20
        }
    }

    data class Delete(val offset: Int, val length: Int, val expected: Int)

    @Test
    fun deletePlan() {
        println()
        println("**** DELETE PLAN TEST ****")
        println()
        var newSize = aChristmasCarol.length

        val deletes = (0 until PLAN_LENGTH).map {
            val offset = random.nextInt(newSize)
            val length = random.nextInt(
                min(100, (newSize - offset))
            )
            val expected = newSize - length
            newSize = expected
            Delete(offset, length, expected)
        }

        (0..deletes.size step 20).forEach {
            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            for (j in 0 until ITERATION_COUNT) {
                stats0[j] = timeit("delete") {
                    deletes.fold(aChristmasCarol) { acc, it ->
                        acc.substring(0, it.offset) + acc.substring(it.offset + it.length)
                    }
                }
                stats1[j] = timeit("delete") {
                    deletes.fold(StringBuilder(aChristmasCarol)) { acc, it ->
                        acc.delete(it.offset, it.offset + it.length)
                    }
                }
                stats2[j] = timeit("delete") {
                    deletes.fold(Rope.BUILDER.build(aChristmasCarol)) { acc, it ->
                        acc.delete(it.offset, it.offset + it.length)
                    }
                }
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope]")
        }
    }

    companion object {
        private var seed = 342342
        val random = Random(seed.toLong())

        const val ITERATION_COUNT = 7
        const val PLAN_LENGTH = 500

        private var complexString: String? = null
        private var complexStringBuffer: StringBuffer? = null
        private var complexRope: Rope? = null

        private fun search2() {
            println()
            println("**** STRING SEARCH TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES)****")
            println(
                """
    * Using a complexly constructed rope and the pattern 'consumes faster
    * than Labor wears; while the used key is always bright,'.
    """.trimIndent()
            )


            val toFind = "Bob was very cheerful with them, and spoke pleasantly to"
            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            for (j in 0 until ITERATION_COUNT) {
                stats0[j] = stringFindTest2(complexString, toFind)
                stats1[j] = stringBufferFindTest2(complexStringBuffer, toFind)
                stats2[j] = ropeFindTest2(complexRope, toFind)
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope]")
        }

        private fun write() {
            println()
            println("**** WRITE TEST ****")
            println("* Illustrates how to write a Rope to a stream efficiently.")

            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            for (j in 0 until ITERATION_COUNT) {
                stats0[j] = ropeWriteBad(complexRope)
                stats1[j] = ropeWriteGood(complexRope)
            }
            stat(stats0, "[Out.write]")
            stat(stats1, "[Rope.write]")
        }

        private fun regexComplex() {
            println()
            println("**** REGULAR EXPRESSION TEST (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****")
            println("* Using a complexly-constructed rope and the pattern 'Crachit'.")


            val p1 = Pattern.compile("Cratchit")
            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            val stats3 = LongArray(ITERATION_COUNT)
            val stats4 = LongArray(ITERATION_COUNT)
            for (j in 0 until ITERATION_COUNT) {
                stats0[j] = stringRegexpTest2(complexString, p1)
                stats1[j] = stringBufferRegexpTest2(complexStringBuffer, p1)
                stats2[j] = ropeRegexpTest2(complexRope, p1)
                stats3[j] = ropeRebalancedRegexpTest2(complexRope, p1)
                stats4[j] = ropeMatcherRegexpTest2(complexRope, p1)
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope]")
            stat(stats3, "[Reblncd Rope]")
            stat(stats4, "[Rope.matcher]")
        }

        private fun traversal2() {
            println()
            println("**** TRAVERSAL TEST 2 (COMPLEXLY-CONSTRUCTED DATASTRUCTURES) ****")
            println(
                """
    * A traversal test wherein the datastructures are complexly
    * constructed, meaning constructed through hundreds of insertions,
    * substrings, and deletions (deletions not yet implemented). In
    * this case, we expect rope performance to suffer, with the
    * iterator version performing better than the charAt version.
    """.trimIndent()
            )
            println()

            val stats0 = LongArray(ITERATION_COUNT)
            val stats1 = LongArray(ITERATION_COUNT)
            val stats2 = LongArray(ITERATION_COUNT)
            val stats3 = LongArray(ITERATION_COUNT)
            for (j in 0..2) {
                stats0[j] = stringTraverseTest2(complexString)
                stats1[j] = stringBufferTraverseTest2(complexStringBuffer)
                stats2[j] = ropeTraverseTest2_1(complexRope)
                stats3[j] = ropeTraverseTest2_2(complexRope)
            }
            stat(stats0, "[String]")
            stat(stats1, "[StringBuffer]")
            stat(stats2, "[Rope/charAt]")
            stat(stats3, "[Rope/itr]")
        }


        private fun stringFindTest2(aChristmasCarol: String?, toFind: String): Long {
            val x = System.nanoTime()
            val loc = aChristmasCarol!!.indexOf(toFind)
            val y = System.nanoTime()
            System.out.printf(
                "[String.find]       indexOf needle length %d found at index %d in % ,18d ns.\n",
                toFind.length,
                loc,
                (y - x)
            )
            return (y - x)
        }

        private fun stringBufferFindTest2(aChristmasCarol: StringBuffer?, toFind: String): Long {
            val x = System.nanoTime()
            val loc = aChristmasCarol!!.indexOf(toFind)
            val y = System.nanoTime()
            System.out.printf(
                "[StringBuffer.find] indexOf needle length %d found at index %d in % ,18d ns.\n",
                toFind.length,
                loc,
                (y - x)
            )
            return (y - x)
        }

        private fun ropeFindTest2(aChristmasCarol: Rope?, toFind: String): Long {
            val x = System.nanoTime()
            val loc = aChristmasCarol!!.indexOf(toFind)
            val y = System.nanoTime()
            System.out.printf(
                "[Rope.find]         indexOf needle length %d found at index %d in % ,18d ns.\n",
                toFind.length,
                loc,
                (y - x)
            )
            return (y - x)
        }

        private fun ropeWriteGood(complexRope: Rope?): Long {
            val out: Writer = StringWriter(complexRope!!.length)
            val x = System.nanoTime()
            complexRope.write(out)
            val y = System.nanoTime()
            System.out.printf("[Rope.write]   Executed write in % ,18d ns.\n", (y - x))
            return (y - x)
        }

        private fun ropeWriteBad(complexRope: Rope?): Long {
            val out: Writer = StringWriter(complexRope!!.length)
            val x = System.nanoTime()
            out.write(complexRope.toString())
            val y = System.nanoTime()
            System.out.printf("[Out.write]    Executed write in % ,18d ns.\n", (y - x))
            return (y - x)
        }

        private fun readpath(path: String): CharArray {
            return Files.readString(Path.of(path)).toCharArray()
        }


        private fun timeit(name: String, f: () -> CharSequence): Long {
            val x = System.nanoTime()
            val result = f()
            val y = System.nanoTime()

            val variant = result.javaClass.simpleName

            val depth = when (result) {
                is Rope -> (result as AbstractRope).depth()
                else -> null
            }

            System.out.printf(
                "[${variant.padEnd(20)}]         Executed ${name} in % ,18d ns. Result has length: %d. Rope Depth: %d\n",
                (y - x),
                result.length,
                depth
            )
            return (y - x)
        }

        private fun ropeInsertTest(aChristmasCarol: CharArray, inserts: List<Insert>): Long {
            var result = Rope.BUILDER.build(aChristmasCarol)

            val x = System.nanoTime()

            inserts.forEach {
                result = result.insert(it.location, result.subSequence(it.offset, it.offset + it.length))
            }

            val y = System.nanoTime()
            System.out.printf(
                "[Rope]         Executed insert plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n",
                (y - x),
                result.length,
                (result as AbstractRope).depth()
            )
            complexRope = result
            return (y - x)
        }


        private fun ropePrependTest(aChristmasCarol: String, prependPlan: Array<IntArray>, planLength: Int): Long {
            val x = System.nanoTime()
            var result = Rope.BUILDER.build(aChristmasCarol)

            for (j in 0 until planLength) {
                val offset = prependPlan[j][0]
                val length = prependPlan[j][1]
                result = result.subSequence(offset, offset + length).append(result)
            }
            val y = System.nanoTime()
            System.out.printf(
                "[Rope]         Executed prepend plan in % ,18d ns. Result has length: %d. Rope Depth: %d\n",
                (y - x),
                result.length,
                (result as AbstractRope).depth()
            )
            return (y - x)
        }



        private fun ropeTraverseTest2_1(aChristmasCarol: Rope?): Long {
            var r = 0
            val x = System.nanoTime()
            for (j in 0 until aChristmasCarol!!.length) r += aChristmasCarol[j].code
            val y = System.nanoTime()
            System.out.printf("[Rope/charAt]  Executed traversal in % ,18d ns. Result checksum: %d\n", (y - x), r)
            return (y - x)
        }

        private fun ropeTraverseTest2_2(aChristmasCarol: Rope?): Long {
            var r = 0
            val x = System.nanoTime()
            for (c in aChristmasCarol!!) r += c.code
            val y = System.nanoTime()
            System.out.printf("[Rope/itr]     Executed traversal in % ,18d ns. Result checksum: %d\n", (y - x), r)
            return (y - x)
        }



        private fun stringBufferInsertTest(aChristmasCarol: CharArray, inserts: List<Insert>): Long {
            val result = StringBuffer(aChristmasCarol.size)
            result.append(aChristmasCarol)

            val x = System.nanoTime()

            inserts.forEach {
                result.insert(it.location, result.subSequence(it.offset, it.offset + it.length))
            }

            val y = System.nanoTime()
            System.out.printf(
                "[StringBuffer] Executed insert plan in % ,18d ns. Result has length: %d\n",
                (y - x),
                result.length
            )
            complexStringBuffer = result
            return (y - x)
        }


        private fun stringBufferPrependTest(
            aChristmasCarol: String,
            prependPlan: Array<IntArray>,
            planLength: Int
        ): Long {
            val x = System.nanoTime()
            val result = StringBuilder(aChristmasCarol)

            for (j in 0 until planLength) {
                val offset = prependPlan[j][0]
                val length = prependPlan[j][1]
                result.insert(0, result.subSequence(offset, offset + length))
            }
            val y = System.nanoTime()
            System.out.printf(
                "[StringBuffer] Executed prepend plan in % ,18d ns. Result has length: %d\n",
                (y - x),
                result.length
            )
            return (y - x)
        }


        private fun stringBufferTraverseTest2(aChristmasCarol: StringBuffer?): Long {
            var r = 0
            val x = System.nanoTime()
            for (j in 0 until aChristmasCarol!!.length) r += aChristmasCarol[j].code
            val y = System.nanoTime()
            System.out.printf("[StringBuffer] Executed traversal in % ,18d ns. Result checksum: %d\n", (y - x), r)
            return (y - x)
        }

        private fun stringInsertTest(aChristmasCarol: CharArray, inserts: List<Insert>): Long {
            var result = String(aChristmasCarol)

            val x = System.nanoTime()

            inserts.forEach {
                result = result.substring(0, it.location) + result.substring(
                    it.offset,
                    it.offset + it.length
                ) + result.substring(it.location)
            }

            val y = System.nanoTime()
            System.out.printf(
                "[String]       Executed insert plan in % ,18d ns. Result has length: %d\n",
                (y - x),
                result.length
            )
            complexString = result
            return (y - x)
        }

        private fun stringPrependTest(aChristmasCarol: String, prependPlan: Array<IntArray>, planLength: Int): Long {
            val x = System.nanoTime()
            var result = aChristmasCarol

            for (j in 0 until planLength) {
                val offset = prependPlan[j][0]
                val length = prependPlan[j][1]
                result = result.substring(offset, offset + length) + result
            }
            val y = System.nanoTime()
            System.out.printf(
                "[String]       Executed prepend plan in % ,18d ns. Result has length: %d\n",
                (y - x),
                result.length
            )
            return (y - x)
        }


        private fun stringTraverseTest2(aChristmasCarol: String?): Long {
            var r = 0
            val x = System.nanoTime()
            for (j in 0 until aChristmasCarol!!.length) r += aChristmasCarol[j].code
            val y = System.nanoTime()
            System.out.printf("[String]       Executed traversal in % ,18d ns. Result checksum: %d\n", (y - x), r)
            return (y - x)
        }


        private fun stringRegexpTest2(aChristmasCarol: String?, pattern: Pattern): Long {
            val x = System.nanoTime()

            var result = 0
            val m = pattern.matcher(aChristmasCarol)
            while (m.find()) ++result

            val y = System.nanoTime()
            System.out.printf("[String]       Executed regexp test in % ,18d ns. Found %d matches.\n", (y - x), result)
            return (y - x)
        }


        private fun stringBufferRegexpTest2(aChristmasCarol: StringBuffer?, pattern: Pattern): Long {
            val x = System.nanoTime()

            var result = 0
            val m = pattern.matcher(aChristmasCarol)
            while (m.find()) ++result

            val y = System.nanoTime()
            System.out.printf("[StringBuffer] Executed regexp test in % ,18d ns. Found %d matches.\n", (y - x), result)
            return (y - x)
        }

        private fun ropeRegexpTest2(aChristmasCarol: Rope?, pattern: Pattern): Long {
            val x = System.nanoTime()

            var result = 0
            val m = pattern.matcher(aChristmasCarol)
            while (m.find()) ++result

            val y = System.nanoTime()
            System.out.printf("[Rope]         Executed regexp test in % ,18d ns. Found %d matches.\n", (y - x), result)
            return (y - x)
        }

        private fun ropeRebalancedRegexpTest2(aChristmasCarol: Rope?, pattern: Pattern): Long {
            val x = System.nanoTime()

            val adaptedRope: CharSequence =
                aChristmasCarol!!.rebalance() //Rope.BUILDER.buildForRegexpSearching(aChristmasCarol);
            var result = 0
            val m = pattern.matcher(adaptedRope)
            while (m.find()) ++result

            val y = System.nanoTime()
            System.out.printf("[Reblncd Rope] Executed regexp test in % ,18d ns. Found %d matches.\n", (y - x), result)
            return (y - x)
        }

        private fun ropeMatcherRegexpTest2(aChristmasCarol: Rope?, pattern: Pattern): Long {
            val x = System.nanoTime()

            var result = 0
            val m = aChristmasCarol!!.matcher(pattern)
            while (m.find()) ++result

            val y = System.nanoTime()
            System.out.printf("[Rope.matcher] Executed regexp test in % ,18d ns. Found %d matches.\n", (y - x), result)
            return (y - x)
        }

        private fun time(x: Long, y: Long): String {
            return (y - x).toString() + "ns"
        }

        private fun stat(stats: LongArray, prefix: String) {
            if (stats.size < 3) System.err.println("Cannot print stats.")
            Arrays.sort(stats)

            val median =
                (if ((stats.size and 1) == 1) stats[stats.size shr 1] else (stats[stats.size shr 1] + stats[1 + (stats.size shr 1)]) / 2).toDouble()
            var average = 0.0
            for (j in 1 until stats.size - 1) {
                average += stats[j].toDouble()
            }
            average /= (stats.size - 2).toDouble()
            System.out.printf("%-14s Average=% ,16.0f %s Median=% ,16.0f%s\n", prefix, average, "ns", median, "ns")
        }

        val aChristmasCarolRaw = readpath("test-files/AChristmasCarol_CharlesDickens.txt")
        val bensAutoRaw = readpath("test-files/AutobiographyOfBenjaminFranklin_BenjaminFranklin.txt")
        val aChristmasCarol = String(aChristmasCarolRaw)
        val bensAuto = String(bensAutoRaw)
    }
}
