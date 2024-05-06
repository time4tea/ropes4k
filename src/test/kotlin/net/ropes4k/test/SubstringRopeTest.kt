/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test

import net.ropes4k.Rope
import net.ropes4k.impl.FlatCharSequenceRope
import net.ropes4k.impl.SubstringRope

class SubstringRopeTest : RopeContract() {
    override fun make(s: String): Rope {
        return SubstringRope(
            FlatCharSequenceRope(s),
            0,
            s.length
        )
    }
}