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
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Stores the location and direction of movement in the edge
 */
public class OrientedLocation {
    private final Edge edge;
    private final double distance;
    private final boolean direct;

    /**
     * Creates the edge point
     *
     * @param edge     the edge
     * @param direct   true if origin is node0
     * @param distance the distance (m)
     */
    public OrientedLocation(Edge edge, boolean direct, double distance) {
        this.edge = edge;
        this.distance = distance;
        this.direct = direct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrientedLocation orientedLocation = (OrientedLocation) o;
        return Double.compare(orientedLocation.distance, distance) == 0 && direct == orientedLocation.direct && edge.equals(orientedLocation.edge);
    }

    /**
     * Returns the distance (m)
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the point at distance
     *
     * @param distance distance(m)
     */
    public OrientedLocation setDistance(double distance) {
        return new OrientedLocation(edge, direct, distance);
    }

    /**
     * Returns the edge
     */
    public Edge getEdge() {
        return edge;
    }

    /**
     * Returns the location of the edge point
     */
    public Point2D getLocation() {
        return edge.getLocation(direct, distance);
    }

    /**
     * Returns the origin node
     */
    public Node getOrigin() {
        return direct ? edge.getNode0() : edge.getNode1();
    }

    /**
     * Returns origin index
     */
    public int getOriginIndex() {
        return getOrigin().indexOf(edge);
    }

    /**
     * Returns the terminal node
     */
    public Node getTerminal() {
        return direct ? edge.getNode1() : edge.getNode0();
    }

    /**
     * Returns terminal index
     */
    public int getTerminalIndex() {
        return getTerminalIndex();
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, distance, direct);
    }

    /**
     * Returns true if origin is node0
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * Returns the same point in opposite direction
     */
    public OrientedLocation reverse() {
        return new OrientedLocation(edge, !direct, edge.getLength() - distance);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrientedLocation.class.getSimpleName() + "[", "]")
                .add("edge=" + edge)
                .add("distance=" + distance)
                .add("direct=" + direct)
                .toString();
    }
}
