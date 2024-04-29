/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import java.io.IOException
import java.io.Writer

/**
 * A rope representing the reversal of character sequence.
 */

internal class ReverseRope(private val rope: InternalRope) : AbstractRope() {

    override val depth: Int = rope.depth
    override val length = rope.length

    override fun get(index: Int): Char {
        return rope[length - index - 1]
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = start

            override fun hasNext(): Boolean {
                return current < length
            }

            override fun next(): Char {
                return get(current++)
            }
        }
    }


    override fun reverse(): InternalRope {
        return rope
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            var current: Int = length - start

            override fun hasNext(): Boolean {
                return current > 0
            }

            override fun next(): Char {
                return get(--current)
            }
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): InternalRope {
        if (startIndex == 0 && endIndex == length) return this
        return rope.subSequence(length - endIndex, length - startIndex).reverse()
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        write(out, 0, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        if (offset < 0 || offset + length > length) throw IndexOutOfBoundsException("Rope index out of bounds:" + (if (offset < 0) offset else offset + length))
        for (j in offset until offset + length) out.write(get(j).code)
    }
}
