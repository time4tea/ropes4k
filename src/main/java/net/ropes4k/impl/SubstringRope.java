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
 * Represents a lazily-evaluated substring of another rope. For performance
 * reasons, the target rope must be a <code>FlatRope</code>.
 *
 * @author aahmad
 */
public class SubstringRope extends AbstractRope {

    private final FlatRope rope;
    private final int offset;
    private final int length;

    public SubstringRope(FlatRope rope, int offset, int length) {
        if (length < 0 || offset < 0 || offset + length > rope.length())
            throw new IndexOutOfBoundsException("Invalid substring offset (" + offset + ") and length (" + length + ") for underlying rope with length " + rope.length());

        this.rope = rope;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        if (index >= length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + index);

        return rope.charAt(offset + index);
    }

    @Override
    public byte depth() {
        return RopeUtilities.INSTANCE.depth(getRope());
    }

    int getOffset() {
        return offset;
    }

    /**
     * Returns the rope underlying this one.
     *
     * @return the rope underlying this one.
     */
    public Rope getRope() {
        return rope;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        return new Iterator<Character>() {

            final Iterator<Character> u = getRope().iterator(getOffset() + start);
            int position = start;

            @Override
            public boolean hasNext() {
                return position < length();
            }

            @Override
            public Character next() {
                ++position;
                return u.next();
            }

            @Override
            public void remove() {
                u.remove();
            }

        };
    }

    @Override
    public int length() {
        return length;
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
            final Iterator<Character> u = getRope().reverseIterator(getRope().length() - getOffset() - length() + start);
            int position = length() - start;

            @Override
            public boolean hasNext() {
                return position > 0;
            }

            @Override
            public Character next() {
                --position;
                return u.next();
            }

            @Override
            public void remove() {
                u.remove();
            }
        };
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start == 0 && end == length())
            return this;
        return new SubstringRope(rope, offset + start, end - start);
    }

    @Override
    public String toString() {
        return rope.toString(offset, length);
    }

    @Override
    public void write(Writer out) throws IOException {
        rope.write(out, offset, length);
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        rope.write(out, this.offset + offset, length);
    }
}
