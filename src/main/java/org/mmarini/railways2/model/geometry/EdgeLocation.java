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

import static java.util.Objects.requireNonNull;

/**
 * Stores the location of movement in the edge
 */
public class EdgeLocation {
    /**
     * Create the location in the edge
     *
     * @param edge        the edge
     * @param destination the destination node
     * @param distance    the distance
     */
    public static EdgeLocation create(Edge edge, Node destination, double distance) {
        return new EdgeLocation(new Direction(edge, destination), distance);
    }

    private final Direction direction;
    private final double distance;

    /**
     * Creates the location
     *
     * @param direction the direction
     * @param distance  the distance to destination  (m)
     */
    public EdgeLocation(Direction direction, double distance) {
        this.direction = requireNonNull(direction);
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeLocation that = (EdgeLocation) o;
        return Double.compare(that.distance, distance) == 0 && direction.equals(that.direction);
    }

    /**
     * Returns the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Returns the distance to the destination
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Returns the location at a give distance to the destination
     *
     * @param distance the distance (m)
     */
    public EdgeLocation setDistance(double distance) {
        return new EdgeLocation(direction, distance);
    }

    /**
     * Returns the location of the edge point
     */
    public Point2D getLocation() {
        return getDirection().getEdge().getLocation(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, distance);
    }

    /**
     * Returns same the location but in opposite direction
     */
    public EdgeLocation opposite() {
        return new EdgeLocation(direction.opposite(), direction.getEdge().getLength() - distance);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EdgeLocation.class.getSimpleName() + "[", "]")
                .add("direction=" + direction)
                .add("distance=" + distance)
                .toString();
    }
}
