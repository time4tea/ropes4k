/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.IOException
import java.io.Writer
import java.util.Arrays
import kotlin.math.min

/**
 * A rope constructed from a character array. This rope is even
 * flatter than a regular flat rope.
 *
 * @author Amin Ahmad
 */
class FlatCharArrayRope @JvmOverloads
constructor(
    sequence: CharArray,
    offset: Int = 0,
    length: Int = sequence.size
) : AbstractRope(), FlatRope {
    private val sequence: CharArray

    /**
     * Constructs a new rope from a character array range.
     *
     * @param sequence the character array.
     * @param offset   the offset in the array.
     * @param length   the length of the array.
     */
    /**
     * Constructs a new rope from a character array.
     *
     * @param sequence the character array.
     */
    init {
        require(length <= sequence.size) { "Length must be less than " + sequence.size }
        this.sequence = CharArray(length)
        System.arraycopy(sequence, offset, this.sequence, 0, length)
    }

    override fun get(index: Int): Char {
        return sequence[index]
    }

    override fun depth(): Int {
        return 0
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    override fun indexOf(ch: Char): Int {
        for (j in sequence.indices) if (sequence[j] == ch) return j
        return -1
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    override fun indexOf(ch: Char, fromIndex: Int): Int {
        if (fromIndex < 0 || fromIndex >= length) throw IndexOutOfBoundsException("Rope index out of range: $fromIndex")
        for (j in fromIndex until sequence.size) if (sequence[j] == ch) return j
        return -1
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    public override fun indexOf(sequence: CharSequence, fromIndex: Int): Int {
        val me = getForSequentialAccess()

        // Implementation of Boyer-Moore-Horspool algorithm with
        // special support for unicode.

        // step 0. sanity check.
        val sequenceLength = sequence.length
        if (sequenceLength == 0) return -1
        if (sequenceLength == 1) return indexOf(sequence[0], fromIndex)

        val bcs = IntArray(256) // bad character shift
        Arrays.fill(bcs, sequenceLength)

        // step 1. preprocessing.
        for (j in 0 until sequenceLength - 1) {
            val c = sequence[j]
            val l = (c.code and 0xFF)
            bcs[l] = min((sequenceLength - j - 1), bcs[l])
        }

        // step 2. search.
        var j = fromIndex + sequenceLength - 1
        while (j < length) {
            var x = j
            var y = sequenceLength - 1
            while (true) {
                val c = me[x]
                if (sequence[y] != c) {
                    j += bcs[me[j].code and 0xFF]
                    break
                }
                if (y == 0) return x
                --x
                --y
            }
        }

        return -1
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = start

            override fun hasNext(): Boolean {
                return current < length
            }

            override fun next(): Char {
                return sequence[current++]
            }
        }
    }

    override val length: Int get() = sequence.size

    override fun reverse(): Rope {
        return ReverseRope(this)
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = length - start

            override fun hasNext(): Boolean {
                return current > 0
            }

            override fun next(): Char {
                return sequence[--current]
            }
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Rope {
        if (startIndex == 0 && endIndex == length) return this
        return if (endIndex - startIndex < 16) {
            FlatCharArrayRope(sequence, startIndex, endIndex - startIndex)
        } else {
            SubstringRope(this, startIndex, endIndex - startIndex)
        }
    }

    override fun toString(): String {
        return String(sequence)
    }


    override fun toString(offset: Int, length: Int): String {
        return String(sequence, offset, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        write(out, 0, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        out.write(sequence, offset, length)
    }

}
