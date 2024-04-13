/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k;

import net.ropes4k.impl.FlatCharArrayRope;
import net.ropes4k.impl.FlatCharSequenceRope;

/**
 * A factory for building ropes.
 * @author Amin Ahmad
 */
public final class RopeBuilder {

	/**
	 * Construct a rope from a character array.
	 * @param sequence a character array
	 * @return a rope representing the underlying character array.
	 */
	public Rope build(final char[] sequence) {
		return new FlatCharArrayRope(sequence);
	}

	/**
	 * Construct a rope from an underlying character sequence.
	 * @param sequence the underlying character sequence.
	 * @return a rope representing the underlying character sequence.
	 */
	public Rope build(final CharSequence sequence) {
		if (sequence instanceof Rope)
			return (Rope) sequence;
		return new FlatCharSequenceRope(sequence);
	}
}
