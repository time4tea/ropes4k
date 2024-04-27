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
 *
 * @author Amin Ahmad
 */
class ConcatenationRope(
    @JvmField val left: Rope,
    @JvmField val right: Rope
) : AbstractRope() {
    private val depth = max(depth(left), depth(right)) + 1


    override val length: Int = left.length + right.length

    override fun get(index: Int): Char {
        if (index >= length) throw IndexOutOfBoundsException("Rope index out of range: $index")

        return (if (index < left.length) left[index] else right[index - left.length])
    }

    override fun depth(): Int = depth

    public override fun getForSequentialAccess(): CharSequence {
        return getForSequentialAccess(this)
    }

    /*
     * Returns this object as a char sequence optimized for
     * regular expression searches.
     * <p>
     */
    private fun getForSequentialAccess(rope: Rope): CharSequence {
        return object : CharSequence {
            private val iterator = rope.iterator(0) as ConcatenationRopeIteratorImpl

            override fun get(index: Int): Char {
                if (index > iterator.pos) {
                    iterator.skip(index - iterator.pos - 1)
                    try {
                        return iterator.next()
                    } catch (e: IllegalArgumentException) {
                        println("Rope length is: " + rope.length + " charAt is " + index)
                        throw e
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
    }

    override fun iterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return if (start >= left.length) {
            right.iterator(start - left.length)
        } else {
            ConcatenationRopeIteratorImpl(this, start)
        }
    }

    override fun rebalance(): Rope {
        return rebalance(this)
    }

    override fun reverse(): Rope {
        return concatenate(right.reverse(), left.reverse())
    }

    override fun reverseIterator(start: Int): Iterator<Char> {
        if (start < 0 || start > length) throw IndexOutOfBoundsException("Rope index out of range: $start")
        return if (start >= right.length) {
            left.reverseIterator(start - right.length)
        } else {
            ConcatenationRopeReverseIteratorImpl(this, start)
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): Rope {
        require(!(startIndex < 0 || endIndex > length)) { "Illegal subsequence ($startIndex,$endIndex)" }
        if (startIndex == 0 && endIndex == length) return this
        val l = left.length
        if (endIndex <= l) return left.subSequence(startIndex, endIndex)
        if (startIndex >= l) return right.subSequence(startIndex - l, endIndex - l)
        return concatenate(
            left.subSequence(startIndex, l),
            right.subSequence(0, endIndex - l)
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