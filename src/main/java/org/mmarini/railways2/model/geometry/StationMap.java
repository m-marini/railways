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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Tracks the nodes of the station map
 */
public class StationMap {
    private final String id;
    private final Map<String, Node> nodeMap;
    private final Map<String, Edge> edges;

    /**
     * Creates the station map
     *
     * @param id      the station identifier
     * @param nodeMap the nodes
     */
    public StationMap(String id, Map<String, Node> nodeMap) {
        this.id = requireNonNull(id);
        this.nodeMap = requireNonNull(nodeMap);
        edges = nodeMap.values().stream().flatMap(
                        n -> Stream.of(n.getEdges())
                ).distinct()
                .collect(Collectors.toMap(Edge::getId, Function.identity()));
    }

    /**
     * Returns the edge by identifier
     *
     * @param id the identifier
     * @throws IllegalArgumentException if edge not found
     */
    public <T extends Edge> T getEdge(String id) {
        Edge edge = edges.get(id);
        if (edge == null) {
            throw new IllegalArgumentException(format("Edge %s not found", id));
        } else {
            return (T) edge;
        }
    }

    /**
     * Returns the map of edges
     */
    public Map<String, Edge> getEdges() {
        return edges;
    }

    /**
     * Returns the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the node by identifier
     *
     * @param id the identifier
     * @throws IllegalArgumentException if node is not found
     */
    public Node getNode(String id) {
        Node node = getNodeMap().get(id);
        if (node == null) {
            throw new IllegalArgumentException(format("Node %s not found", id));
        } else {
            return node;
        }
    }

    /**
     * Returns the node map
     */
    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }
}
