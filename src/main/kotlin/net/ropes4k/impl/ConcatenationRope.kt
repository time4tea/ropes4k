/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import net.ropes4k.impl.RopeUtilities.Companion.concatenate
import net.ropes4k.impl.RopeUtilities.Companion.depth
import net.ropes4k.impl.RopeUtilities.Companion.rebalance
import java.io.IOException
import java.io.Writer
import kotlin.math.max

/**
 * A rope that represents the concatenation of two other ropes.
 */
internal class ConcatenationRope(
    val left: Rope,
    val right: Rope
) : AbstractRope() {
    private val depth = max(depth(left), depth(right)) + 1


    override val length: Int = left.length + right.length

    override fun get(index: Int): Char {
        if (index >= length) throw IndexOutOfBoundsException("Rope index out of range: $index")

        return (if (index < left.length) left[index] else right[index - left.length])
    }

    override fun depth(): Int = depth

    /*
     * Returns this object as a char sequence optimized for
     * regular expression searches.
     * <p>
     */
    public override fun getForSequentialAccess(): CharSequence {
        return SequentialRopeAccessor(this)
    }

    class SequentialRopeAccessor(private val rope: Rope) : CharSequence {
        private val iterator = rope.iterator(0) as ConcatenationRopeIterator

        override fun get(index: Int): Char {
            if (index > iterator.pos) {
                iterator.skip(index - iterator.pos - 1)
                try {
                    return iterator.next()
                } catch (e: NoSuchElementException) {
                    throw IndexOutOfBoundsException("Index $index >= length $length")
                }
            } else { /* if (index <= lastIndex) */
                val toMoveBack = iterator.pos - index + 1
                if (iterator.canMoveBackwards(toMoveBack)) {
                    iterator.moveBackwards(toMoveBack)
                    return iterator.next()
                } else {
                    return rope[index]
                }
            }
        }

        override val length: Int get() = rope.length

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return rope.subSequence(startIndex, endIndex)
        }
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Start $start < 0 or > length $length")
        return if (start >= left.length) {
            right.iterator(start - left.length)
        } else {
            ConcatenationRopeIterator(this, start)
        }
    }

    override fun rebalance(): Rope {
        return rebalance(this)
    }

    override fun reverse(): Rope {
        return concatenate(right.reverse(), left.reverse())
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Start $start < 0 or > length $length")
        return if (start >= right.length) {
            left.reverseIterator(start - right.length)
        } else {
            ConcatenationRopeReverseIterator(this, start)
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Rope {
        require(!(startIndex < 0 || endIndex > length)) { "Start/End ($startIndex/$endIndex) out of bounds (0/$length)" }
        if (startIndex == 0 && endIndex == length) return this
        val l = left.length
        if (endIndex <= l) return left.subSequence(startIndex, endIndex)
        if (startIndex >= l) return right.subSequence(startIndex - l, endIndex - l)
        return concatenate(
            left.subSequence(startIndex, l), right.subSequence(0, endIndex - l)
        )
    }

    @Throws(IOException::class)
    override fun write(out: Writer) {
        left.write(out)
        right.write(out)
    }

    @Throws(IOException::class)
    override fun write(out: Writer, offset: Int, length: Int) {
        if (offset + length <= left.length) {
            left.write(out, offset, length)
        } else if (offset >= left.length) {
            right.write(out, offset - left.length, length)
        } else {
            val writeLeft = left.length - offset
            left.write(out, offset, writeLeft)
            right.write(out, 0, length - writeLeft)
        }
    }
}
