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

package org.mmarini.railways2.model.geometry;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Describes the edge between map points
 */
public interface Edge {

    /**
     * Returns the rectangle bounds of the edge
     */
    Rectangle2D getBounds();

    /**
     * Returns the edge identifier
     */
    String getId();

    /**
     * Returns the length of edge (m)
     */
    double getLength();

    /**
     * Returns the point of the location in the edge
     *
     * @param location the edge location
     */
    Point2D getLocation(EdgeLocation location);

    /**
     * Returns the first node
     */
    Node getNode0();

    /**
     * Returns the second node
     */
    Node getNode1();

    /**
     * Returns the orientation of a point in the edge (RAD)
     *
     * @param location the edge location
     */
    double getOrientation(EdgeLocation location);
}
