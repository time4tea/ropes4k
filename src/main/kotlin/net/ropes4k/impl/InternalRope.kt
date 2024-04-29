/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

import net.ropes4k.Rope

internal interface InternalRope: Rope {
    val depth: Int
    override fun reverse(): InternalRope
    override fun subSequence(startIndex: Int, endIndex: Int): InternalRope
}