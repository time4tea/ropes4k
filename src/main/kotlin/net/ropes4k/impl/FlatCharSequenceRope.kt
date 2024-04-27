/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.IOException
import java.io.Writer
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A rope constructed from an underlying character sequence.
 */
internal class FlatCharSequenceRope(private val sequence: CharSequence) : AbstractRope(), FlatRope {
    override fun get(index: Int): Char {
        return sequence[index]
    }

    override fun depth(): Int {
        return 0
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = start

            override fun hasNext(): Boolean {
                return current < length
            }

            override fun next(): Char {
                if ( current < length ) {
                    return sequence[current++]
                }
                throw NoSuchElementException("Iterator is at end of Rope at $current")
            }
        }
    }

    override val length: Int
        get() = sequence.length

    override fun matcher(pattern: Pattern): Matcher {
        // optimized to return a matcher directly on the underlying sequence.
        return pattern.matcher(sequence)
    }

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
        return if (endIndex - startIndex < 8 || sequence is String /* special optimization for String */) {
            FlatCharSequenceRope(sequence.subSequence(startIndex, endIndex))
        } else {
            SubstringRope(this, startIndex, endIndex - startIndex)
        }
    }

    override fun toString(): String {
        return sequence.toString()
    }

    override fun toString(offset: Int, length: Int): String {
        return sequence.subSequence(offset, offset + length).toString()
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        write(out, 0, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        if (offset < 0 || offset + length > this.length) throw IndexOutOfBoundsException("Rope index out of bounds:" + (if (offset < 0) offset else offset + length))

        if (sequence is String) {    // optimization for String
            out.write(sequence.substring(offset, offset + length))
            return
        }
        for (j in offset until offset + length) out.write(sequence[j].code)
    }
}
