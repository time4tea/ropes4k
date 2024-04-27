/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import java.util.ArrayDeque;
import java.util.Iterator;

import net.ropes4k.Rope;

/**
 * A fast iterator for concatenated ropes. Iterating over a complex
 * rope structure is guaranteed to be O(n) so long as it is reasonably
 * well-balanced. Compare this to O(nlogn) for iteration using
 * <code>charAt</code>.
 *
 * @author aahmad
 */
public class ConcatenationRopeIteratorImpl implements Iterator<Character> {

	private final ArrayDeque<Rope> toTraverse;
	private Rope currentRope;
	private int currentRopePos;
	private int skip;
	private int currentAbsolutePos;


	public ConcatenationRopeIteratorImpl(Rope rope) {
		this(rope, 0);
	}

	public ConcatenationRopeIteratorImpl(Rope rope, int start) {
		toTraverse = new ArrayDeque<Rope>();
		toTraverse.push(rope);
		currentRope = null;
		initialize();

		if (start < 0 || start > rope.length()) {
			throw new IllegalArgumentException("Rope index out of range: " + start);
		}
		moveForward(start);
	}

	public boolean canMoveBackwards(int amount) {
		return (-1 <= (currentRopePos - amount));
	}

	public int getPos() {
		return currentAbsolutePos;
	}

	@Override
	public boolean hasNext() {
		return currentRopePos < currentRope.length() - 1 || !toTraverse.isEmpty();
	}

	/**
	 * Initialize the currentRope and currentRopePos fields.
	 */
	private void initialize() {
		while (!toTraverse.isEmpty()) {
			currentRope = toTraverse.pop();
			if (currentRope instanceof ConcatenationRope) {
				toTraverse.push(((ConcatenationRope) currentRope).getRight());
				toTraverse.push(((ConcatenationRope) currentRope).getLeft());
			} else {
				break;
			}
		}
		if (currentRope == null)
			throw new IllegalArgumentException("No terminal ropes present.");
		currentRopePos = -1;
		currentAbsolutePos = -1;
	}

	public void moveBackwards(int amount) {
		if (!canMoveBackwards(amount))
			throw new IllegalArgumentException("Unable to move backwards " + amount + ".");
		currentRopePos -= amount;
		currentAbsolutePos -= amount;
	}

	public void moveForward(int amount) {
		currentAbsolutePos += amount;
		int remainingAmt = amount;
		while (remainingAmt != 0) {
			int available = currentRope.length() - currentRopePos - 1;
			if (remainingAmt <= available) {
				currentRopePos += remainingAmt;
				return;
			}
			remainingAmt -= available;
			if (toTraverse.isEmpty()) {
				currentAbsolutePos -= remainingAmt;
				throw new IllegalArgumentException("Unable to move forward " + amount + ". Reached end of rope.");
			}

			while (!toTraverse.isEmpty()) {
				currentRope = toTraverse.pop();
				if (currentRope instanceof ConcatenationRope) {
					toTraverse.push(((ConcatenationRope) currentRope).getRight());
					toTraverse.push(((ConcatenationRope) currentRope).getLeft());
				} else {
					currentRopePos = -1;
					break;
				}
			}
		}
	}

	@Override
	public Character next() {
		moveForward(1 + skip);
		skip = 0;
		return currentRope.charAt(currentRopePos);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Rope iterator is read-only.");
	}

	/* (non-Javadoc)
	 * @see org.ahmadsoft.ropes.impl.RopeIterators#skip(int)
	 */
	public void skip(int skip) {
		this.skip = skip;
	}
}
