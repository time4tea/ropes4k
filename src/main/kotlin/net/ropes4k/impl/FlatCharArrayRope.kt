/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import java.io.IOException
import java.io.Writer

internal class FlatCharArrayRope(
    chars: CharArray,
    offset: Int = 0,
    length: Int = chars.size
) : AbstractRope(), FlatRope {
    private val sequence: CharArray

    init {
        require(length <= chars.size) { "Length must be less than " + chars.size }
        this.sequence = CharArray(length)
        System.arraycopy(chars, offset, this.sequence, 0, length)
    }

    override val length = this.sequence.size

    override fun get(index: Int): Char {
        return sequence[index]
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

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = start

            override fun hasNext(): Boolean {
                return current < length
            }

            override fun next(): Char {
                if (current < length) {
                    return sequence[current++]
                }
                throw NoSuchElementException("Iterator is at end of Rope at $current")
            }
        }
    }


    override fun reverse(): InternalRope {
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

    override fun subSequence(startIndex: Int, endIndex: Int): InternalRope {
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
