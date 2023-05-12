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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Describes a node with a set of connecting edges
 */
public class Node {

    private final String id;
    private final Point2D location;
    private List<Edge> edges;
    private List<Direction> directions;

    /**
     * Creates the node
     *
     * @param id       the identifier
     * @param location the location
     */
    public Node(String id, Point2D location) {
        this.id = requireNonNull(id);
        this.location = location;
        this.edges = List.of();
        this.directions = null;
    }

    /**
     * Returns the directions from the node
     */
    private List<Direction> createDirections() {
        return edges.stream().flatMap(edge -> {
            Node node0 = edge.getNode0();
            Node node1 = edge.getNode1();
            return node0 == this ?
                    Stream.of(new Direction(edge, node1)) :
                    node1 == this ?
                            Stream.of(new Direction(edge, node0)) :
                            Stream.of();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id.equals(node.id);
    }

    /**
     * Returns the edges of node
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Sets the edges of a node
     *
     * @param edges edges
     */
    void setEdges(List<Edge> edges) {
        this.edges = edges;
        this.directions = null;
    }

    /**
     * Returns the directions from the node
     */
    public List<Direction> getExits() {
        if (directions == null) {
            // Lazy reference of directions
            directions = createDirections();
        }
        return directions;
    }

    /**
     * Returns the node identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the location
     */
    public Point2D getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringBuilder(Node.class.getSimpleName())
                .append("[")
                .append(id)
                .append("]").toString();
    }
}
