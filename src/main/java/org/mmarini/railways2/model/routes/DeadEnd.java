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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Stops the train at the end of edge
 */
public class DeadEnd extends AbstractSingleNodeRoute implements SectionTerminal{
    public static final int NUM_CONNECTIONS = 1;

    /**
     * Returns the entry by node
     *
     * @param nodes the nodes
     */
    public static DeadEnd create(Node... nodes) {
        requireNonNull(nodes);
        int numNodes = nodes.length;
        if (numNodes != NUM_CONNECTIONS) {
            throw new IllegalArgumentException(format(
                    "DeadEnd requires %d node (%d)",
                    NUM_CONNECTIONS,
                    numNodes));
        }
        return new DeadEnd(nodes[0], Map.of());
    }

    /**
     * Creates the dead end route
     *
     * @param node        the node
     * @param exitByEntry the connection map
     */
    protected DeadEnd(Node node, Map<Direction, Direction> exitByEntry) {
        super(node, exitByEntry);
    }

    @Override
    public Collection<Direction> getValidExits() {
        return List.of();
    }
}
