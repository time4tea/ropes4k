/*
 * Copyright (C) 2024 James Richardson
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl

import java.util.Arrays

/**
 * A character sequence defined by a character
 * and a repeat count.
 */
internal class RepeatedCharacterSequence(
    private val character: Char,
    private val repeat: Int
) : CharSequence {
    override fun get(index: Int): Char {
        return character
    }

    override val length = repeat


    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return RepeatedCharacterSequence(character, endIndex - startIndex)
    }

    override fun toString(): String {
        val result = CharArray(repeat)
        Arrays.fill(result, character)
        return String(result)
    }
}
