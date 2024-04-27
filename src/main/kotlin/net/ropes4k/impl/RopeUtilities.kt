/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import net.ropes4k.Rope;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Contains utlities for manipulating ropes.
 *
 * @author aahmad
 */
class RopeUtilities {

    private static final long[] FIBONACCI = {0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L, 55L, 89L, 144L, 233L, 377L, 610L, 987L, 1597L, 2584L, 4181L, 6765L, 10946L, 17711L, 28657L, 46368L, 75025L, 121393L, 196418L, 317811L, 514229L, 832040L, 1346269L, 2178309L, 3524578L, 5702887L, 9227465L, 14930352L, 24157817L, 39088169L, 63245986L, 102334155L, 165580141L, 267914296L, 433494437L, 701408733L, 1134903170L, 1836311903L, 2971215073L, 4807526976L, 7778742049L, 12586269025L, 20365011074L, 32951280099L, 53316291173L, 86267571272L, 139583862445L, 225851433717L, 365435296162L, 591286729879L, 956722026041L, 1548008755920L, 2504730781961L, 4052739537881L, 6557470319842L, 10610209857723L, 17167680177565L, 27777890035288L, 44945570212853L, 72723460248141L, 117669030460994L, 190392490709135L, 308061521170129L, 498454011879264L, 806515533049393L, 1304969544928657L, 2111485077978050L, 3416454622906707L, 5527939700884757L, 8944394323791464L, 14472334024676221L, 23416728348467685L, 37889062373143906L, 61305790721611591L, 99194853094755497L, 160500643816367088L, 259695496911122585L, 420196140727489673L, 679891637638612258L, 1100087778366101931L, 1779979416004714189L, 2880067194370816120L, 4660046610375530309L, 7540113804746346429L};
    private static final short MAX_ROPE_DEPTH = 96;
    private static final String SPACES = "                                                                                                                                                                                                        ";

    public static RopeUtilities INSTANCE = new RopeUtilities();

    /**
     * Rebalance a rope if the depth has exceeded MAX_ROPE_DEPTH. If the
     * rope depth is less than MAX_ROPE_DEPTH or if the rope is of unknown
     * type, no rebalancing will occur.
     *
     * @param r the rope to rebalance.
     * @return a rebalanced copy of the specified rope.
     */
    public Rope autoRebalance(Rope r) {
        if (r instanceof AbstractRope && ((AbstractRope) r).depth() > RopeUtilities.MAX_ROPE_DEPTH) {
            return rebalance(r);
        } else {
            return r;
        }
    }

    /**
     * Concatenate two ropes. Implements all recommended optimizations in "Ropes: an
     * Alternative to Strings".
     *
     * @param left  the first rope.
     * @param right the second rope.
     * @return the concatenation of the specified ropes.
     */
    Rope concatenate(Rope left, Rope right) {
        if (left.isEmpty())
            return right;
        if (right.isEmpty())
            return left;
        if ((long) left.length() + right.length() > Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                    "Left length=" + left.length() + ", right length=" + right.length()
                            + ". Concatenation would overflow length field.");
        final int combineLength = 17;
        if (left.length() + right.length() < combineLength) {
            return new FlatCharSequenceRope(left.toString() + right);
        }
        if (!(left instanceof ConcatenationRope)) {
            if (right instanceof ConcatenationRope cRight) {
                if (left.length() + cRight.getLeft().length() < combineLength)
                    return autoRebalance(new ConcatenationRope(new FlatCharSequenceRope(left.toString() + cRight.getLeft()), cRight.getRight()));
            }
        }
        if (!(right instanceof ConcatenationRope)) {
            if (left instanceof ConcatenationRope cLeft) {
                if (right.length() + cLeft.getRight().length() < combineLength)
                    return autoRebalance(new ConcatenationRope(cLeft.getLeft(), new FlatCharSequenceRope(cLeft.getRight().toString() + right)));
            }
        }

        return autoRebalance(new ConcatenationRope(left, right));
    }

    /**
     * Returns the depth of the specified rope.
     *
     * @param r the rope.
     * @return the depth of the specified rope.
     */
    byte depth(Rope r) {
        if (r instanceof AbstractRope) {
            return ((AbstractRope) r).depth();
        } else {
            return 0;
            //throw new IllegalArgumentException("Bad rope");
        }
    }

    boolean isBalanced(Rope r) {
        byte depth = depth(r);
        if (depth >= RopeUtilities.FIBONACCI.length - 2)
            return false;
        return (RopeUtilities.FIBONACCI[depth + 2] <= r.length());    // TODO: not necessarily valid w/e.g. padding char sequences.
    }

    public Rope rebalance(Rope r) {
        // get all the nodes into a list

        ArrayList<Rope> leafNodes = new ArrayList<>();
        ArrayDeque<Rope> toExamine = new ArrayDeque<>();
        // begin a depth first loop.
        toExamine.add(r);
        while (!toExamine.isEmpty()) {
            Rope x = toExamine.pop();
            if (x instanceof ConcatenationRope) {
                toExamine.push(((ConcatenationRope) x).getRight());
                toExamine.push(((ConcatenationRope) x).getLeft());
            } else {
                leafNodes.add(x);
            }
        }
        return merge(leafNodes, 0, leafNodes.size());
    }

    private Rope merge(ArrayList<Rope> leafNodes, int start, int end) {
        int range = end - start;
        switch (range) {
            case 1:
                return leafNodes.get(start);
            case 2:
                return new ConcatenationRope(leafNodes.get(start), leafNodes.get(start + 1));
            default:
                int middle = start + (range / 2);
                return new ConcatenationRope(merge(leafNodes, start, middle), merge(leafNodes, middle, end));
        }
    }

    /**
     * Visualize a rope.
     *
     */
    void visualize(Rope r, PrintStream out) {
        visualize(r, out, (byte) 0);
    }

    public void visualize(Rope r, PrintStream out, int depth) {
        if (r instanceof FlatRope) {
            out.print(RopeUtilities.SPACES.substring(0, depth * 2));
            out.println("\"" + r + "\"");
//			out.println(r.length());
        }
        if (r instanceof SubstringRope) {
            out.print(RopeUtilities.SPACES.substring(0, depth * 2));
            out.println("substring " + r.length() + " \"" + r + "\"");
//			this.visualize(((SubstringRope)r).getRope(), out, depth+1);
        }
        if (r instanceof ConcatenationRope) {
            out.print(RopeUtilities.SPACES.substring(0, depth * 2));
            out.println("concat[left]");
            visualize(((ConcatenationRope) r).getLeft(), out, depth + 1);
            out.print(RopeUtilities.SPACES.substring(0, depth * 2));
            out.println("concat[right]");
            visualize(((ConcatenationRope) r).getRight(), out, depth + 1);
        }
    }

    public void stats(Rope r, PrintStream out) {
        int nonLeaf = 0;
        ArrayList<Rope> leafNodes = new ArrayList<Rope>();
        ArrayDeque<Rope> toExamine = new ArrayDeque<Rope>();
        // begin a depth first loop.
        toExamine.add(r);
        while (!toExamine.isEmpty()) {
            Rope x = toExamine.pop();
            if (x instanceof ConcatenationRope) {
                ++nonLeaf;
                toExamine.push(((ConcatenationRope) x).getRight());
                toExamine.push(((ConcatenationRope) x).getLeft());
            } else {
                leafNodes.add(x);
            }
        }
        out.println("rope(length=" + r.length() + ", leaf nodes=" + leafNodes.size() + ", non-leaf nodes=" + nonLeaf + ", depth=" + RopeUtilities.INSTANCE.depth(r) + ")");
    }

}
