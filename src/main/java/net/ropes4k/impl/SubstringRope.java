/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import net.ropes4k.Rope;

/**
 * Represents a lazily-evaluated substring of another rope. For performance
 * reasons, the target rope must be a <code>FlatRope</code>.
 * @author aahmad
 */
public class SubstringRope extends AbstractRope {

	private final FlatRope rope;
	private final int offset;
	private final int length;

	public SubstringRope(final FlatRope rope, final int offset, final int length) {
		if (length < 0 || offset < 0 || offset + length > rope.length())
			throw new IndexOutOfBoundsException("Invalid substring offset (" + offset + ") and length (" + length + ") for underlying rope with length " + rope.length());

		this.rope = rope;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public char charAt(final int index) {
		if (index >= this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + index);

		return this.rope.charAt(this.offset + index);
	}

	@Override
	public byte depth() {
		return RopeUtilities.INSTANCE.depth(getRope());
	}

	int getOffset() {
		return this.offset;
	}

	/**
	 * Returns the rope underlying this one.
	 * @return the rope underlying this one.
	 */
	public Rope getRope() {
		return this.rope;
	}

	@Override
	public Iterator<Character> iterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new Iterator<Character>() {

			final Iterator<Character> u = SubstringRope.this.getRope().iterator(SubstringRope.this.getOffset() + start);
			int position = start;

			@Override
			public boolean hasNext() {
				return this.position < SubstringRope.this.length();
			}

			@Override
			public Character next() {
				++this.position;
				return this.u.next();
			}

			@Override
			public void remove() {
				this.u.remove();
			}

		};
	}

	@Override
	public int length() {
		return this.length;
	}

	@Override
	public Rope reverse() {
		return new ReverseRope(this);
	}

	@Override
	public Iterator<Character> reverseIterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new Iterator<Character>() {
			final Iterator<Character> u = SubstringRope.this.getRope().reverseIterator(SubstringRope.this.getRope().length() - SubstringRope.this.getOffset() - SubstringRope.this.length() + start);
			int position = SubstringRope.this.length() - start;

			@Override
			public boolean hasNext() {
				return this.position > 0;
			}

			@Override
			public Character next() {
				--this.position;
				return this.u.next();
			}

			@Override
			public void remove() {
				this.u.remove();
			}
		};
	}

	@Override
	public Rope subSequence(final int start, final int end) {
		if (start == 0 && end == this.length())
			return this;
		return new SubstringRope(this.rope, this.offset + start, end-start);
	}

	@Override
	public String toString() {
		return this.rope.toString(this.offset, this.length);
	}

	@Override
	public void write(final Writer out) throws IOException {
		this.rope.write(out, this.offset, this.length);
	}

	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException {
		this.rope.write(out, this.offset + offset, length);
	}
}
