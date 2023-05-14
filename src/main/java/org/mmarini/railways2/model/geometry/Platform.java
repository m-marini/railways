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
import java.util.function.BiFunction;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

/**
 * Connects two points for train transit and unload/load passengers
 */
public class Platform extends Track {
    /**
     * Returns the platform builder
     *
     * @param id the platform identifier
     */
    public static BiFunction<Node, Node, Edge> builder(String id) {
        return (node0, node1) -> Platform.create(id, node0, node1);
    }

    /**
     * Create the edge
     *
     * @param id    the edge identifier
     * @param node0 the first node
     * @param node1 the second node
     */
    public static Platform create(String id, Node node0, Node node1) {
        requireNonNull(node0);
        requireNonNull(node1);
        Point2D p0 = node0.getLocation();
        Point2D p1 = node1.getLocation();
        double x0 = p0.getX();
        double x1 = p1.getX();
        double y0 = p0.getY();
        double y1 = p1.getY();
        double length = p0.distance(p1);
        Rectangle2D bounds = new Rectangle2D.Double(
                min(x0, x1),
                min(y0, y1),
                abs(x1 - x0),
                abs(y1 - y0));
        return new Platform(id, node0, node1, length, bounds);
    }

    /**
     * Create the edge
     *
     * @param id     the edge identifier
     * @param node0  the first node
     * @param node1  the second node
     * @param length the length of platform
     * @param bounds the bounds of platform
     */
    protected Platform(String id, Node node0, Node node1, double length, Rectangle2D bounds) {
        super(id, node0, node1, length, bounds);
    }
}
