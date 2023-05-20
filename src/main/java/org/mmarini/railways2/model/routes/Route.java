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
import java.util.Optional;

/**
 * Defines the junction of different edges by joining different directions
 * Handles one or more nodes and can have configuration status that defines the connections among the node directions
 */
public interface Route {

    /**
     * Returns the crossing edges for a given direction
     *
     * @param direction the direction
     */
    Collection<? extends Edge> getCrossingEdges(Direction direction);

    /**
     * Returns the connected direction of route
     *
     * @param direction the direction
     */
    Optional<Direction> getExit(Direction direction);

    /**
     * Returns the identifier
     */
    String getId();

    /**
     * Returns the nodes
     */
    List<Node> getNodes();

    /**
     * Returns the valid entries of the route
     */
    Collection<Direction> getValidEntries();

    /**
     * Returns the valid exit of the route
     */
    Collection<Direction> getValidExits();
}
