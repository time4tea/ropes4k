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
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.*
import java.util.regex.Pattern

class RopeTest {
    private fun Rope.substring(start: Int, end: Int): String {
        val out = StringWriter(end - start)
        write(out, start, end - start)
        return out.toString()
    }

    @Test
    fun testSubstringDeleteBug() {
        val s = "12345678902234567890"

        var rope = Rope.BUILDER.build(s.toCharArray()) // bugs

        rope = rope.delete(0, 1)

        expectThat(rope.substring(0, 2)).isEqualTo("23")
        expectThat(rope.substring(0, 0)).isEqualTo("")
        expectThat(rope.substring(7, 10)).isEqualTo("902")


        rope = Rope.BUILDER.build(s) // no bugs
        rope = rope.delete(0, 1)
        expectThat(rope.substring(0, 2)).isEqualTo("23")
        expectThat(rope.substring(0, 0)).isEqualTo("")
        expectThat(rope.substring(7, 10)).isEqualTo("902")
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

        expectThat(r.substring(0, 6)).isEqualTo("round ")
        expectThat(r.substring(0, 7)).isEqualTo("round 0")
        expectThat(r.substring(0, 8)).isEqualTo("round 0 ")
        expectThat(r.substring(0, 9)).isEqualTo("round 0 1")
        expectThat(r.substring(0, 10)).isEqualTo("round 0 12")
        expectThat(r.substring(0, 18)).isEqualTo("round 0 1234567890")
        expectThat(r.substring(0, r.length)).isEqualTo("round 0 1234567890")
    }


    @Test
    fun testLengthOverflow() {
        var x1 = Rope.BUILDER.build("01")
        for (j in 2..30) x1 = x1.append(x1)
        expectThat(x1.length).isEqualTo(1073741824)
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
        expectThat(r3.toString()).isEqualTo("alphabeta")

        r1 = Rope.BUILDER.build("The quick brown fox jumped over")
        r3 = r1.append(r1)
        expectThat(r3.toString()).isEqualTo("The quick brown fox jumped overThe quick brown fox jumped over")
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
        expectThat(i.next()).isEqualTo('2')
        Assertions.assertTrue(i.hasNext())
        expectThat(i.next()).isEqualTo('3')
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
        expectThat(i.next()).isEqualTo('6')
        Assertions.assertTrue(i.hasNext())
        expectThat(i.next()).isEqualTo('7')
        Assertions.assertTrue(!i.hasNext())
    }

    @Test
    fun testReverse() {
        val x1: Rope = FlatCharSequenceRope("012345")
        val x2: Rope = FlatCharSequenceRope("67")
        val x3: Rope = ConcatenationRope(x1, x2)

        expectThat(x1.reverse().toString()).isEqualTo("543210")
        expectThat(x3.reverse().toString()).isEqualTo("76543210")
        expectThat(x3.reverse().reverse().reverse()).isEqualTo(x3.reverse())
        expectThat(x3.reverse().subSequence(1, 7).toString()).isEqualTo("654321")
    }

    @Test
    fun testTrim() {
        val x1: Rope = FlatCharSequenceRope("\u0012  012345")
        val x2: Rope = FlatCharSequenceRope("\u0002 67	       \u0007")
        val x3: Rope = ConcatenationRope(x1, x2)

        expectThat(x1.trimStart().toString()).isEqualTo("012345")
        expectThat(x2.trimStart().toString()).isEqualTo("67	       \u0007")
        expectThat(x3.trimStart().toString()).isEqualTo("012345\u0002 67	       \u0007")

        expectThat(x1.trimEnd().toString()).isEqualTo("\u0012  012345")
        expectThat(x2.trimEnd().toString()).isEqualTo("\u0002 67")
        expectThat(x3.trimEnd().toString()).isEqualTo("\u0012  012345\u0002 67")
        expectThat(x3.trimEnd().reverse().trimEnd().reverse().toString()).isEqualTo("012345\u0002 67")

        expectThat(x3.trimEnd().trimStart()).isEqualTo(x3.trimStart().trimEnd())
        expectThat(x3.trimStart().reverse().trimStart().reverse()).isEqualTo(x3.trimStart().trimEnd())
        expectThat(x3.trim()).isEqualTo(x3.trimStart().trimEnd())
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

        expectThat(r3).isEqualTo(r1)
        Assertions.assertFalse(r1 == r2)
    }

