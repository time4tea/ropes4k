/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.IOException
import java.io.ObjectStreamException
import java.io.Serial
import java.io.StringWriter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.min

internal abstract class AbstractRope : InternalRope {
    private var hashCode = 0

    override val depth = 0

    override fun append(c: Char): Rope {
        return concatenate(this, FlatCharSequenceRope(c.toString()))
    }

    override fun append(chars: CharSequence): Rope {
        return concatenate(this, FlatCharSequenceRope(chars))
    }

    override fun append(chars: CharSequence, start: Int, end: Int): Rope {
        return concatenate(this, FlatCharSequenceRope(chars).subSequence(start, end))
    }

    override fun compareTo(other: CharSequence): Int {
        val compareTill: Int = min(other.length, length)
        val i = iterator()
        for (j in 0 until compareTill) {
            val x = i.next()
            val y = other[j]
            if (x != y) return x.code - y.code
        }
        return length - other.length
    }

    override fun delete(start: Int, end: Int): Rope {
        if (start == end) return this
        return subSequence(0, start).append(subSequence(end, length))
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rope) {
            if (other.hashCode() != hashCode() || other.length != length) return false
            val i1 = iterator()
            val i2: Iterator<Char> = other.iterator()

            while (i1.hasNext()) {
                val a = i1.next()
                val b = i2.next()
                if (a != b) return false
            }
            return true
        }
        return false
    }

    /**
     * A utility method that returns an instance of this rope optimized
     * for sequential access.
     */
    protected open fun getForSequentialAccess(): CharSequence = this

    override fun hashCode(): Int {
        if (hashCode == 0 && length > 0) {
            if (length < 6) {
                for (c in this) {
                    hashCode = 31 * hashCode + c.code
                }
            } else {
                val i = iterator()
                for (j in 0..4) hashCode = 31 * hashCode + i.next().code
                hashCode = 31 * hashCode + get(length - 1).code
            }
        }
        return hashCode
    }

    override fun indexOf(ch: Char, fromIndex: Int): Int {
        if (fromIndex < 0 || fromIndex >= length) throw IndexOutOfBoundsException("Rope index out of range: $fromIndex")
        var index = fromIndex - 1
        val i = iterator(fromIndex)
        while (i.hasNext()) {
            ++index
            if (i.next() == ch) return index
        }
        return -1
    }

    override fun startsWith(prefix: CharSequence, offset: Int): Boolean {
        if (offset < 0 || offset > length) throw IndexOutOfBoundsException("Rope offset out of range: $offset")
        if (offset + prefix.length > length) return false

        var x = 0
        val i = iterator(offset)
        while (i.hasNext() && x < prefix.length) {
            if (i.next() != prefix[x++]) return false
        }
        return true
    }

    override fun endsWith(suffix: CharSequence, offset: Int): Boolean {
        return startsWith(suffix, length - suffix.length - offset)
    }

    override fun indexOf(sequence: CharSequence, fromIndex: Int): Int {
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

    override fun insert(at: Int, chars: CharSequence): Rope {
        val r = Rope.of(chars)

        if (at == 0) return r.append(this)
        else if (at == length) return append(r)
        else if (at < 0 || at > length) throw IndexOutOfBoundsException("$at is out of insert range [0:$length]")
        return subSequence(0, at).append(r).append(subSequence(at, length))
    }

    override fun iterator(): Iterator<Char> {
        return iterator(0)
    }

    override fun trimStart(): Rope {
        var index = -1
        for (c in this) {
            ++index
            if (c.code > 0x20 && !Character.isWhitespace(c)) break
        }
        return if (index <= 0) this
        else subSequence(index, length)
    }

    override fun matcher(pattern: Pattern): Matcher {
        return pattern.matcher(getForSequentialAccess())
    }

    override fun rebalance(): Rope {
        return this
    }

    override fun trimEnd(): Rope {
        var index = length + 1
        val i = reverseIterator()
        while (i.hasNext()) {
            val c = i.next()
            --index
            if (c.code > 0x20 && !Character.isWhitespace(c)) break
        }
        return if (index >= length) this
        else subSequence(0, index)
    }

    override fun toString(): String {
        val out = StringWriter(length)
        try {
            write(out)
            out.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return out.toString()
    }

    override fun trim(): Rope {
        return trimStart().trimEnd()
    }

    @Serial
    @Throws(ObjectStreamException::class)
    fun writeReplace(): Any {
        return SerializedRope(this)
    }

    override fun padStart(toLength: Int, padChar: Char): Rope {
        val toPad = toLength - length
        if (toPad < 1) return this
        return concatenate(
            FlatCharSequenceRope(RepeatedCharacterSequence(padChar, toPad)),
            this
        )
    }

    override fun padEnd(toLength: Int, padChar: Char): Rope {
        val toPad = toLength - length
        if (toPad < 1) return this
        return concatenate(
            this,
            FlatCharSequenceRope(RepeatedCharacterSequence(padChar, toPad))
        )
    }
}
