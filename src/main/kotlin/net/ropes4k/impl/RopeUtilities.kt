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
        private val FIBONACCI = longArrayOf(
            0L,
            1L,
            1L,
            2L,
            3L,
            5L,
            8L,
            13L,
            21L,
            34L,
            55L,
            89L,
            144L,
            233L,
            377L,
            610L,
            987L,
            1597L,
            2584L,
            4181L,
            6765L,
            10946L,
            17711L,
            28657L,
            46368L,
            75025L,
            121393L,
            196418L,
            317811L,
            514229L,
            832040L,
            1346269L,
            2178309L,
            3524578L,
            5702887L,
            9227465L,
            14930352L,
            24157817L,
            39088169L,
            63245986L,
            102334155L,
            165580141L,
            267914296L,
            433494437L,
            701408733L,
            1134903170L,
            1836311903L,
            2971215073L,
            4807526976L,
            7778742049L,
            12586269025L,
            20365011074L,
            32951280099L,
            53316291173L,
            86267571272L,
            139583862445L,
            225851433717L,
            365435296162L,
            591286729879L,
            956722026041L,
            1548008755920L,
            2504730781961L,
            4052739537881L,
            6557470319842L,
            10610209857723L,
            17167680177565L,
            27777890035288L,
            44945570212853L,
            72723460248141L,
            117669030460994L,
            190392490709135L,
            308061521170129L,
            498454011879264L,
            806515533049393L,
            1304969544928657L,
            2111485077978050L,
            3416454622906707L,
            5527939700884757L,
            8944394323791464L,
            14472334024676221L,
            23416728348467685L,
            37889062373143906L,
            61305790721611591L,
            99194853094755497L,
            160500643816367088L,
            259695496911122585L,
            420196140727489673L,
            679891637638612258L,
            1100087778366101931L,
            1779979416004714189L,
            2880067194370816120L,
            4660046610375530309L,
            7540113804746346429L
        )
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
        fun depth(r: Rope?): Byte {
            return if (r is AbstractRope) {
                r.depth()
            } else {
                0
                //throw new IllegalArgumentException("Bad rope");
            }
        }

        fun isBalanced(r: Rope): Boolean {
            val depth = Companion.depth(r)
            if (depth >= FIBONACCI.size - 2) return false
            return (FIBONACCI[depth + 2] <= r.length) // TODO: not necessarily valid w/e.g. padding char sequences.
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
                    if (left.length + right.left.length < combineLength) return Companion.autoRebalance(
                        ConcatenationRope(
                            FlatCharSequenceRope(left.toString() + right.left),
                            right.right
                        )
                    )
                }
            }
            if (right !is ConcatenationRope) {
                if (left is ConcatenationRope) {
                    if (right.length + left.right.length < combineLength) return Companion.autoRebalance(
                        ConcatenationRope(
                            left.left,
                            FlatCharSequenceRope(left.right.toString() + right)
                        )
                    )
                }
            }

            return Companion.autoRebalance(ConcatenationRope(left, right))
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