    @Test
    fun testHashCode() {
        val r1 = Rope.BUILDER.build("alpha")
        val r2 = Rope.BUILDER.build("beta")
        val r3 = Rope.BUILDER.build("alpha")

        expectThat(r3.hashCode()).isEqualTo(r1.hashCode())
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
        expectThat(r3.indexOf('l')).isEqualTo(1)
        expectThat(r3.indexOf('e')).isEqualTo(6)


        var r = Rope.BUILDER.build("abcdef")
        expectThat(r.indexOf('z')).isEqualTo(-1)
        expectThat(r.indexOf('a')).isEqualTo(0)
        expectThat(r.indexOf('b')).isEqualTo(1)
        expectThat(r.indexOf('f')).isEqualTo(5)


        expectThat(r.indexOf('b', 0)).isEqualTo(1)
        expectThat(r.indexOf('a', 0)).isEqualTo(0)
        expectThat(r.indexOf('z', 0)).isEqualTo(-1)
        expectThat(r.indexOf('b', 2)).isEqualTo(-1)
        expectThat(r.indexOf('f', 5)).isEqualTo(5)

        expectThat(r.indexOf("cd", 1)).isEqualTo(2)

        r = Rope.BUILDER.build("The quick brown fox jumped over the jumpy brown dog.")
        expectThat(r.indexOf("The")).isEqualTo(0)
        expectThat(r.indexOf("brown")).isEqualTo(10)
        expectThat(r.indexOf("brown", 10)).isEqualTo(10)
        expectThat(r.indexOf("brown", 11)).isEqualTo(42)
        expectThat(r.indexOf("brown", 43)).isEqualTo(-1)
        expectThat(r.indexOf("hhe")).isEqualTo(-1)

        r = Rope.BUILDER.build("zbbzzz")
        expectThat(r.indexOf("ab", 1)).isEqualTo(-1)
    }

    @Test
    fun testInsert() {
        val r1 = Rope.BUILDER.build("alpha")
        expectThat(r1.insert(0, "beta").toString()).isEqualTo("betaalpha")
        expectThat(r1.insert(r1.length, "beta").toString()).isEqualTo("alphabeta")
        expectThat(r1.insert(1, "beta").toString()).isEqualTo("abetalpha")
    }

    @Test
    fun testPrepend() {
        var r1 = Rope.BUILDER.build("alphabeta")
        for (j in 0..1) r1 = r1.subSequence(0, 5).append(r1)
        expectThat(r1.toString()).isEqualTo("alphaalphaalphabeta")
        r1 = r1.append(r1.subSequence(5, 15))
        expectThat(r1.toString()).isEqualTo("alphaalphaalphabetaalphaalpha")
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
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('3')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r1.reverseIterator(4)
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r2.reverseIterator()
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('3')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertFalse(x.hasNext())

        x = r2.reverseIterator(4)
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertFalse(x.hasNext())

        x = r3.reverseIterator()
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r3.reverseIterator(1)
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator() //0123443210012
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('3')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('3')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator(7)
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('4')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('3')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('2')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('1')
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
        Assertions.assertFalse(x.hasNext())

        x = r4.reverseIterator(12)
        Assertions.assertTrue(x.hasNext())
        expectThat(x.next() as Char).isEqualTo('0')
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
        expectThat(r.padStart(5).toString()).isEqualTo("hello")
        expectThat(r.padStart(0).toString()).isEqualTo("hello")
        expectThat(r.padStart(-1).toString()).isEqualTo("hello")
        expectThat(r.padStart(6).toString()).isEqualTo(" hello")
        expectThat(r.padStart(7).toString()).isEqualTo("  hello")
        expectThat(r.padStart(6, '~').toString()).isEqualTo("~hello")
        expectThat(r.padStart(7, '~').toString()).isEqualTo("~~hello")
        expectThat(r.padStart(30, '~').toString()).isEqualTo("~~~~~~~~~~~~~~~~~~~~~~~~~hello")
    }

    @Test
    fun testPadEnd() {
        val r = Rope.BUILDER.build("hello")
        expectThat(r.padEnd(5).toString()).isEqualTo("hello")
        expectThat(r.padEnd(0).toString()).isEqualTo("hello")
        expectThat(r.padEnd(-1).toString()).isEqualTo("hello")
        expectThat(r.padEnd(6).toString()).isEqualTo("hello ")
        expectThat(r.padEnd(7).toString()).isEqualTo("hello  ")
        expectThat(r.padEnd(6, '~').toString()).isEqualTo("hello~")
        expectThat(r.padEnd(7, '~').toString()).isEqualTo("hello~~")
        expectThat(r.padEnd(30, '~').toString()).isEqualTo("hello~~~~~~~~~~~~~~~~~~~~~~~~~")
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
        expectThat(r.toString()).isEqualTo("a")
        r = r.append("boy")
        expectThat(r.toString()).isEqualTo("aboy")
        r = r.append("test", 0, 4)
        expectThat(r.toString()).isEqualTo("aboytest")
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

        expectThat(r1[0]).isEqualTo('0')
        expectThat(r1[9]).isEqualTo('9')
        expectThat(r2[0]).isEqualTo('0')
        expectThat(r3[0]).isEqualTo('9')
        expectThat(r4[0]).isEqualTo('0')
        expectThat(r4[9]).isEqualTo('9')
        expectThat(r4[10]).isEqualTo('9')
    }

    @Test
    fun testRegexp() {
        val r = ConcatenationRope(FlatCharSequenceRope("012345"), FlatCharSequenceRope("6789"))
        var c = r.forSequentialAccess
        for (j in 0..9) {
            expectThat(c[j]).isEqualTo(r[j])
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
            expectThat(r1.indexOf(s2)).isEqualTo(s1.indexOf(s2))
        }
        run {
            // extra test, aahmad
            val s1 = "ABABAABBABABBAAABBBAAABABABABBBBAA"
            val s2 = "ABABAB"

            val r1 = Rope.BUILDER.build(s1)
            expectThat(r1.indexOf(s2)).isEqualTo(s1.indexOf(s2))
        }
    }
}
