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
 * Represents a lazily-evaluated substring of another rope. For performance
 * reasons, the target rope must be a `FlatRope`.
 */
class SubstringRope(val rope: FlatRope, val offset: Int, override val length: Int) : AbstractRope() {

    init {
        if (length < 0 || offset < 0 || offset + length > rope.length) throw IndexOutOfBoundsException("Invalid substring offset (" + offset + ") and length (" + length + ") for underlying rope with length " + rope.length)
    }

    override fun get(index: Int): Char {
        if (index >= length) throw IndexOutOfBoundsException("Rope index out of range: $index")

        return rope[offset + index]
    }

    override fun depth(): Int {
        return depth(getRope())
    }

    /**
     * Returns the rope underlying this one.
     *
     * @return the rope underlying this one.
     */
    fun getRope(): Rope {
        return rope
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            val u: Iterator<Char> = getRope().iterator(offset + start)
            var position: Int = start

            override fun hasNext(): Boolean {
                return position < length
            }

            override fun next(): Char {
                ++position
                return u.next()
            }
        }
    }

    override fun reverse(): Rope {
        return ReverseRope(this)
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return object : Iterator<Char> {
            val u: Iterator<Char> = getRope().reverseIterator(getRope().length - offset - length + start)
            var position: Int = length - start

            override fun hasNext(): Boolean {
                return position > 0
            }

            override fun next(): Char {
                --position
                return u.next()
            }
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Rope {
        if (startIndex == 0 && endIndex == length) return this
        return SubstringRope(rope, offset + startIndex, endIndex - startIndex)
    }

    override fun toString(): String {
        return rope.toString(offset, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        rope.write(out, offset, length)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        rope.write(out, this.offset + offset, length)
    }
}
