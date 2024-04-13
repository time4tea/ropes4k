/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package org.ahmadsoft.ropes.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

import org.ahmadsoft.ropes.Rope;

/**
 * An instance of this class replaces ropes during the serialization
 * process. This class serializes into string form, and deserializes
 * into a <code>FlatRope</code>. <code>readResolve</code> returns the
 * flat rope.
 * <p>
 * The purpose of this class is to provide a performant serialization
 * mechanism for Ropes. The ideal serial form of a rope is as a String,
 * regardless of the particular in-memory representation.
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
	public SerializedRope() {}

	/**
	 * Create a new concatenation rope from two ropes.
	 * @param left the first rope.
	 * @param right the second rope.
	 */
	public SerializedRope(final Rope rope) {
		this.rope = rope;
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		// Read the UTF string and build a rope from it. This should
		// result in a FlatRope.
		this.rope = Rope.BUILDER.build(in.readUTF());
	}

	private Object readResolve() throws ObjectStreamException {
		// Substitute an instance of this class with the deserialized
		// rope.
		return this.rope;
	}

	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		// Evaluate the rope (toString()) and write as UTF. Unfortunately,
		// this requires O(n) temporarily-allocated heap space.
		out.writeUTF(this.rope.toString());
	}
}
