/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ropes4k.Rope;

/**
 * A rope constructed from an underlying character sequence.
 * @author Amin Ahmad
 */
public final class FlatCharSequenceRope extends AbstractRope implements FlatRope {

	private final CharSequence sequence;

	/**
	 * Constructs a new rope from an underlying character sequence.
	 */
	public FlatCharSequenceRope(final CharSequence sequence) {
		this.sequence = sequence;
	}

	@Override
	public char charAt(final int index) {
		return this.sequence.charAt(index);
	}

	@Override
	public byte depth() {
		return 0;
	}

	@Override
	public Iterator<Character> iterator(final int start) {
		if (start < 0 || start > this.length())
			throw new IndexOutOfBoundsException("Rope index out of range: " + start);
		return new Iterator<Character>() {
			int current = start;
			@Override
			public boolean hasNext() {
				return this.current < FlatCharSequenceRope.this.length();
			}

			@Override
			public Character next() {
				return FlatCharSequenceRope.this.sequence.charAt(this.current++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Rope iterator is read-only.");
			}
		};
	}

	@Override
	public int length() {
		return this.sequence.length();
	}

	@Override
	public Matcher matcher(final Pattern pattern) {
		// optimized to return a matcher directly on the underlying sequence.
		return pattern.matcher(this.sequence);
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
			int current = FlatCharSequenceRope.this.length() - start;
			@Override
			public boolean hasNext() {
				return this.current > 0;
			}

			@Override
			public Character next() {
				return FlatCharSequenceRope.this.sequence.charAt(--this.current);
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
		if (end - start < 8 || this.sequence instanceof String /* special optimization for String */) {
			return new FlatCharSequenceRope(this.sequence.subSequence(start, end));
		} else {
			return new SubstringRope(this, start, end-start);
		}
	}

	@Override
	public String toString() {
		return this.sequence.toString();
	}

	public String toString(final int offset, final int length) {
		return this.sequence.subSequence(offset, offset + length).toString();
	}

	@Override
	public void write(final Writer out) throws IOException {
		this.write(out, 0, this.length());
	}

	@Override
	public void write(final Writer out, final int offset, final int length) throws IOException {
		if (offset < 0 || offset + length > this.length())
			throw new IndexOutOfBoundsException("Rope index out of bounds:" + (offset < 0 ? offset: offset + length));

		if (this.sequence instanceof String) {	// optimization for String
			out.write(((String) this.sequence).substring(offset, offset+length));
			return;
		}
		for (int j=offset; j<offset + length; ++j)
			out.write(this.sequence.charAt(j));
	}
}
