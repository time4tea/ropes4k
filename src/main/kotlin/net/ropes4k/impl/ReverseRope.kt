/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import net.ropes4k.impl.RopeUtilities.Companion.depth
import java.io.IOException
import java.io.Writer

/**
 * A rope representing the reversal of character sequence.
 * Internal implementation only.
 *
 * @author Amin Ahmad
 */
class ReverseRope
/**
 * Constructs a new rope from an underlying rope.
 *
 *
 * Balancing algorithm works optimally when only FlatRopes or
 * SubstringRopes are supplied. Framework must guarantee this
 * as no runtime check is performed.
 */(private val rope: Rope) : AbstractRope() {
    override fun get(index: Int): Char {
        return rope[length - index - 1]
    }

    override fun depth(): Int {
        return depth(rope)
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : MutableIterator<Char> {
            var current: Int = start

            override fun hasNext(): Boolean {
                return current < length
            }

            override fun next(): Char {
                return get(current++)
            }

            override fun remove() {
                throw UnsupportedOperationException("Rope iterator is read-only.")
            }
        }
    }

    override val length: Int get() = rope.length

    override fun reverse(): Rope {
        return rope
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : MutableIterator<Char> {
            var current: Int = length - start

            override fun hasNext(): Boolean {
                return current > 0
            }

            override fun next(): Char {
                return get(--current)
            }

            override fun remove() {
                throw UnsupportedOperationException("Rope iterator is read-only.")
            }
        }
    }

    override fun subSequence(start: Int, end: Int): Rope {
        if (start == 0 && end == length) return this
        return rope.subSequence(length - end, length - start).reverse()
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
