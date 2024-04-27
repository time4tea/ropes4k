/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import net.ropes4k.Rope;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rope constructed from an underlying character sequence.
 *
 * @author Amin Ahmad
 */
public final class FlatCharSequenceRope extends AbstractRope implements FlatRope {

    private final CharSequence sequence;

    /**
     * Constructs a new rope from an underlying character sequence.
     */
    public FlatCharSequenceRope(CharSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public char charAt(int index) {
        return sequence.charAt(index);
    }

    @Override
    public byte depth() {
        return 0;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        return new Iterator<Character>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return current < length();
            }

            @Override
            public Character next() {
                return sequence.charAt(current++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public int length() {
        return sequence.length();
    }

    @Override
    public Matcher matcher(Pattern pattern) {
        // optimized to return a matcher directly on the underlying sequence.
        return pattern.matcher(sequence);
    }

    @Override
    public Rope reverse() {
        return new ReverseRope(this);
    }

    @Override
    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        return new Iterator<Character>() {
            int current = length() - start;

            @Override
            public boolean hasNext() {
                return current > 0;
            }

            @Override
            public Character next() {
                return sequence.charAt(--current);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start == 0 && end == length())
            return this;
        if (end - start < 8 || sequence instanceof String /* special optimization for String */) {
            return new FlatCharSequenceRope(sequence.subSequence(start, end));
        } else {
            return new SubstringRope(this, start, end - start);
        }
    }

    @Override
    public String toString() {
        return sequence.toString();
    }

    public String toString(int offset, int length) {
        return sequence.subSequence(offset, offset + length).toString();
    }

    @Override
    public void write(Writer out) throws IOException {
        write(out, 0, length());
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        if (offset < 0 || offset + length > length())
            throw new IndexOutOfBoundsException("Rope index out of bounds:" + (offset < 0 ? offset : offset + length));

        if (sequence instanceof String) {    // optimization for String
            out.write(((String) sequence).substring(offset, offset + length));
            return;
        }
        for (int j = offset; j < offset + length; ++j)
            out.write(sequence.charAt(j));
    }
}
