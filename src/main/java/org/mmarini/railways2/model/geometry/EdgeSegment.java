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

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Defines the terminal points in the segment of edge
 */
public class EdgeSegment {
    /**
     * Returns the edge segment from the location for a given distance
     *
     * @param location the location
     * @param distance the distance
     */
    public static EdgeSegment create(EdgeLocation location, double distance) {
        Direction direction = location.getDirection();
        Edge edge = direction.getEdge();
        double length = edge.getLength();
        Node to = direction.getDestination();
        double d0 = location.getDistance();
        double d1 = d0 - distance;
        return edge.getNode0().equals(to) ?
                new EdgeSegment(edge, d1, length - d0) :
                new EdgeSegment(edge, length - d0, d1);
    }

    /**
     * Returns the full segment of an edge
     *
     * @param edge the edge
     */
    public static EdgeSegment createFullSegment(Edge edge) {
        return new EdgeSegment(edge, 0, 0);
    }

    private final Edge edge;
    private final double distance0;
    private final double distance1;

    /**
     * Creates the edge segment
     *
     * @param edge      the edge
     * @param distance0 the distance from node0
     * @param distance1 the distance from node1
     */
    public EdgeSegment(Edge edge, double distance0, double distance1) {
        this.edge = requireNonNull(edge);
        this.distance0 = distance0;
        this.distance1 = distance1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeSegment that = (EdgeSegment) o;
        return Double.compare(that.distance0, distance0) == 0 && Double.compare(that.distance1, distance1) == 0 && edge.equals(that.edge);
    }

    /**
     * Returns the distance from node0
     */
    public double getDistance0() {
        return distance0;
    }


    /**
     * Returns the distance from node1
     */
    public double getDistance1() {
        return distance1;
    }

    /**
     * Returns the edge
     */
    public <T extends Edge> T getEdge() {
        return (T) edge;
    }

    /**
     * Returns the location of terminal0
     */
    public EdgeLocation getLocation0() {
        return EdgeLocation.create(edge, edge.getNode0(), distance0);
    }

    /**
     * Returns the location of terminal1
     */
    public EdgeLocation getLocation1() {
        return EdgeLocation.create(edge, edge.getNode1(), distance1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, distance0, distance1);
    }

    @Override
    public String toString() {
        return new StringBuilder(EdgeSegment.class.getSimpleName())
                .append("[")
                .append(edge.getId())
                .append(", ")
                .append(distance0)
                .append(", ")
                .append(distance1)
                .append("]")
                .toString();
    }
}
