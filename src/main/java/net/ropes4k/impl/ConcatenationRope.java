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
 * A rope that represents the concatenation of two other ropes.
 * @author Amin Ahmad
 */
public final class ConcatenationRope extends AbstractRope {

    private final Rope left;
    private final Rope right;
    private final byte depth;
    private final int length;

    /**
     * Create a new concatenation rope from two ropes.
     * @param left the first rope.
     * @param right the second rope.
     */
    public ConcatenationRope(Rope left, Rope right) {
        this.left   = left;
        this.right  = right;
        depth  = (byte) (Math.max(RopeUtilities.INSTANCE.depth(left), RopeUtilities.INSTANCE.depth(right)) + 1);
        length = left.length() + right.length();
    }

    @Override
    public char charAt(int index) {
        if (index >= length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + index);

        return (index < left.length() ? left.charAt(index): right.charAt(index - left.length()));
    }

    @Override
    public byte depth() {
        return depth;
    }

    @Override
    public CharSequence getForSequentialAccess() {
        return getForSequentialAccess(this);
    }

    /*
     * Returns this object as a char sequence optimized for
     * regular expression searches.
     * <p>
     * <emph>This method is public only to facilitate unit
     * testing.</emph>
     */
    private CharSequence getForSequentialAccess(Rope rope) {
        return new CharSequence() {

            private final ConcatenationRopeIteratorImpl iterator = (ConcatenationRopeIteratorImpl) rope.iterator(0);

            @Override
            public char charAt(int index) {
                if (index > iterator.getPos()) {
                    iterator.skip(index- iterator.getPos()-1);
                    try {
                        return iterator.next();
                    } catch (IllegalArgumentException e) {
                        System.out.println("Rope length is: " + rope.length() + " charAt is " + index);
                        throw e;
                    }
                } else { /* if (index <= lastIndex) */
                    int toMoveBack = iterator.getPos() - index + 1;
                    if (iterator.canMoveBackwards(toMoveBack)) {
                        iterator.moveBackwards(toMoveBack);
                        return iterator.next();
                    } else {
                        return rope.charAt(index);
                    }
                }
            }

            @Override
            public int length() {
                return rope.length();
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return rope.subSequence(start, end);
            }

        };
    }

    /**
     * Return the left-hand rope.
     * @return the left-hand rope.
     */
    public Rope getLeft() {
        return left;
    }

    /**
     * Return the right-hand rope.
     * @return the right-hand rope.
     */
    public Rope getRight() {
        return right;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        if (start >= left.length()) {
            return right.iterator(start - left.length());
        } else {
            return new ConcatenationRopeIteratorImpl(this, start);
        }
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Rope rebalance() {
        return RopeUtilities.INSTANCE.rebalance(this);
    }

    @Override
    public Rope reverse() {
        return RopeUtilities.INSTANCE.concatenate(getRight().reverse(), getLeft().reverse());
    }

    @Override
    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        if (start >= right.length()) {
            return left.reverseIterator(start - right.length());
        } else {
            return new ConcatenationRopeReverseIteratorImpl(this, start);
        }
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start < 0 || end > length())
            throw new IllegalArgumentException("Illegal subsequence (" + start + "," + end + ")");
        if (start == 0 && end == length())
            return this;
        int l = left.length();
        if (end <= l)
            return left.subSequence(start, end);
        if (start >= l)
            return right.subSequence(start - l, end - l);
        return RopeUtilities.INSTANCE.concatenate(
            left.subSequence(start, l),
            right.subSequence(0, end - l));
    }

    @Override
    public void write(Writer out) throws IOException {
        left.write(out);
        right.write(out);
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        if (offset + length <= left.length()) {
            left.write(out, offset, length);
        } else if (offset >= left.length()) {
            right.write(out, offset - left.length(), length);
        } else {
            int writeLeft = left.length() - offset;
            left.write(out, offset, writeLeft);
            right.write(out, 0, length - writeLeft);
        }
    }
}
