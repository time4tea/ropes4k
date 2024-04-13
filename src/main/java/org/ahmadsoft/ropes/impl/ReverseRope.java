/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package org.ahmadsoft.ropes.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.ahmadsoft.ropes.Rope;

/**
 * A rope representing the reversal of character sequence.
 * Internal implementation only.
 * @author Amin Ahmad
 */
public final class ReverseRope extends AbstractRope {

	private final Rope rope;

	/**
	 * Constructs a new rope from an underlying rope.
	 * <p>
	 * Balancing algorithm works optimally when only FlatRopes or
	 * SubstringRopes are supplied. Framework must guarantee this
	 * as no runtime check is performed.
	 * @param rope
	 */
	public ReverseRope(final Rope rope) {
		this.rope = rope;
	}

	@Override
	public char charAt(final int index) {
		return this.rope.charAt(this.length() - index - 1);
	}

	@Override
	public byte depth() {
		return RopeUtilities.INSTANCE.depth(this.rope);
	}

	@Override
	public Iterator<Character> iterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new Iterator<Character>() {
			int current = start;
			@Override
			public boolean hasNext() {
				return this.current < ReverseRope.this.length();
			}

			@Override
			public Character next() {
				return ReverseRope.this.charAt(this.current++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Rope iterator is read-only.");
			}
		};
	}

	@Override
	public int length() {
		return this.rope.length();
	}

	@Override
	public Rope reverse() {
		return this.rope;
	}

	public Iterator<Character> reverseIterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new Iterator<Character>() {
			int current = ReverseRope.this.length() - start;
			@Override
			public boolean hasNext() {
				return this.current > 0;
			}

			@Override
			public Character next() {
				return ReverseRope.this.charAt(--this.current);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Rope iterator is read-only.");
			}
		};
	}

	@Override
	public Rope subSequence(final int start, final int end) {
		if (start == 0 && end == this.length())
			return this;
		return this.rope.subSequence(this.length() - end, this.length() - start).reverse();
	}

	@Override
	public void write(final Writer out) throws IOException {
		this.write(out, 0, this.length());
	}

	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException {
		if (offset < 0 || offset + length > this.length())
			throw new IndexOutOfBoundsException("Rope index out of bounds:" + (offset < 0 ? offset: offset + length));
		for (int j=offset; j<offset + length; ++j)
			out.write(this.charAt(j));
	}
}
