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

package org.mmarini.railways2.model.route;

import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.Node;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Route {
    /**
     * Returns the edge connection for a given index.
     * <p>
     * The returned edge depends on the route status,
     * e.g. switches return different edge dependening on direct or deviated status
     * </p>
     *
     * @param index the index
     */
    default Optional<RouteDirection> getConnectedDirection(int index) {
        return getDirection(getConnectedIndex(index));
    }

    /**
     * Returns the connection index for a given index
     *
     * @param index the index
     */
    default int getConnectedIndex(int index) {
        return -1;
    }

    /**
     * Returns the direction for a given index.
     *
     * @param index the edge index
     */
    default Optional<RouteDirection> getDirection(int index) {
        if (index >= 0 && index < getNumDirections()) {
            return Optional.of(new RouteDirection(this, index));
        } else {
            return Optional.empty();
        }
    }

    default Stream<RouteDirection> getDirections() {
        return IntStream.range(0, getNumDirections())
                .mapToObj(i -> new RouteDirection(this, i));
    }

    /**
     * Returns the edge of the direction
     *
     * @param index the direction index
     */
    default Optional<Edge> getEdge(int index) {
        return index >= 0 && index <= getNumDirections() ?
                Optional.of(getNode().getEdges()[index]) :
                Optional.empty();
    }

    /**
     * Returns the identifier
     */
    String getId();

    /**
     * Returns the node
     */
    Node getNode();

    /**
     * Returns the number of connection
     */
    int getNumDirections();

    default Stream<RouteDirection> getValidDirections() {
        return getDirections();
    }

    /**
     * Returns the index directin for a given index.
     *
     * @param edge the edge
     */
    default int indexOf(Edge edge) {
        Edge[] edges = getNode().getEdges();
        for (int i = 0; i < edges.length; i++) {
            if (edges[i].equals(edge)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if direction at index is locked
     *
     * @param index index
     */
    default boolean isLocked(int index) {
        return false;
    }
}
