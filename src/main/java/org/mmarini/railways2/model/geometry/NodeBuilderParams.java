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
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * The parameters for node building
 */
public class NodeBuilderParams {
    /**
     * Returns the node builder
     *
     * @param id    the node identifier
     * @param x     the x coordinate of node
     * @param y     the y coordinate of node
     * @param edges the edge identifier list
     */
    public static NodeBuilderParams create(String id, double x, double y, String... edges) {
        return new NodeBuilderParams(id, new Point2D.Double(x, y), Arrays.asList(edges));
    }

    private final String id;
    private final Point2D location;
    private final List<String> edges;

    /**
     * Creates the node builders
     *
     * @param id       the node identifier
     * @param location the node location
     * @param edges    the edge identifier list
     */
    protected NodeBuilderParams(String id, Point2D location, List<String> edges) {
        this.id = requireNonNull(id);
        this.location = requireNonNull(location);
        this.edges = requireNonNull(edges);
    }

    /**
     * Returns the node
     */
    public Node buildNode() {
        return new Node(id, location);
    }

    /**
     * Returns the edge identifier list
     */
    public List<String> getEdges() {
        return edges;
    }

    /**
     * Returns the node builder with different edges
     *
     * @param edges the edges
     */
    public NodeBuilderParams setEdges(List<String> edges) {
        return this.edges.equals(edges) ? this : new NodeBuilderParams(id, location, edges);
    }

    /**
     * The node identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the node builder with different node identifier
     *
     * @param id the node identifier
     */
    public NodeBuilderParams setId(String id) {
        return this.id.equals(id) ? this : new NodeBuilderParams(id, location, edges);
    }

    /**
     * Returns the location
     */
    public Point2D getLocation() {
        return location;
    }

    /**
     * Returns the node builder with different node location
     *
     * @param location the node location
     */
    public NodeBuilderParams setLocation(Point2D location) {
        return this.location.equals(location) ? this : new NodeBuilderParams(id, location, edges);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NodeBuilderParams.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .toString();
    }
}
