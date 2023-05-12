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
import org.mmarini.railways2.model.geometry.Node;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Connects the two edges of a node in both the directions
 */
public class Junction extends AbstractSingleNodeRoute {
    public static final int NUM_CONNECTIONS = 2;

    /**
     * Creates the junction
     *
     * @param nodes the nodes
     */
    public static Junction create(Node... nodes) {
        requireNonNull(nodes);
        int numNodes = nodes.length;
        if (numNodes != 1) {
            throw new IllegalArgumentException(format(
                    "Junction requires 1 node (%d)",
                    numNodes));
        }
        Node node = nodes[0];
        int size = node.getEdges().size();
        if (size != NUM_CONNECTIONS) {
            throw new IllegalArgumentException(format(
                    "Route %s requires %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    size));
        }
        List<Direction> dirs = node.getExits();
        Direction dir0 = dirs.get(0);
        Direction dir1 = dirs.get(1);
        Map<Direction, Direction> exitByEntry = Map.of(
                dir0.opposite(), dir1,
                dir1.opposite(), dir0
        );
        return new Junction(node, exitByEntry);
    }

    /**
     * Creates the junction
     *
     * @param node        the node
     * @param exitByEntry the connection map
     */
    protected Junction(Node node, Map<Direction, Direction> exitByEntry) {
        super(node, exitByEntry);
    }
}
