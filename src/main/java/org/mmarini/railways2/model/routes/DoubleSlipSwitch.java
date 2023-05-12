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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Switches routes between the crossing tracks
 * <p>
 * Four nodes are required each with 3 directions.
 * The configuration of node must be
 * <pre>
 * a0 -- a -- ab -- b -- b0
 *         -- ad -- d
 *
 *         -- cb -- b
 * c0 -- c -- cd -- d -- d0
 * </pre>
 *
 * <p>
 * The through connections are
 * <pre>
 *  a0 -- a -- ab -- b -- b0
 *  c0 -- c -- cd -- d -- d0
 * </pre>
 * The diverging connections are
 * <pre>
 *  a0 -- a -- ad -- d -- d0
 *  c0 -- c -- cb -- b -- b0
 * </pre>
 * </p>
 */
public class DoubleSlipSwitch implements Route {

    /**
     * Creates the double slip switch in deviated configuration.
     *
     * @param nodes the nodes of switch
     */
    public static DoubleSlipSwitch diverging(Node... nodes) {
        validateNodes(nodes);
        String id = Arrays.stream(nodes).map(Node::getId).min(String::compareTo).orElseThrow();
        Node a = nodes[0];
        Node b = nodes[1];
        Node c = nodes[2];
        Node d = nodes[3];
        /*
         *  a0 -- a -- ad -- d -- d0
         *  c0 -- c -- cb -- b -- b0
         */
        Edge a0 = a.getEdges().get(0);
        Edge b0 = b.getEdges().get(0);
        Edge c0 = c.getEdges().get(0);
        Edge d0 = d.getEdges().get(0);
        Edge ad = a.getEdges().get(2);
        Edge cb = c.getEdges().get(2);
        Direction _0a = new Direction(a0, a);
        Direction _0b = new Direction(b0, b);
        Direction _0c = new Direction(c0, c);
        Direction _0d = new Direction(d0, d);
        Direction _ad = new Direction(ad, d);
        Direction _cb = new Direction(cb, b);
        Direction _a0 = _0a.opposite();
        Direction _b0 = _0b.opposite();
        Direction _c0 = _0c.opposite();
        Direction _d0 = _0d.opposite();
        Direction _da = _ad.opposite();
        Direction _bc = _cb.opposite();
        Map<Direction, Direction> exitByDirection = Map.of(
                _0a, _ad,
                _ad, _d0,
                _0d, _da,
                _da, _a0,
                _0c, _cb,
                _cb, _b0,
                _0b, _bc,
                _bc, _c0
        );
        Collection<Direction> exits = exitByDirection.values();
        Set<? extends Edge> crossAD = Set.of(c0, b0);
        Set<? extends Edge> crossBC = Set.of(a0, d0);
        Map<Direction, Collection<? extends Edge>> crossingEdgeByDirection = Map.of(
                _0a, crossAD,
                _0d, crossAD,
                _0b, crossBC,
                _0c, crossBC
        );
        return new DoubleSlipSwitch(id, List.of(nodes), false, exits, exitByDirection, crossingEdgeByDirection);
    }

    /**
     * Creates the double slip switch in through configuration.
     *
     * @param nodes the nodes of switch
     */
    public static DoubleSlipSwitch through(Node... nodes) {
        validateNodes(nodes);
        String id = Arrays.stream(nodes).map(Node::getId).min(String::compareTo).orElseThrow();
        Node a = nodes[0];
        Node b = nodes[1];
        Node c = nodes[2];
        Node d = nodes[3];
        /*
         *  a0 -- a -- ab -- b -- b0
         *  c0 -- c -- cd -- d -- d0
         */
        Edge a0 = a.getEdges().get(0);
        Edge b0 = b.getEdges().get(0);
        Edge c0 = c.getEdges().get(0);
        Edge d0 = d.getEdges().get(0);
        Edge ab = a.getEdges().get(1);
        Edge cd = c.getEdges().get(1);
        Direction _0a = new Direction(a0, a);
        Direction _0b = new Direction(b0, b);
        Direction _0c = new Direction(c0, c);
        Direction _0d = new Direction(d0, d);
        Direction _ab = new Direction(ab, b);
        Direction _cd = new Direction(cd, d);
        Direction _a0 = _0a.opposite();
        Direction _b0 = _0b.opposite();
        Direction _c0 = _0c.opposite();
        Direction _d0 = _0d.opposite();
        Direction _ba = _ab.opposite();
        Direction _dc = _cd.opposite();
        Map<Direction, Direction> exitByDirection = Map.of(
                _0a, _ab,
                _ab, _b0,
                _0b, _ba,
                _ba, _a0,
                _0c, _cd,
                _cd, _d0,
                _0d, _dc,
                _dc, _c0
        );
        Collection<Direction> exits = exitByDirection.values();
        Set<? extends Edge> crossAB = Set.of(c0, d0);
        Set<? extends Edge> crossCD = Set.of(a0, b0);
        Map<Direction, Collection<? extends Edge>> crossingEdgeByDirection = Map.of(
                _0a, crossAB,
                _0b, crossAB,
                _0c, crossCD,
                _0d, crossCD
                );
        return new DoubleSlipSwitch(id, List.of(nodes), true, exits, exitByDirection, crossingEdgeByDirection);
    }

