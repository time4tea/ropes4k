/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import net.ropes4k.Rope
import java.io.Externalizable
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.ObjectStreamException
import java.io.Serial

/**
 * An instance of this class replaces ropes during the serialization
 * process. This class serializes into string form, and deserializes
 * into a `FlatRope`. `readResolve` returns the
 * flat rope.
 *
 *
 * The purpose of this class is to provide a performant serialization
 * mechanism for Ropes. The ideal serial form of a rope is as a String,
 * regardless of the particular in-memory representation.
 */
internal class SerializedRope : Externalizable {
    private var rope: Rope? = null

    /**
     * Public no-arg constructor for use during serialization.
     */
    @Suppress("unused")
    constructor()

    constructor(rope: Rope) {
        this.rope = rope
    }

    @Throws(IOException::class)
    override fun readExternal(`in`: ObjectInput) {
        rope = Rope.of(`in`.readUTF())
    }

    @Serial
    @Throws(ObjectStreamException::class)
    private fun readResolve(): Any? {
        return rope
    }

    @Throws(IOException::class)
    override fun writeExternal(out: ObjectOutput) {
        // Evaluate the rope (toString()) and write as UTF. Unfortunately,
        // this requires O(n) temporarily-allocated heap space.
        out.writeUTF(rope.toString())
    }
}
