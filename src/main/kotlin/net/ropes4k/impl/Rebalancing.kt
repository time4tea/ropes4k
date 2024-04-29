/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

import net.ropes4k.Rope
import java.util.ArrayDeque

internal fun rebalance(r: InternalRope): InternalRope {
    // get all the nodes into a list

    val leafNodes = ArrayList<InternalRope>()
    val toExamine = ArrayDeque<InternalRope>()
    // begin a depth first loop.
    toExamine.add(r)
    while (!toExamine.isEmpty()) {
        val x = toExamine.pop()
        if (x is ConcatenationRope) {
            toExamine.push(x.right)
            toExamine.push(x.left)
        } else {
            leafNodes.add(x)
        }
    }
    return merge(leafNodes, 0, leafNodes.size)
}

private fun merge(leafNodes: ArrayList<InternalRope>, start: Int, end: Int): InternalRope {
    val range = end - start
    when (range) {
        1 -> return leafNodes[start]
        2 -> return ConcatenationRope(leafNodes[start], leafNodes[start + 1])
        else -> {
            val middle = start + (range / 2)
            return ConcatenationRope(
                merge(leafNodes, start, middle),
                merge(leafNodes, middle, end)
            )
        }
    }
}
