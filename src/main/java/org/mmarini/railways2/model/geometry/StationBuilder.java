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

import org.mmarini.Tuple2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Builds station by adding definitions
 */
public class StationBuilder {
    public static final Rectangle2D.Double EMPTY_BOUND = new Rectangle2D.Double(-5, -5, 10, 10);
    private final String id;
    private final Map<String, Node> nodes;
    private final Map<String, String[]> nodeEdges;
    private final List<BiFunction<Node, Node, ? extends Edge>> edgeBuilders;
    private final List<String[]> edgeNodes;


    /**
     * Creates the builder
     *
     * @param id the station identifier
     */
    public StationBuilder(String id) {
        this.id = requireNonNull(id);
        this.nodes = new HashMap<>();
        this.nodeEdges = new HashMap<>();
        this.edgeBuilders = new ArrayList<>();
        this.edgeNodes = new ArrayList<>();
    }

    /**
     * Returns the station builder with a nde edge
     *
     * @param builder the edge builder
     * @param node0   the node0 identifier
     * @param node1   the node1 identifier
     */
    public StationBuilder addEdge(BiFunction<Node, Node, Edge> builder, String node0, String node1) {
        this.edgeBuilders.add(requireNonNull(builder));
        this.edgeNodes.add(new String[]{requireNonNull(node0), requireNonNull(node1)});
        return this;
    }

    /**
     * Returns the builder with a new node
     *
     * @param id       the node identifier
     * @param location the node location
     * @param edges    the edge identifier list
     */
    public StationBuilder addNode(String id, Point2D.Double location, String... edges) {
        requireNonNull(id);
        requireNonNull(location);
        requireNonNull(edges);
        if (nodes.containsKey(id)) {
            throw new IllegalArgumentException(format("Node %s already defined", id));
        }
        nodes.put(id, new Node(id, location));
        nodeEdges.put(id, edges);
        return this;
    }

    /**
     * Returns the built station
     */
    public StationMap build() {
        // Creates edges
        Map<String, Edge> edgeMap = IntStream.range(0, edgeBuilders.size())
                .mapToObj(i -> Tuple2.of(
                        edgeBuilders.get(i),
                        edgeNodes.get(i)))
                .map(t -> {
                    Node node0 = nodes.get(t._2[0]);
                    if (node0 == null) {
                        throw new IllegalArgumentException(format("Node %s not found", t._2[0]));
                    }
                    Node node1 = nodes.get(t._2[1]);
                    if (node1 == null) {
                        throw new IllegalArgumentException(format("Node %s not found", t._2[1]));
                    }
                    Edge edge = t._1.apply(node0, node1);
                    return Tuple2.of(edge.getId(), edge);
                })
                .collect(Tuple2.toMap());

        // Sets the node edges
        for (Node node : nodes.values()) {
            List<Edge> edges = Arrays.stream(nodeEdges.get(node.getId())).map(
                    edgeId -> {
                        Edge edge = edgeMap.get(edgeId);
                        if (edge == null) {
                            throw new IllegalArgumentException(format("Edge %s not found for node %s", edgeId, node.getId()));
                        }
                        return edge;
                    }
            ).collect(Collectors.toList());
            node.setEdges(edges);
        }
        Rectangle2D bounds = edgeMap.values().stream()
                .reduce(null,
                        (rect, edges) -> {
                            Rectangle2D bounds1 = edges.getBounds();
                            return rect == null ? bounds1 : rect.createUnion(bounds1);
                        },
                        (a, b) -> a == null ? b : a.createUnion(b));

        return new StationMap(id, nodes, bounds != null ? bounds : EMPTY_BOUND);
    }
}
