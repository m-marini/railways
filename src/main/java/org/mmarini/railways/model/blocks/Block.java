/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.railways.model.blocks;

import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Optional.empty;

/**
 * A generic block
 */
public interface Block {
    /**
     * Returns the identifier
     */
    String getId();

    /**
     * Returns the index of connection
     *
     * @param text the connection id
     */
    int indexOf(String text);

    /**
     * Returns the location of connection
     *
     * @param index the connection index
     */
    Point2D location(int index);

    /**
     * Returns the location of connection
     *
     * @param text the connection id
     */
    default Optional<Point2D> location(String text) {
        int index = indexOf(text);
        return index >= 0 ? Optional.of(location(index)) : empty();
    }

    /**
     * Returns the orientation (DEG) of connection (entering into the block)
     *
     * @param index the connection index
     */
    int orientation(int index);


    /**
     * Returns the orientation (DEG) of connection
     *
     * @param text the connection id
     */
    default OptionalInt orientation(String text) {
        int index = indexOf(text);
        return index >= 0 ? OptionalInt.of(orientation(index)) : OptionalInt.empty();
    }
}
