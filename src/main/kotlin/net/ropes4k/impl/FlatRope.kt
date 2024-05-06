/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

/**
 * A rope that is directly backed by a data source.
 */
internal interface FlatRope : InternalRope {
    /**
     * Returns a `String` representation of a range
     * in this rope.
     *
     * @param offset the offset.
     * @param length the length.
     * @return a `String` representation of a range
     * in this rope.
     */
    fun toString(offset: Int, length: Int): String
}
