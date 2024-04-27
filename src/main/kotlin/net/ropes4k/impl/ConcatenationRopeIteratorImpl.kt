/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.util.ArrayDeque

/**
 * A fast iterator for concatenated ropes. Iterating over a complex
 * rope structure is guaranteed to be O(n) so long as it is reasonably
 * well-balanced. Compare this to O(nlogn) for iteration using
 * `charAt`.
 */
internal class ConcatenationRopeIteratorImpl(rope: Rope, start: Int) : Iterator<Char> {
    private val toTraverse = ArrayDeque<Rope>()
    private var currentRope: Rope?
    private var currentRopePos = 0
    private var skip = 0
    var pos: Int = 0
        private set


    init {
        toTraverse.push(rope)
        currentRope = null
        initialize()

        require(!(start < 0 || start > rope.length)) { "Rope index out of range: $start" }
        moveForward(start)
    }

    fun canMoveBackwards(amount: Int): Boolean {
        return (-1 <= (currentRopePos - amount))
    }

    override fun hasNext(): Boolean {
        return currentRopePos < currentRope!!.length - 1 || !toTraverse.isEmpty()
    }

    /**
     * Initialize the currentRope and currentRopePos fields.
     */
    private fun initialize() {
        while (!toTraverse.isEmpty()) {
            currentRope = toTraverse.pop()
            if (currentRope is ConcatenationRope) {
                toTraverse.push((currentRope as ConcatenationRope).right)
                toTraverse.push((currentRope as ConcatenationRope).left)
            } else {
                break
            }
        }
        requireNotNull(currentRope) { "No terminal ropes present." }
        currentRopePos = -1
        pos = -1
    }

    fun moveBackwards(amount: Int) {
        require(canMoveBackwards(amount)) { "Unable to move backwards $amount." }
        currentRopePos -= amount
        pos -= amount
    }

    fun moveForward(amount: Int) {
        pos += amount
        var remainingAmt = amount
        while (remainingAmt != 0) {
            val available = currentRope!!.length - currentRopePos - 1
            if (remainingAmt <= available) {
                currentRopePos += remainingAmt
                return
            }
            remainingAmt -= available
            if (toTraverse.isEmpty()) {
                pos -= remainingAmt
                throw IllegalArgumentException("Unable to move forward $amount. Reached end of rope.")
            }

            while (!toTraverse.isEmpty()) {
                currentRope = toTraverse.pop()
                if (currentRope is ConcatenationRope) {
                    toTraverse.push((currentRope as ConcatenationRope).right)
                    toTraverse.push((currentRope as ConcatenationRope).left)
                } else {
                    currentRopePos = -1
                    break
                }
            }
        }
    }

    override fun next(): Char {
        moveForward(1 + skip)
        skip = 0
        return currentRope!![currentRopePos]
    }

    fun skip(skip: Int) {
        this.skip = skip
    }
}
