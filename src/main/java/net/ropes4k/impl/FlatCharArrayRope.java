/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import net.ropes4k.Rope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A rope constructed from a character array. This rope is even
 * flatter than a regular flat rope.
 *
 * @author Amin Ahmad
 */
public final class FlatCharArrayRope extends AbstractRope implements FlatRope {

    private final char[] sequence;

    /**
     * Constructs a new rope from a character array.
     *
     * @param sequence the character array.
     */
    public FlatCharArrayRope(char[] sequence) {
        this(sequence, 0, sequence.length);
    }

    /**
     * Constructs a new rope from a character array range.
     *
     * @param sequence the character array.
     * @param offset   the offset in the array.
     * @param length   the length of the array.
     */
    public FlatCharArrayRope(char[] sequence, int offset, int length) {
        if (length > sequence.length)
            throw new IllegalArgumentException("Length must be less than " + sequence.length);
        this.sequence = new char[length];
        System.arraycopy(sequence, offset, this.sequence, 0, length);
    }

    @Override
    public char charAt(int index) {
        return sequence[index];
    }

    @Override
    public byte depth() {
        return 0;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(char ch) {
        for (int j = 0; j < sequence.length; ++j)
            if (sequence[j] == ch)
                return j;
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(char ch, int fromIndex) {
        if (fromIndex < 0 || fromIndex >= length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
        for (int j = fromIndex; j < sequence.length; ++j)
            if (sequence[j] == ch)
                return j;
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(CharSequence sequence, int fromIndex) {
        // Implementation of Boyer-Moore-Horspool algorithm with
        // special support for unicode.

        // step 0. sanity check.
        int length = sequence.length();
        if (length == 0)
            return -1;
        if (length == 1)
            return indexOf(sequence.charAt(0), fromIndex);

        int[] bcs = new int[256]; // bad character shift
        Arrays.fill(bcs, length);

        // step 1. preprocessing.
        for (int j = 0; j < length - 1; ++j) {
            char c = sequence.charAt(j);
            int l = (c & 0xFF);
            bcs[l] = Math.min(length - j - 1, bcs[l]);
        }

        // step 2. search.
        for (int j = fromIndex + length - 1; j < length(); ) {
            int x = j, y = length - 1;
            while (true) {
                if (sequence.charAt(y) != this.sequence[x]) {
                    j += bcs[(this.sequence[x] & 0xFF)];
                    break;
                }
                if (y == 0)
                    return x;
                --x;
                --y;
            }

        }

        return -1;
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
                return sequence[current++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public int length() {
        return sequence.length;
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
                return sequence[--current];
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
        if (end - start < 16) {
            return new FlatCharArrayRope(sequence, start, end - start);
        } else {
            return new SubstringRope(this, start, end - start);
        }
    }

    @NotNull
	@Override
    public String toString() {
        return new String(sequence);
    }


    public String toString(int offset, int length) {
        return new String(sequence, offset, length);
    }

    @Override
    public void write(Writer out) throws IOException {
        write(out, 0, length());
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        out.write(sequence, offset, length);
    }
}
