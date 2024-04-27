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

/**
 * A rope representing the reversal of character sequence.
 * Internal implementation only.
 *
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
     */
    public ReverseRope(Rope rope) {
        this.rope = rope;
    }

    @Override
    public char charAt(int index) {
        return rope.charAt(length() - index - 1);
    }

    @Override
    public int depth() {
        return RopeUtilities.Companion.depth(rope);
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        return new Iterator<>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return current < length();
            }

            @Override
            public Character next() {
                return charAt(current++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public int length() {
        return rope.length();
    }

    @Override
    public Rope reverse() {
        return rope;
    }

    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        return new Iterator<>() {
            int current = length() - start;

            @Override
            public boolean hasNext() {
                return current > 0;
            }

            @Override
            public Character next() {
                return charAt(--current);
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
        return rope.subSequence(length() - end, length() - start).reverse();
    }

    @Override
    public void write(Writer out) throws IOException {
        write(out, 0, length());
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        if (offset < 0 || offset + length > length())
            throw new IndexOutOfBoundsException("Rope index out of bounds:" + (offset < 0 ? offset : offset + length));
        for (int j = offset; j < offset + length; ++j)
            out.write(charAt(j));
    }
}
