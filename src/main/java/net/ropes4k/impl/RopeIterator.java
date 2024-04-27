/*
 * Copyright (C) 2024
 *  - Originally Copyright (C) 2007 Amin Ahmad.
 * Licenced under GPL
 */
package net.ropes4k.impl;

import java.util.Iterator;

public interface RopeIterator extends Iterator<Character> {
    /**
     * Returns the position of the last character returned.
     *
     * @return
     */
    int getPosition();
}
