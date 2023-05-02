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
 * Describes a node with a set of connecting edges
 */
public class Node {

    private final String id;
    private final Point2D location;
    private Edge[] edges;

    /**
     * Creates the node
     *
     * @param id       the identifier
     * @param location the location
     * @param edges    the edges
     */
    public Node(String id, Point2D location, Edge... edges) {
        this.id = requireNonNull(id);
        this.location = location;
        this.edges = requireNonNull(edges);
        for (Edge edge : edges) {
            requireNonNull(edge);
        }
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
    public Edge[] getEdges() {
        return edges;
    }

    /**
     * Sets the edges of a node
     *
     * @param edges edges
     */
    void setEdges(Edge[] edges) {
        this.edges = edges;
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

    /**
     * Returns the index of edge or -1 if none
     *
     * @param edge the edge
     */
    public int indexOf(Edge edge) {
        for (int i = 0; i < edges.length; i++) {
            if (edges[i].equals(edge)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .toString();
    }
}
