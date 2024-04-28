/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope

internal class RopeUtilities {
    companion object {
        fun depth(r: Rope?): Int {
            return if (r is AbstractRope) {
                r.depth()
            } else {
                0
            }
        }
    }
}
