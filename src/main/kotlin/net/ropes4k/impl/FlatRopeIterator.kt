/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

internal class FlatRopeIterator(private val flat: FlatRope, start: Int) : Iterator<Char> {
    private var current: Int = start

    init {
        if (start < 0 || start > flat.length) throw IndexOutOfBoundsException("Rope index out of range: $start")
    }

    override fun hasNext(): Boolean {
        return current < flat.length
    }

    override fun next(): Char {
        if ( current < flat.length ) {
            return flat[current++]
        }
        throw NoSuchElementException("Iterator is at end of Rope at $current")
    }
}