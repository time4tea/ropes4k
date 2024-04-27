/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import net.ropes4k.Rope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serial;

/**
 * An instance of this class replaces ropes during the serialization
 * process. This class serializes into string form, and deserializes
 * into a <code>FlatRope</code>. <code>readResolve</code> returns the
 * flat rope.
 * <p>
 * The purpose of this class is to provide a performant serialization
 * mechanism for Ropes. The ideal serial form of a rope is as a String,
 * regardless of the particular in-memory representation.
 *
 * @author Amin Ahmad
 */
final class SerializedRope implements Externalizable {

    /**
     * The rope.
     */
    private Rope rope;

    /**
     * Public no-arg constructor for use during serialization.
     */
    public SerializedRope() {
    }

    public SerializedRope(Rope rope) {
        this.rope = rope;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        // Read the UTF string and build a rope from it. This should
        // result in a FlatRope.
        rope = Rope.BUILDER.build(in.readUTF());
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
        // Substitute an instance of this class with the deserialized
        // rope.
        return rope;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // Evaluate the rope (toString()) and write as UTF. Unfortunately,
        // this requires O(n) temporarily-allocated heap space.
        out.writeUTF(rope.toString());
    }
}
