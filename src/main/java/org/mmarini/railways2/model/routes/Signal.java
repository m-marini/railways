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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.Node;

import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.yaml.Utils.objectMapper;

/**
 * Dissects two edges in boh direction
 * <p>
 * The signal can be locked in the entry directions
 * </p>
 */
public class Signal extends AbstractSingleNodeRoute implements SectionTerminal {

    public static final int NUM_CONNECTIONS = 2;

    /**
     * Returns the unlocked signal
     *
     * @param nodes the nodes
     */
    public static Signal create(Node... nodes) {
        requireNonNull(nodes);
        int numNodes = nodes.length;
        if (numNodes != 1) {
            throw new IllegalArgumentException(format(
                    "Signals requires 1 nodes (%d)",
                    numNodes));
        }
        Node node = nodes[0];
        int numEdges = node.getExits().size();
        if (numEdges != NUM_CONNECTIONS) {
            throw new IllegalArgumentException(format(
                    "Route %s requires %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    numEdges));
        }

        List<Direction> exits = node.getExits();
        Direction exit0 = exits.get(0);
        Direction exit1 = exits.get(1);
        Direction entry0 = exit0.opposite();
        Direction entry1 = exit1.opposite();

        return new Signal(node, Set.of(),
                Map.of(
                        entry0, exit1,
                        entry1, exit0
                ));
    }

    /**
     * Returns the function creating signal locked in the given entry directions
     *
     * @param locks the locked entry directions
     */
    public static Function<Node[], Signal> createLocks(Direction... locks) {
        return nodes -> {
            Signal signal = create(nodes);
            for (Direction lock : locks) {
                signal = signal.lock(lock);
            }
            return signal;
        };
    }

    private final Set<Direction> locks;

    /**
     * Create the signal
     *
     * @param node        the node
     * @param locks       the lock entry directions
     * @param exitByEntry the connections map
     */
    protected Signal(Node node, Set<Direction> locks, Map<Direction, Direction> exitByEntry) {
        super(node, exitByEntry);
        this.locks = requireNonNull(locks);
    }

    @Override
    public Optional<Direction> getExit(Direction direction) {
        return Optional.ofNullable(exitByEntry.get(direction));
    }

    @Override
    public ObjectNode getJson() {
        ObjectNode result = super.getJson();
        ArrayNode locksNode = objectMapper.createArrayNode();
        locks.stream()
                .map(Direction::getEdge)
                .map(Edge::getId)
                .forEach(locksNode::add);
        result.set("locks", locksNode);
        return result;
    }

    /**
     * Returns true id the exit is locked
     *
     * @param exit the exit
     */
    public boolean isExitLocked(Direction exit) {
        return Tuple2.stream(exitByEntry)
                .anyMatch(t -> t._2.equals(exit) && isLocked(t._1));
    }

    /**
     * Returns true if the signal is locked in the entry direction
     *
     * @param direction the direction
     */
    public boolean isLocked(Direction direction) {
        return locks.contains(direction);
    }

    /**
     * Returns the signal locked in the given entry direction
     *
     * @param direction the direction
     */
    public Signal lock(Direction direction) {
        if (isLocked(direction)) {
            return this;
        } else {
            validateLock(direction);
            Set<Direction> newLocks = new HashSet<>(locks);
            newLocks.add(direction);
            return new Signal(node, newLocks, exitByEntry);
        }
    }

    /**
     * Returns the signal with locks
     *
     * @param locks the locks
     */
    public Signal setLocks(Direction... locks) {
        for (Direction lock : locks) {
            validateLock(lock);
        }
        return new Signal(node, Set.of(locks), exitByEntry);
    }

    /**
     * Returns the signal unlocked in the given entry direction
     *
     * @param direction the direction
     */
    public Signal unlock(Direction direction) {
        if (!isLocked(direction)) {
            return this;
        } else {
            Set<Direction> newLocks = new HashSet<>(locks);
            newLocks.remove(direction);
            return new Signal(node, newLocks, exitByEntry);
        }
    }

    /**
     * Validates direction for entry
     *
     * @param direction the direction
     * @throws IllegalArgumentException in case of invalid direction
     */
    private void validateLock(Direction direction) {
        if (!(exitByEntry.containsKey(direction))) {
            throw new IllegalArgumentException(format("Invalid direction %s for route %s",
                    direction,
                    getId()));
        }
    }
}