    /**
     * Validates node
     *
     * @param nodes the nodes
     * @throws IllegalArgumentException if nodes are not valid
     */
    private static void validateNodes(Node... nodes) {
        requireNonNull(nodes);
        int numNodes = nodes.length;
        if (numNodes != 4) {
            throw new IllegalArgumentException(format(
                    "DoubleSlipSwitch requires 4 nodes (%d)",
                    numNodes));
        }
        for (Node node : nodes) {
            requireNonNull(node);
            int size = node.getEdges().size();
            if (size != 3) {
                throw new IllegalArgumentException(format(
                        "Double slip switch node %s requires 3 edges (%d)",
                        node.getId(),
                        size));
            }
        }

        /*
         * a0 -- a -- ab -- b -- b0
         *         -- ad -- d
         *
         *         -- cb -- b
         * c0 -- c -- cd -- d -- d0
         *
         * a1 = b1 = ab
         * c1 = d1 = ad
         * a2 = d2 = ad
         * c2 = b2 = cb
         */
        Node a = nodes[0];
        Node b = nodes[1];
        Node c = nodes[2];
        Node d = nodes[3];
        Edge a1 = a.getEdges().get(1);
        Edge a2 = a.getEdges().get(2);
        Edge b1 = b.getEdges().get(1);
        Edge b2 = b.getEdges().get(2);
        Edge c1 = c.getEdges().get(1);
        Edge c2 = c.getEdges().get(2);
        Edge d1 = d.getEdges().get(1);
        Edge d2 = d.getEdges().get(2);
        if (!(a1.equals(b1))) {
            throw new IllegalArgumentException(format("Edge 1 of nodes %s, %s must be equals (%s != %s)",
                    a.getId(), b.getId(),
                    a1.getId(), b1.getId()
            ));
        }
        if (!(c1.equals(d1))) {
            throw new IllegalArgumentException(format("Edge 1 of nodes %s, %s must be equals (%s != %s)",
                    c.getId(), d.getId(),
                    c1.getId(), d1.getId()
            ));
        }
        if (!(a2.equals(d2))) {
            throw new IllegalArgumentException(format("Edge 1 of nodes %s, %s must be equals (%s != %s)",
                    a.getId(), d.getId(),
                    a2.getId(), d2.getId()
            ));
        }
        if (!(c2.equals(b2))) {
            throw new IllegalArgumentException(format("Edge 1 of nodes %s, %s must be equals (%s != %s)",
                    c.getId(), b.getId(),
                    c2.getId(), b2.getId()
            ));
        }
    }

    private final String id;
    private final List<Node> nodes;
    private final boolean through;
    private final Collection<Direction> exits;
    private final Map<Direction, Direction> exitByDirection;
    private final Map<Direction, Collection<? extends Edge>> crossingEdgeByDirection;

    /**
     * Creates the double slip switch
     *
     * @param id                      the identifier
     * @param nodes                   the nodes
     * @param through                 true if the configuration is through
     * @param exits                   the valid exits
     * @param exitByDirection         the exit by direction map
     * @param crossingEdgeByDirection the crossing edge by direction
     */
    protected DoubleSlipSwitch(String id, List<Node> nodes, boolean through, Collection<Direction> exits, Map<Direction, Direction> exitByDirection, Map<Direction, Collection<? extends Edge>> crossingEdgeByDirection) {
        this.id = requireNonNull(id);
        this.nodes = requireNonNull(nodes);
        this.through = through;
        this.exits = requireNonNull(exits);
        this.exitByDirection = requireNonNull(exitByDirection);
        this.crossingEdgeByDirection = crossingEdgeByDirection;
    }

    /**
     * Returns the double slip switch in deviated configuration
     */
    public DoubleSlipSwitch diverging() {
        return through ? diverging(nodes.toArray(Node[]::new)) : this;
    }

    @Override
    public Collection<? extends Edge> getCrossingEdges(Direction direction) {
        Collection<? extends Edge> edges = crossingEdgeByDirection.get(direction);
        return edges != null ? edges : List.of();
    }

    @Override
    public Optional<Direction> getExit(Direction direction) {
        return Optional.ofNullable(exitByDirection.get(direction));
    }

    @Override
    public Collection<Direction> getExits() {
        return exits;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    /**
     * Returns true if is in through configuration
     */
    public boolean isThrough() {
        return through;
    }

    /**
     * Returns the double slip switch in through configuration
     */
    public DoubleSlipSwitch through() {
        return through ? this :
                through(nodes.toArray(Node[]::new));
    }
}
