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
import java.util.StringJoiner;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Defines the travel direction of an edge
 */
public class Direction {

    private final Edge edge;
    private final Node destination;

    /**
     * Creates the direction
     *
     * @param edge        the edge
     * @param destination the destination node
     */
    public Direction(Edge edge, Node destination) {
        this.edge = requireNonNull(edge);
        this.destination = requireNonNull(destination);
        if (!(edge.getNode0().equals(destination) || edge.getNode1().equals(destination))) {
            throw new IllegalArgumentException(format("destination %s is not a terminal node of edge %s",
                    destination.getId(),
                    edge.getId()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direction direction = (Direction) o;
        return edge.equals(direction.edge) && destination.equals(direction.destination);
    }

    /**
     * Returns the destination node
     */
    public Node getDestination() {
        return destination;
    }

    /**
     * Returns the edge
     */
    public Edge getEdge() {
        return edge;
    }

    /**
     * Returns the origin node
     */
    public Node getOrigin() {
        Node node0 = edge.getNode0();
        return destination.equals(node0) ? edge.getNode1() : node0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, destination);
    }

    /**
     * Returns the opposite direction
     */
    public Direction opposite() {
        Node node0 = edge.getNode0();
        return (node0.equals(destination)) ?
                new Direction(edge, edge.getNode1()) :
                new Direction(edge, node0);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Direction.class.getSimpleName() + "[", "]")
                .add("edge=" + edge)
                .add("destination=" + destination)
                .toString();
    }
}
