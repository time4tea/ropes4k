/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.test

import net.ropes4k.Rope
import net.ropes4k.impl.ConcatenationRope
import net.ropes4k.impl.FlatCharSequenceRope
import net.ropes4k.impl.ReverseRope
import net.ropes4k.impl.SubstringRope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.*
import java.util.regex.Pattern

class RopeTest {
    private fun fromRope(rope: Rope, start: Int, end: Int): String? {
        try {
            val out: Writer = StringWriter(end - start)
            rope.write(out, start, end - start)
            return out.toString()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
    }

    @Test
    fun testSubstringDeleteBug() {
        val s = "12345678902234567890"

        var rope = Rope.BUILDER.build(s.toCharArray()) // bugs

        rope = rope.delete(0, 1)
        Assertions.assertEquals("23", fromRope(rope, 0, 2))
        Assertions.assertEquals("", fromRope(rope, 0, 0))
        Assertions.assertEquals("902", fromRope(rope, 7, 10))


        rope = Rope.BUILDER.build(s) // no bugs
        rope = rope.delete(0, 1)
        Assertions.assertEquals("23", fromRope(rope, 0, 2))
        Assertions.assertEquals("", fromRope(rope, 0, 0))
        Assertions.assertEquals("902", fromRope(rope, 7, 10))
    }

    /**
     * Bug reported by ugg.ugg@gmail.com.
     */
    @Test
    fun testRopeWriteBug() {
        var r = Rope.BUILDER.build("")
        r = r.append("round ")
        r = r.append(0.toString())
        r = r.append(" 1234567890")

        Assertions.assertEquals("round ", fromRope(r, 0, 6))
        Assertions.assertEquals("round 0", fromRope(r, 0, 7))
        Assertions.assertEquals("round 0 ", fromRope(r, 0, 8))
        Assertions.assertEquals("round 0 1", fromRope(r, 0, 9))
        Assertions.assertEquals("round 0 12", fromRope(r, 0, 10))
        Assertions.assertEquals("round 0 1234567890", fromRope(r, 0, 18))
        Assertions.assertEquals("round 0 1234567890", fromRope(r, 0, r.length))
    }


    @Test
    fun testLengthOverflow() {
        var x1 = Rope.BUILDER.build("01")
        for (j in 2..30) x1 = x1.append(x1)
        Assertions.assertEquals(1073741824, x1.length)
        try {
            x1 = x1.append(x1)
            Assertions.fail<Any>("Expected overflow.")
        } catch (e: IllegalArgumentException) {
            // this is what we expect
        }
    }

    @Test
    fun testMatches() {
        val x1: Rope = FlatCharSequenceRope("0123456789")
        val x2: Rope = ConcatenationRope(x1, x1)

        Assertions.assertTrue(x2.matches("0.*9"))
        Assertions.assertTrue(x2.matches(Pattern.compile("0.*9")))

        Assertions.assertTrue(x2.matches("0.*90.*9"))
        Assertions.assertTrue(x2.matches(Pattern.compile("0.*90.*9")))
    }

    @Test
    fun testConcatenationFlatFlat() {
        var r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        var r3 = r1.append(r2)
        Assertions.assertEquals("alphabeta", r3.toString())

        r1 = Rope.BUILDER.build("The quick brown fox jumped over")
        r3 = r1.append(r1)
        Assertions.assertEquals("The quick brown fox jumped overThe quick brown fox jumped over", r3.toString())
    }

    @Test
    fun testIterator() {
        val x1: Rope = FlatCharSequenceRope("0123456789")
        val x2: Rope = FlatCharSequenceRope("0123456789")
        val x3: Rope = FlatCharSequenceRope("0123456789")
        val c1 = ConcatenationRope(x1, x2)
        val c2 = ConcatenationRope(c1, x3)

        var i: Iterator<Char> = c2.iterator()
        for (j in 0 until c2.length) {
            Assertions.assertTrue(i.hasNext(), "Has next (" + j + "/" + c2.length + ")")
            i.next()
        }
        Assertions.assertTrue(!i.hasNext())

        val z1 = FlatCharSequenceRope("0123456789")
        val z2: Rope = SubstringRope(z1, 2, 0)
        val z3: Rope = SubstringRope(z1, 2, 2)
        val z4: Rope = ConcatenationRope(z3, SubstringRope(z1, 6, 2)) // 2367

        i = z2.iterator()
        Assertions.assertTrue(!i.hasNext())
        i = z3.iterator()
        Assertions.assertTrue(i.hasNext())
        Assertions.assertEquals('2', i.next())
        Assertions.assertTrue(i.hasNext())
        Assertions.assertEquals('3', i.next())
        Assertions.assertTrue(!i.hasNext())
        for (j in 0..z3.length) {
            try {
                z3.iterator(j)
            } catch (e: Exception) {
                Assertions.fail<Any>("$j $e")
            }
        }
        Assertions.assertTrue(4 == z4.length)
        for (j in 0..z4.length) {
            try {
                z4.iterator(j)
            } catch (e: Exception) {
                Assertions.fail<Any>("$j $e")
            }
        }
        i = z4.iterator(4)
        Assertions.assertTrue(!i.hasNext())
        i = z4.iterator(2)
        Assertions.assertTrue(i.hasNext())
        Assertions.assertEquals('6', i.next())
        Assertions.assertTrue(i.hasNext())
        Assertions.assertEquals('7', i.next())
        Assertions.assertTrue(!i.hasNext())
    }

    @Test
    fun testReverse() {
        val x1: Rope = FlatCharSequenceRope("012345")
        val x2: Rope = FlatCharSequenceRope("67")
        val x3: Rope = ConcatenationRope(x1, x2)

        Assertions.assertEquals("543210", x1.reverse().toString())
        Assertions.assertEquals("76543210", x3.reverse().toString())
        Assertions.assertEquals(x3.reverse(), x3.reverse().reverse().reverse())
        Assertions.assertEquals("654321", x3.reverse().subSequence(1, 7).toString())
    }

    @Test
    fun testTrim() {
        val x1: Rope = FlatCharSequenceRope("\u0012  012345")
        val x2: Rope = FlatCharSequenceRope("\u0002 67	       \u0007")
        val x3: Rope = ConcatenationRope(x1, x2)

        Assertions.assertEquals("012345", x1.trimStart().toString())
        Assertions.assertEquals("67	       \u0007", x2.trimStart().toString())
        Assertions.assertEquals("012345\u0002 67	       \u0007", x3.trimStart().toString())

        Assertions.assertEquals("\u0012  012345", x1.trimEnd().toString())
        Assertions.assertEquals("\u0002 67", x2.trimEnd().toString())
        Assertions.assertEquals("\u0012  012345\u0002 67", x3.trimEnd().toString())
        Assertions.assertEquals("012345\u0002 67", x3.trimEnd().reverse().trimEnd().reverse().toString())

        Assertions.assertEquals(x3.trimStart().trimEnd(), x3.trimEnd().trimStart())
        Assertions.assertEquals(x3.trimStart().trimEnd(), x3.trimStart().reverse().trimStart().reverse())
        Assertions.assertEquals(x3.trimStart().trimEnd(), x3.trim())
    }

    @Test
    fun testCreation() {
        try {
            Rope.BUILDER.build("The quick brown fox jumped over")
        } catch (e: Exception) {
            Assertions.fail<Any>("Nonempty string: " + e.message)
        }
        try {
            Rope.BUILDER.build("")
        } catch (e: Exception) {
            Assertions.fail<Any>("Empty string: " + e.message)
        }
    }

    @Test
    fun testEquals() {
        val r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        val r3 = Rope.BUILDER.build("alpha")

        Assertions.assertEquals(r1, r3)
        Assertions.assertFalse(r1 == r2)
    }

    @Test
    fun testHashCode() {
        val r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        val r3 = Rope.BUILDER.build("alpha")

        Assertions.assertEquals(r1.hashCode(), r3.hashCode())
        Assertions.assertFalse(r1.hashCode() == r2.hashCode())
    }

    @Test
    fun testHashCode2() {
        val r1: Rope = FlatCharSequenceRope(StringBuffer("The quick brown fox."))
        val r2: Rope = ConcatenationRope(FlatCharSequenceRope(""), FlatCharSequenceRope("The quick brown fox."))

        Assertions.assertTrue(r1 == r2)
        Assertions.assertTrue(r1 == r2)
    }

    @Test
    fun testIndexOf() {
        val r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        val r3 = r1.append(r2)
        Assertions.assertEquals(1, r3.indexOf('l'))
        Assertions.assertEquals(6, r3.indexOf('e'))


        var r = Rope.BUILDER.build("abcdef")
        Assertions.assertEquals(-1, r.indexOf('z'))
        Assertions.assertEquals(0, r.indexOf('a'))
        Assertions.assertEquals(1, r.indexOf('b'))
        Assertions.assertEquals(5, r.indexOf('f'))


        Assertions.assertEquals(1, r.indexOf('b', 0))
        Assertions.assertEquals(0, r.indexOf('a', 0))
        Assertions.assertEquals(-1, r.indexOf('z', 0))
        Assertions.assertEquals(-1, r.indexOf('b', 2))
        Assertions.assertEquals(5, r.indexOf('f', 5))

        Assertions.assertEquals(2, r.indexOf("cd", 1))

        r = Rope.BUILDER.build("The quick brown fox jumped over the jumpy brown dog.")
        Assertions.assertEquals(0, r.indexOf("The"))
        Assertions.assertEquals(10, r.indexOf("brown"))
        Assertions.assertEquals(10, r.indexOf("brown", 10))
        Assertions.assertEquals(42, r.indexOf("brown", 11))
        Assertions.assertEquals(-1, r.indexOf("brown", 43))
        Assertions.assertEquals(-1, r.indexOf("hhe"))

        r = Rope.BUILDER.build("zbbzzz")
        Assertions.assertEquals(-1, r.indexOf("ab", 1))
    }

    @Test
    fun testInsert() {
        val r1 = Rope.BUILDER.build("alpha")
        Assertions.assertEquals("betaalpha", r1.insert(0, "beta").toString())
        Assertions.assertEquals("alphabeta", r1.insert(r1.length, "beta").toString())
        Assertions.assertEquals("abetalpha", r1.insert(1, "beta").toString())
    }

    @Test
    fun testPrepend() {
        var r1 = Rope.BUILDER.build("alphabeta")
        for (j in 0..1) r1 = r1.subSequence(0, 5).append(r1)
        Assertions.assertEquals("alphaalphaalphabeta", r1.toString())
        r1 = r1.append(r1.subSequence(5, 15))
        Assertions.assertEquals("alphaalphaalphabetaalphaalpha", r1.toString())
    }

    @Test
    fun testCompareTo() {
        val r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        val r3 = Rope.BUILDER.build("alpha")
        val r4 = Rope.BUILDER.build("alpha1")
        val s2 = "beta"

        Assertions.assertTrue(r1.compareTo(r3) == 0)
        Assertions.assertTrue(r1.compareTo(r2) < 0)
        Assertions.assertTrue(r2.compareTo(r1) > 0)
        Assertions.assertTrue(r1.compareTo(r4) < 0)
        Assertions.assertTrue(r4.compareTo(r1) > 0)
        Assertions.assertTrue(r1.compareTo(s2) < 0)
        Assertions.assertTrue(r2.compareTo(s2) == 0)
    }

    @Test
    fun testToString() {
        val phrase = "The quick brown fox jumped over the lazy brown dog. Boy am I glad the dog was asleep."
        val r1 = Rope.BUILDER.build(phrase)
        Assertions.assertTrue(phrase == r1.toString())
        Assertions.assertTrue(phrase.subSequence(7, 27) == r1.subSequence(7, 27).toString())
    }

    @Test
    fun testReverseIterator() {
        val r1 = FlatCharSequenceRope("01234")
        val r2 = ReverseRope(r1)
        val r3 = SubstringRope(r1, 0, 3)
        val r4 = ConcatenationRope(ConcatenationRope(r1, r2), r3) //0123443210012

        var x = r1.reverseIterator()
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('3', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r1.reverseIterator(4)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r2.reverseIterator()
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('3', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r2.reverseIterator(4)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r3.reverseIterator()
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r3.reverseIterator(1)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator() //0123443210012
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('3', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('3', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator(7)
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('4', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('3', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('2', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('1', x.next() as Char)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator(12)
        Assertions.assertTrue(x.hasNext())
        Assertions.assertEquals('0', x.next() as Char)
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator(13)
        Assertions.assertFalse(x.hasNext())
    }

    @Test
    fun testSerialize() {
        val r1 = FlatCharSequenceRope("01234")
        val r2 = ReverseRope(r1)
        val r3 = SubstringRope(r1, 0, 1)
        val r4 = ConcatenationRope(ConcatenationRope(r1, r2), r3) //01234432100

        val out = ByteArrayOutputStream()
        try {
            val oos = ObjectOutputStream(out)
            oos.writeObject(r4)
            oos.close()
            val `in` = ByteArrayInputStream(out.toByteArray())
            val ois = ObjectInputStream(`in`)
            val r = ois.readObject() as Rope
            Assertions.assertTrue(r is FlatCharSequenceRope)
        } catch (e: Exception) {
            Assertions.fail<Any>(e.toString())
        }
    }

    @Test
    fun testPadStart() {
        val r = Rope.BUILDER.build("hello")
        Assertions.assertEquals("hello", r.padStart(5).toString())
        Assertions.assertEquals("hello", r.padStart(0).toString())
        Assertions.assertEquals("hello", r.padStart(-1).toString())
        Assertions.assertEquals(" hello", r.padStart(6).toString())
        Assertions.assertEquals("  hello", r.padStart(7).toString())
        Assertions.assertEquals("~hello", r.padStart(6, '~').toString())
        Assertions.assertEquals("~~hello", r.padStart(7, '~').toString())
        Assertions.assertEquals("~~~~~~~~~~~~~~~~~~~~~~~~~hello", r.padStart(30, '~').toString())
    }

    @Test
    fun testPadEnd() {
        val r = Rope.BUILDER.build("hello")
        Assertions.assertEquals("hello", r.padEnd(5).toString())
        Assertions.assertEquals("hello", r.padEnd(0).toString())
        Assertions.assertEquals("hello", r.padEnd(-1).toString())
        Assertions.assertEquals("hello ", r.padEnd(6).toString())
        Assertions.assertEquals("hello  ", r.padEnd(7).toString())
        Assertions.assertEquals("hello~", r.padEnd(6, '~').toString())
        Assertions.assertEquals("hello~~", r.padEnd(7, '~').toString())
        Assertions.assertEquals("hello~~~~~~~~~~~~~~~~~~~~~~~~~", r.padEnd(30, '~').toString())
    }

    @Test
    fun testSubstringBounds() {
        val r =
            Rope.BUILDER.build("01234567890123456789012345678901234567890123456789012345678901234567890123456789".toCharArray())
        val r2 = r.subSequence(0, 30)
        try {
            r2[31]
            Assertions.fail<Any>("Expected IndexOutOfBoundsException")
        } catch (e: IndexOutOfBoundsException) {
            // success
        }
    }

    @Test
    fun testAppend() {
        var r = Rope.BUILDER.build("")
        r = r.append('a')
        Assertions.assertEquals("a", r.toString())
        r = r.append("boy")
        Assertions.assertEquals("aboy", r.toString())
        r = r.append("test", 0, 4)
        Assertions.assertEquals("aboytest", r.toString())
    }

    @Test
    fun testEmpty() {
        val r1 = Rope.BUILDER.build("")
        val r2 = Rope.BUILDER.build("012345")

        Assertions.assertTrue(r1.isEmpty())
        Assertions.assertFalse(r2.isEmpty())
        Assertions.assertTrue(r2.subSequence(2, 2).isEmpty())
    }

    @Test
    fun testCharAt() {
        val r1 = FlatCharSequenceRope("0123456789")
        val r2 = SubstringRope(r1, 0, 1)
        val r3 = SubstringRope(r1, 9, 1)
        val r4 = ConcatenationRope(r1, r3)

        Assertions.assertEquals('0', r1[0])
        Assertions.assertEquals('9', r1[9])
        Assertions.assertEquals('0', r2[0])
        Assertions.assertEquals('9', r3[0])
        Assertions.assertEquals('0', r4[0])
        Assertions.assertEquals('9', r4[9])
        Assertions.assertEquals('9', r4[10])
    }

    @Test
    fun testRegexp() {
        val r = ConcatenationRope(FlatCharSequenceRope("012345"), FlatCharSequenceRope("6789"))
        var c = r.forSequentialAccess
        for (j in 0..9) {
            Assertions.assertEquals(r[j], c[j])
        }
        c = r.forSequentialAccess

        val indices = intArrayOf(1, 2, 1, 3, 5, 0, 6, 7, 8, 1, 7, 7, 7)
        for (i in indices) {
            Assertions.assertEquals(r[i], c[i], "Index: $i")
        }
    }

    @Test
    fun testStartsEndsWith() {
        val r = Rope.BUILDER.build("Hello sir, how do you do?")
        Assertions.assertTrue(r.startsWith(""))
        Assertions.assertTrue(r.startsWith("H"))
        Assertions.assertTrue(r.startsWith("He"))
        Assertions.assertTrue(r.startsWith("Hello "))
        Assertions.assertTrue(r.startsWith("", 0))
        Assertions.assertTrue(r.startsWith("H", 0))
        Assertions.assertTrue(r.startsWith("He", 0))
        Assertions.assertTrue(r.startsWith("Hello ", 0))
        Assertions.assertTrue(r.startsWith("", 1))
        Assertions.assertTrue(r.startsWith("e", 1))
        Assertions.assertTrue(r.endsWith("?"))
        Assertions.assertTrue(r.endsWith("do?"))
        Assertions.assertTrue(r.endsWith("o", 1))
        Assertions.assertTrue(r.endsWith("you do", 1))
    }

    /**
     * Reported by Blake Watkins <blakewatkins></blakewatkins>@gmail.com> on
     * 21 Mar 2009.
     */
    @Test
    fun testIndexOfBug() {
        run {
            // original test, bwatkins
            val s1 = "CCCCCCPIFPCFFP"
            val s2 = "IFPCFFP"

            val r1 = Rope.BUILDER.build(s1)
            Assertions.assertEquals(s1.indexOf(s2), r1.indexOf(s2))
        }
        run {
            // extra test, aahmad
            val s1 = "ABABAABBABABBAAABBBAAABABABABBBBAA"
            val s2 = "ABABAB"

            val r1 = Rope.BUILDER.build(s1)
            Assertions.assertEquals(s1.indexOf(s2), r1.indexOf(s2))
        }
    }
}
