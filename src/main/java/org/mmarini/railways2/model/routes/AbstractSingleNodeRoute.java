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

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Defines the junction of different edges by joining different directions.
 * <p>
 * Handles one single node.
 * </p>
 */
public abstract class AbstractSingleNodeRoute implements Route {
    protected final Node node;
    protected final Map<Direction, Direction> exitByEntry;
    private final Collection<Direction> exits;

    /**
     * Creates the abstract single node route
     *
     * @param node        the node
     * @param exitByEntry the connection map
     */
    protected AbstractSingleNodeRoute(Node node, Map<Direction, Direction> exitByEntry) {
        this.node = requireNonNull(node);
        this.exitByEntry = requireNonNull(exitByEntry);
        this.exits = exitByEntry.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSingleNodeRoute that = (AbstractSingleNodeRoute) o;
        return getId().equals(that.getId());
    }

    @Override
    public Collection<Edge> getCrossingEdges(Direction direction) {
        return List.of();
    }

    @Override
    public Optional<Direction> getExit(Direction direction) {
        return Optional.ofNullable(exitByEntry.get(direction));
    }

    @Override
    public Collection<Direction> getExits() {
        return exits;
    }

    @Override
    public String getId() {
        return node.getId();
    }

    @Override
    public Collection<Node> getNodes() {
        return List.of(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
                .append("[")
                .append(getId())
                .append("]")
                .toString();
    }
}
