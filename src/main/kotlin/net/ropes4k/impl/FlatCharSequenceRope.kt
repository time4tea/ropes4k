/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import java.io.IOException
import java.io.Writer
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A rope constructed from an underlying character sequence.
 */
internal class FlatCharSequenceRope(private val chars: CharSequence) : AbstractRope(), FlatRope {

    override val length = chars.length

    override fun get(index: Int): Char {
        return chars[index]
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
                    return chars[current++]
                }
                throw NoSuchElementException("Iterator is at end of Rope at $current")
            }
        }
    }


    override fun matcher(pattern: Pattern): Matcher {
        // optimized to return a matcher directly on the underlying sequence.
        return pattern.matcher(chars)
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
                return chars[--current]
            }
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): InternalRope {
        if (startIndex == 0 && endIndex == length) return this
        return if (endIndex - startIndex < 8 || chars is String /* special optimization for String */) {
            FlatCharSequenceRope(chars.subSequence(startIndex, endIndex))
        } else {
            SubstringRope(this, startIndex, endIndex - startIndex)
        }
    }

    override fun toString(): String {
        return chars.toString()
    }

    override fun toString(offset: Int, length: Int): String {
        return chars.subSequence(offset, offset + length).toString()
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        write(out, 0, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        if (offset < 0 || offset + length > this.length) throw IndexOutOfBoundsException("Rope index out of bounds:" + (if (offset < 0) offset else offset + length))

        if (chars is String) {    // optimization for String
            out.write(chars.substring(offset, offset + length))
            return
        }
        for (j in offset until offset + length) out.write(chars[j].code)
    }
}
