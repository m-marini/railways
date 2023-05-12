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

package org.mmarini.railways1.model.routes;

import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.OrientedLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Describes the direction of route
 */
public class RouteDirection {
    private final SingleNodeRoute route;
    private final int index;

    /**
     * Create the side of route
     *
     * @param route the route
     * @param index the index
     */
    public RouteDirection(SingleNodeRoute route, int index) {
        this.route = requireNonNull(route);
        this.index = index;
    }

    /**
     * Returns the connected direction
     */
    public Optional<RouteDirection> connectedDirection() {
        return route.getConnectedDirection(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteDirection routeDirection = (RouteDirection) o;
        return index == routeDirection.index && route.equals(routeDirection.route);
    }

    /**
     * Returns the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the edge point of direction
     */
    public Optional<OrientedLocation> getLocation() {
        return route.getEdge(index).map(edge -> {
            Node node = route.getNode();
            boolean direct = edge.getNode0().equals(node);
            return new OrientedLocation(edge, direct, 0);
        });
    }

    /**
     * Returns the route
     */
    public SingleNodeRoute getRoute() {
        return route;
    }

    /**
     * Returns the terminal node of direction
     */
    public Optional<Node> getTerminal() {
        return getLocation().map(OrientedLocation::getTerminal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route, index);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RouteDirection.class.getSimpleName() + "[", "]")
                .add(route + "," + index)
                .toString();
    }
}
