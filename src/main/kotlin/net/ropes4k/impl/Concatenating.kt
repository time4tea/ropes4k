/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */

package net.ropes4k.impl

/**
 * Concatenate two ropes. Implements all recommended optimizations in "Ropes: an
 * Alternative to Strings".
 *
 * @param left  the first rope.
 * @param right the second rope.
 * @return the concatenation of the specified ropes.
 */
internal fun concatenate(left: InternalRope, right: InternalRope): InternalRope {
    if (left.isEmpty()) return right
    if (right.isEmpty()) return left
    require(left.length.toLong() + right.length <= Int.MAX_VALUE) {
        ("Left length=" + left.length + ", right length=" + right.length
                + ". Concatenation would overflow length field.")
    }
    val combineLength = 17
    if (left.length + right.length < combineLength) {
        return FlatCharSequenceRope(left.toString() + right)
    }
    if (left !is ConcatenationRope) {
        if (right is ConcatenationRope) {
            if (left.length + right.left.length < combineLength) return maybeRebalance(
                ConcatenationRope(
                    FlatCharSequenceRope(left.toString() + right.left.toString()),
                    right.right
                )
            )
        }
    }
    if (right !is ConcatenationRope) {
        if (left is ConcatenationRope) {
            if (right.length + left.right.length < combineLength) return maybeRebalance(
                ConcatenationRope(
                    left.left,
                    FlatCharSequenceRope(left.right.toString() + right.toString())
                )
            )
        }
    }

    return maybeRebalance(ConcatenationRope(left, right))
}

private const val MAX_ROPE_DEPTH: Short = 96

private fun maybeRebalance(r: InternalRope): InternalRope {
    return if (r.depth > MAX_ROPE_DEPTH) {
        rebalance(r)
    } else {
        r
    }
}

