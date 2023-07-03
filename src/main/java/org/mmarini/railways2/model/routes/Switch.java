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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Node;

import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Switches the edge connection between through or diverging route
 */
public class Switch extends AbstractSingleNodeRoute {

    public static final int NUM_CONNECTIONS = 3;


    /**
     * Returns the function createing the switch in the given configuration
     *
     * @param through true if through configuration
     */
    public static Function<Node[], Switch> create(boolean through) {
        return through ? Switch::through : Switch::diverging;
    }

    /**
     * Returns the diverging switch
     *
     * @param nodes the nodes
     */
    public static Switch diverging(Node... nodes) {
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
        Direction exit2 = node.getExits().get(2);
        Direction entry0 = exit0.opposite();
        Direction entry2 = exit2.opposite();
        return new Switch(node, false, Map.of(
                entry0, exit2,
                entry2, exit0
        ));
    }

    /**
     * Returns the through switch
     *
     * @param nodes the nodes
     */
    public static Switch through(Node... nodes) {
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
        Direction entry0 = exit0.opposite();
        Direction entry1 = exit1.opposite();
        return new Switch(node, true, Map.of(
                entry0, exit1,
                entry1, exit0
        ));
    }

    private final boolean through;

    /**
     * Creates a switch
     *
     * @param node        the node
     * @param through     true if the switch is in through mode (main line)
     * @param exitByEntry the connection map
     */
    protected Switch(Node node, boolean through, Map<Direction, Direction> exitByEntry) {
        super(node, exitByEntry);
        this.through = through;
    }

    /**
     * Returns the diverging switch
     */
    public Switch diverging() {
        return through ? diverging(node) : this;
    }

    @Override
    public ObjectNode getJson() {
        ObjectNode result = super.getJson();
        result.put("through", through);
        return result;
    }

    /**
     * Returns true if the switch is through (main line)
     */
    public boolean isThrough() {
        return through;
    }

    /**
     * Returns the diverging switch
     */
    public Switch through() {
        return through ? this : through(node);
    }
}
