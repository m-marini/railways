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

package org.mmarini.railways2.model.routes;

import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.Node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Connects the edges in pair with cross track
 * 0 --\  /-- 3
 * X
 * 2 --/ \ -- 1
 */
public class CrossRoute extends AbstractSingleNodeRoute {

    public static final int NUM_CONNECTIONS = 4;

    /**
     * Returns the cross route
     *
     * @param nodes the nodes
     */
    public static CrossRoute create(Node... nodes) {
        requireNonNull(nodes);
        int numNodes = nodes.length;
        if (numNodes != 1) {
            throw new IllegalArgumentException(format(
                    "Switch requires 1 nodes (%d)",
                    numNodes));
        }
        Node node = nodes[0];
        requireNonNull(node);
        int size = node.getEdges().size();
        if (size != NUM_CONNECTIONS) {
            throw new IllegalArgumentException(format(
                    "Required node %s with %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    size));
        }
        Direction exit0 = node.getExits().get(0);
        Direction exit1 = node.getExits().get(1);
        Direction exit2 = node.getExits().get(2);
        Direction exit3 = node.getExits().get(3);
        Direction entry0 = exit0.opposite();
        Direction entry1 = exit1.opposite();
        Direction entry2 = exit2.opposite();
        Direction entry3 = exit3.opposite();
        return new CrossRoute(node, Map.of(
                entry0, exit1,
                entry1, exit0,
                entry2, exit3,
                entry3, exit2
        ), Map.of(
                entry0, List.of(exit2.getEdge(), exit3.getEdge()),
                entry1, List.of(exit2.getEdge(), exit3.getEdge()),
                entry2, List.of(exit0.getEdge(), exit1.getEdge()),
                entry3, List.of(exit0.getEdge(), exit1.getEdge())
        ));
    }

    private final Map<Direction, Collection<Edge>> crossingEdges;

    /**
     * Creates a cross route
     *
     * @param node          the node
     * @param exitByEntry   the connection map
     * @param crossingEdges the crossing edges
     */
    protected CrossRoute(Node node, Map<Direction, Direction> exitByEntry, Map<Direction, Collection<Edge>> crossingEdges) {
        super(node, exitByEntry);
        this.crossingEdges = crossingEdges;
    }

    @Override
    public Collection<Edge> getCrossingEdges(Direction direction) {
        Collection<Edge> edges = crossingEdges.get(direction);
        return edges != null ? edges : List.of();
    }
}
