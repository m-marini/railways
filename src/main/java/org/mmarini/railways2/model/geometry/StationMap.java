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

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.yaml.schema.Validator.nonNegativeNumber;
import static org.mmarini.yaml.schema.Validator.string;

/**
 * Tracks the nodes of the station map
 */
public class StationMap {

    public static final Validator DIRECTION_VALIDATOR = Validator.objectPropertiesRequired(Map.of(
            "edge", string(),
            "destination", string()
    ), List.of("edge", "destination"));

    public static final Validator LOCATION_VALIDATOR = Validator.objectPropertiesRequired(Map.of(
            "edge", string(),
            "destination", string(),
            "distance", nonNegativeNumber()
    ), List.of("edge", "destination", "distance"));
    private final String id;
    private final Map<String, Node> nodeMap;
    private final Map<String, ? extends Edge> edges;
    private final Rectangle2D bounds;

    /**
     * Creates the station map
     *
     * @param id      the station identifier
     * @param nodeMap the nodes
     * @param bounds  the station bound
     */
    protected StationMap(String id, Map<String, Node> nodeMap, Rectangle2D bounds) {
        this.id = requireNonNull(id);
        this.nodeMap = requireNonNull(nodeMap);
        edges = nodeMap.values().stream()
                .flatMap(node -> node.getEdges().stream())
                .distinct()
                .collect(Collectors.toMap(Edge::getId, Function.identity()));
        this.bounds = bounds;
    }

    /**
     * Returns the direction from json document
     *
     * @param root    the document
     * @param locator the locator
     */
    public Direction directionFromJson(JsonNode root, Locator locator) {
        DIRECTION_VALIDATOR.apply(locator).accept(root);
        Edge edge = getEdge(locator.path("edge").getNode(root).asText());
        Node dest = getNode(locator.path("destination").getNode(root).asText());
        return new Direction(edge, dest);
    }

    /**
     * Returns the bounds of the station (m)
     */
    public Rectangle2D getBounds() {
        return bounds;
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
    public Map<String, ? extends Edge> getEdges() {
        return edges;
    }

    /**
     * Returns the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the nearest edge location to the point
     *
     * @param point the point
     */
    public EdgeLocation getNearestLocation(Point2D point) {
        return edges.values().stream()
                .map(edge -> edge.getNearestLocation(point))
                .min(Comparator.comparingDouble(a -> a.getLocation().distance(point)))
                .orElseThrow();
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

    /**
     * Returns the direction from json document
     *
     * @param root    the document
     * @param locator the locator
     */
    public EdgeLocation locationFromJson(JsonNode root, Locator locator) {
        LOCATION_VALIDATOR.apply(locator).accept(root);
        Edge edge = getEdge(locator.path("edge").getNode(root).asText());
        Node dest = getNode(locator.path("destination").getNode(root).asText());
        double distance = locator.path("distance").getNode(root).asDouble();
        return EdgeLocation.create(edge, dest, distance);
    }
}
