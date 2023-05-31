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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Builds station by adding definitions
 */
public class StationBuilder {
    public static final Rectangle2D.Double EMPTY_BOUND = new Rectangle2D.Double(-5, -5, 10, 10);
    private final String id;
    private final List<EdgeBuilderParams> edgeBuilderParams;
    private final List<NodeBuilderParams> nodeBuilders;


    /**
     * Creates the builder
     *
     * @param id the station identifier
     */
    public StationBuilder(String id) {
        this.id = requireNonNull(id);
        this.nodeBuilders = new ArrayList<>();
        this.edgeBuilderParams = new ArrayList<>();
    }

    /**
     * Returns the builder with a new platform
     *
     * @param id    the platform identifier
     * @param angle the curve angle (RAD)
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     */
    public StationBuilder addCurve(String id, double angle, String node0, String node1) {
        return addEdge(EdgeBuilderParams.curve(id, node0, node1, angle));
    }

    /**
     * Returns the station builder with a new edge
     *
     * @param edgeBuilderParams the edge builder
     */
    public StationBuilder addEdge(EdgeBuilderParams edgeBuilderParams) {
        this.edgeBuilderParams.add(edgeBuilderParams);
        return this;
    }

    /**
     * Returns the builder with a new node
     *
     * @param id    the node identifier
     * @param x     the x coordinate (m)
     * @param y     the y coordinate (m)
     * @param edges the edge identifier list
     */
    public StationBuilder addNode(String id, double x, double y, String... edges) {
        return addNode(new NodeBuilderParams(id, new Point2D.Double(x, y), Arrays.asList(edges)));
    }

    /**
     * Returns the builder with a new node
     *
     * @param params the node builder parameters
     */
    public StationBuilder addNode(NodeBuilderParams params) {
        nodeBuilders.add(requireNonNull(params));
        return this;
    }

    /**
     * Returns the builder with a new node
     *
     * @param id       the node identifier
     * @param location the node location
     * @param edges    the edge identifier list
     */
    public StationBuilder addNode(String id, Point2D location, String... edges) {
        return addNode(new NodeBuilderParams(id, location, Arrays.asList(edges)));
    }

    /**
     * Returns the builder with a new platform
     *
     * @param id    the platform identifier
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     */
    public StationBuilder addPlatform(String id, String node0, String node1) {
        return addEdge(EdgeBuilderParams.platform(id, node0, node1));
    }

    /**
     * Returns the builder with a new track
     *
     * @param id    the track identifier
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     */
    public StationBuilder addTrack(String id, String node0, String node1) {
        return addEdge(EdgeBuilderParams.track(id, node0, node1));
    }

    /**
     * Returns the built station
     */
    public StationMap build() {
        // Creates nodes
        Map<String, Node> nodeById = nodeBuilders.stream().map(NodeBuilderParams::buildNode)
                .collect(Collectors.toMap(Node::getId, Function.identity()));

        // Creates edges
        Map<String, Edge> edgeById = edgeBuilderParams.stream().map(params -> {
            Node node0 = nodeById.get(params.getNode0());
            if (node0 == null) {
                throw new IllegalArgumentException(format("Node %s not found", params.getNode0()));
            }
            Node node1 = nodeById.get(params.getNode1());
            if (node1 == null) {
                throw new IllegalArgumentException(format("Node %s not found", params.getNode1()));
            }
            return params.getBuilder().apply(node0, node1);
        }).collect(Collectors.toMap(
                Edge::getId, Function.identity()
        ));

        // Sets the node edges
        for (NodeBuilderParams params : nodeBuilders) {
            Node node = nodeById.get(params.getId());
            List<Edge> edges = params.getEdges().stream()
                    .map(edgeId -> {
                        Edge edge = edgeById.get(edgeId);
                        if (edge == null) {
                            throw new IllegalArgumentException(format("Edge %s not found for node %s", edgeId, node.getId()));
                        }
                        return edge;
                    }).collect(Collectors.toList());
            node.setEdges(edges);
        }
        Rectangle2D bounds = edgeById.values().stream()
                .reduce(null,
                        (rect, edges) -> {
                            Rectangle2D bounds1 = edges.getBounds();
                            return rect == null ? bounds1 : rect.createUnion(bounds1);
                        },
                        (a, b) -> a == null ? b : a.createUnion(b));

        return new StationMap(id, nodeById, bounds != null ? bounds : EMPTY_BOUND);
    }
}
