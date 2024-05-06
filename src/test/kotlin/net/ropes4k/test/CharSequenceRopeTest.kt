/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.test

import net.ropes4k.Rope

class CharSequenceRopeTest : RopeContract() {
    override fun make(s: String): Rope {
        return Rope.of(s)
    }
}