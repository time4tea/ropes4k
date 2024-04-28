/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.PrintStream
import java.util.ArrayDeque

internal class RopeUtilities {
    companion object {
        private const val MAX_ROPE_DEPTH: Short = 96

        private fun merge(leafNodes: ArrayList<Rope>, start: Int, end: Int): Rope {
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

        fun rebalance(r: Rope): Rope {
            // get all the nodes into a list

            val leafNodes = ArrayList<Rope>()
            val toExamine = ArrayDeque<Rope>()
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

        /**
         * Returns the depth of the specified rope.
         *
         * @param r the rope.
         * @return the depth of the specified rope.
         */
        fun depth(r: Rope?): Int {
            return if (r is AbstractRope) {
                r.depth()
            } else {
                0
            }
        }

        fun visualize(r: Rope, out: PrintStream, depth: Int) {
            if (r is FlatRope) {
                out.print(" ".repeat(depth * 2))
                out.println("\"" + r + "\"")
            }
            if (r is SubstringRope) {
                out.print(" ".repeat(depth * 2))
                out.println("substring " + r.length + " \"" + r + "\"")
            }
            if (r is ConcatenationRope) {
                out.print(" ".repeat(depth * 2))
                out.println("concat[left]")
                visualize(r.left, out, depth + 1)
                out.print(" ".repeat(depth * 2))
                out.println("concat[right]")
                visualize(r.right, out, depth + 1)
            }
        }

        /**
         * Visualize a rope.
         *
         */
        fun visualize(r: Rope, out: PrintStream) {
            visualize(r, out, 0.toByte().toInt())
        }

        /**
         * Rebalance a rope if the depth has exceeded MAX_ROPE_DEPTH. If the
         * rope depth is less than MAX_ROPE_DEPTH or if the rope is of unknown
         * type, no rebalancing will occur.
         *
         * @param r the rope to rebalance.
         * @return a rebalanced copy of the specified rope.
         */
        fun autoRebalance(r: Rope): Rope {
            return if (r is AbstractRope && r.depth() > MAX_ROPE_DEPTH) {
                rebalance(r)
            } else {
                r
            }
        }

        /**
         * Concatenate two ropes. Implements all recommended optimizations in "Ropes: an
         * Alternative to Strings".
         *
         * @param left  the first rope.
         * @param right the second rope.
         * @return the concatenation of the specified ropes.
         */
        fun concatenate(left: Rope, right: Rope): Rope {
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
                    if (left.length + right.left.length < combineLength) return autoRebalance(
                        ConcatenationRope(
                            FlatCharSequenceRope(left.toString() + right.left),
                            right.right
                        )
                    )
                }
            }
            if (right !is ConcatenationRope) {
                if (left is ConcatenationRope) {
                    if (right.length + left.right.length < combineLength) return autoRebalance(
                        ConcatenationRope(
                            left.left,
                            FlatCharSequenceRope(left.right.toString() + right)
                        )
                    )
                }
            }

            return autoRebalance(ConcatenationRope(left, right))
        }

        fun stats(r: Rope, out: PrintStream) {
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
                "rope(length=" + r.length + ", leaf nodes=" + leafNodes.size + ", non-leaf nodes=" + nonLeaf + ", depth=" + depth(
                    r
                ) + ")"
            )
        }
    }
}
