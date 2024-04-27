/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import net.ropes4k.Rope;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for ropes that implements many of the common operations.
 *
 * @author Amin Ahmad
 */
public abstract class AbstractRope implements Rope {

    protected int hashCode = 0;

    @Override
    public Rope append(char c) {
        return RopeUtilities.INSTANCE.concatenate(this, Rope.BUILDER.build(String.valueOf(c)));
    }

    @Override
    public Rope append(CharSequence suffix) {
        return RopeUtilities.INSTANCE.concatenate(this, Rope.BUILDER.build(suffix));
    }

    @Override
    public Rope append(CharSequence csq, int start, int end) {
        return RopeUtilities.INSTANCE.concatenate(this, Rope.BUILDER.build(csq).subSequence(start, end));
    }

    @Override
    public int compareTo(CharSequence sequence) {
        int compareTill = Math.min(sequence.length(), length());
        Iterator<Character> i = iterator();
        for (int j = 0; j < compareTill; ++j) {
            char x = i.next();
            char y = sequence.charAt(j);
            if (x != y)
                return x - y;
        }
        return length() - sequence.length();
    }

    @Override
    public Rope delete(int start, int end) {
        if (start == end)
            return this;
        return subSequence(0, start).append(subSequence(end, length()));
    }

    /*
     * The depth of the current rope, as defined in "Ropes: an Alternative
     * to Strings".
     */
    public abstract byte depth();

    @Override
    public boolean equals(Object other) {
        if (other instanceof Rope rope) {
            if (rope.hashCode() != hashCode() || rope.length() != length())
                return false;
            Iterator<Character> i1 = iterator();
            Iterator<Character> i2 = rope.iterator();

            while (i1.hasNext()) {
                char a = i1.next();
                char b = i2.next();
                if (a != b)
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * A utility method that returns an instance of this rope optimized
     * for sequential access.
     */
    protected CharSequence getForSequentialAccess() {
        return this;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0 && length() > 0) {
            if (length() < 6) {
                for (char c : this)
                    hashCode = 31 * hashCode + c;
            } else {
                Iterator<Character> i = iterator();
                for (int j = 0; j < 5; ++j)
                    hashCode = 31 * hashCode + i.next();
                hashCode = 31 * hashCode + charAt(length() - 1);
            }
        }
        return hashCode;
    }

    @Override
    public int indexOf(char ch) {
        int index = -1;
        for (char c : this) {
            ++index;
            if (c == ch)
                return index;
        }
        return -1;
    }

    @Override
    public boolean startsWith(CharSequence prefix) {
        return startsWith(prefix, 0);
    }

    @Override
    public boolean startsWith(CharSequence prefix, int offset) {
        if (offset < 0 || offset > length())
            throw new IndexOutOfBoundsException("Rope offset out of range: " + offset);
        if (offset + prefix.length() > length())
            return false;

        int x = 0;
        for (Iterator<Character> i = iterator(offset); i.hasNext() && x < prefix.length(); ) {
            if (i.next().charValue() != prefix.charAt(x++))
                return false;
        }
        return true;
    }

    @Override
    public boolean endsWith(CharSequence suffix) {
        return endsWith(suffix, 0);
    }

    @Override
    public boolean endsWith(CharSequence suffix, int offset) {
        return startsWith(suffix, length() - suffix.length() - offset);
    }

    @Override
    public int indexOf(char ch, int fromIndex) {
        if (fromIndex < 0 || fromIndex >= length())
            throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
        int index = fromIndex - 1;
        for (Iterator<Character> i = iterator(fromIndex); i.hasNext(); ) {
            ++index;
            if (i.next().charValue() == ch)
                return index;
        }
        return -1;
    }

    @Override
    public int indexOf(CharSequence sequence) {
        return indexOf(sequence, 0);
    }

    @Override
    public int indexOf(CharSequence sequence, int fromIndex) {
        CharSequence me = getForSequentialAccess();

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
                char c = me.charAt(x);
                if (sequence.charAt(y) != c) {
                    j += bcs[(me.charAt(j) & 0xFF)];
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
    public Rope insert(int dstOffset, CharSequence s) {
        Rope r = (s == null) ? Rope.BUILDER.build("null") : Rope.BUILDER.build(s);
        if (dstOffset == 0)
            return r.append(this);
        else if (dstOffset == length())
            return append(r);
        else if (dstOffset < 0 || dstOffset > length())
            throw new IndexOutOfBoundsException(dstOffset + " is out of insert range [" + 0 + ":" + length() + "]");
        return subSequence(0, dstOffset).append(r).append(subSequence(dstOffset, length()));
    }

    @Override
    public Iterator<Character> iterator() {
        return iterator(0);
    }

    @Override
    public Rope trimStart() {
        int index = -1;
        for (char c : this) {
            ++index;
            if (c > 0x20 && !Character.isWhitespace(c))
                break;
        }
        if (index <= 0)
            return this;
        else
            return subSequence(index, length());
    }

    @Override
    public Matcher matcher(Pattern pattern) {
        return pattern.matcher(getForSequentialAccess());
    }

    @Override
    public boolean matches(Pattern regex) {
        return regex.matcher(getForSequentialAccess()).matches();
    }

    @Override
    public boolean matches(String regex) {
        return Pattern.matches(regex, getForSequentialAccess());
    }

    @Override
    public Rope rebalance() {
        return this;
    }

    @Override
    public Iterator<Character> reverseIterator() {
        return reverseIterator(0);
    }

    @Override
    public Rope trimEnd() {
        int index = length() + 1;
        for (Iterator<Character> i = reverseIterator(); i.hasNext(); ) {
            char c = i.next();
            --index;
            if (c > 0x20 && !Character.isWhitespace(c))
                break;
        }
        if (index >= length())
            return this;
        else
            return subSequence(0, index);
    }

    @Override
    public String toString() {
        StringWriter out = new StringWriter(length());
        try {
            write(out);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    @Override
    public Rope trim() {
        return trimStart().trimEnd();
    }

    public Object writeReplace() throws ObjectStreamException {
        return new SerializedRope(this);
    }


    @Override
    public Rope padStart(int toWidth) {
        return padStart(toWidth, ' ');
    }

    @Override
    public Rope padStart(int toWidth, char padChar) {
        int toPad = toWidth - length();
        if (toPad < 1)
            return this;
        return RopeUtilities.INSTANCE.concatenate(
                Rope.BUILDER.build(new RepeatedCharacterSequence(padChar, toPad)),
                this);
    }

    @Override
    public Rope padEnd(int toWidth) {
        return padEnd(toWidth, ' ');
    }

    @Override
    public Rope padEnd(int toWidth, char padChar) {
        int toPad = toWidth - length();
        if (toPad < 1)
            return this;
        return RopeUtilities.INSTANCE.concatenate(
                this,
                Rope.BUILDER.build(new RepeatedCharacterSequence(padChar, toPad)));
    }

    @Override
    public boolean isEmpty() {
        return length() == 0;
    }
}
