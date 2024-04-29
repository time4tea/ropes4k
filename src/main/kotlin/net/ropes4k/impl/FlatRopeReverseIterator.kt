/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

internal class FlatRopeReverseIterator(private val flat: FlatRope, start: Int) : Iterator<Char> {
    private var current: Int = flat.length - start

    init {
        if (start < 0 || start > flat.length) throw IndexOutOfBoundsException("Rope index out of range: $start")
    }

    override fun hasNext(): Boolean {
        return current > 0
    }

    override fun next(): Char {
        return flat[--current]
    }
}