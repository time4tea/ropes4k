/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.PrintStream
import java.util.ArrayDeque

/**
 * Visualize a rope.
 *
 */
public fun visualize(r: Rope, out: PrintStream = System.out) {
    visualize(r, out, 0)
}

private fun visualize(r: Rope, out: PrintStream, indent: Int) {
    if (r is FlatRope) {
        out.print(" ".repeat(indent * 2))
        out.println("\"" + r + "\"")
    }
    if (r is SubstringRope) {
        out.print(" ".repeat(indent * 2))
        out.println("substring " + r.length + " \"" + r + "\"")
    }
    if (r is ConcatenationRope) {
        out.print(" ".repeat(indent * 2))
        out.println("concat[left]")
        visualize(r.left, out, indent + 1)
        out.print(" ".repeat(indent * 2))
        out.println("concat[right]")
        visualize(r.right, out, indent + 1)
    }
}

public fun stats(r: Rope, out: PrintStream) {
    var nonLeaf = 0
    val leafNodes = ArrayList<Rope>()
    val toExamine = ArrayDeque<Rope>()
    // begin a depth first loop.
    toExamine.add(r)
    while (!toExamine.isEmpty()) {
        val x = toExamine.pop()
        if (x is ConcatenationRope) {
            ++nonLeaf
            toExamine.push(x.right)
            toExamine.push(x.left)
        } else {
            leafNodes.add(x)
        }
    }
    out.println(
        "rope(length=" + r.length + ", leaf nodes=" + leafNodes.size + ", non-leaf nodes=" + nonLeaf + ", depth=" + (r as InternalRope).depth + ")"
    )
}
