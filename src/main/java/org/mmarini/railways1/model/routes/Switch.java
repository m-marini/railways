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

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Switches the edge connection between direct route and deviated route
 */
public class Switch extends AbstractSingleNodeRoute {

    public static final int NUM_CONNECTIONS = 3;

    /**
     * Returns the direct switch
     *
     * @param node the node
     */
    public static Switch create(Node node) {
        return new Switch(node, true);
    }

    private final boolean direct;

    /**
     * Creates a switch
     *
     * @param node   the node
     * @param direct true if direct route
     */
    protected Switch(Node node, boolean direct) {
        super(node);
        int size = node.getEdges().size();
        if (size != 3) {
            throw new IllegalArgumentException(format(
                    "Required node %s with %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    size));
        }
        this.direct = direct;
    }

    @Override
    public int getConnectedIndex(int index) {
        switch (index) {
            case 0:
                return direct ? 1 : 2;
            case 1:
                return direct ? 0 : -1;
            case 2:
                return direct ? -1 : 0;
            default:
                return -1;
        }
    }

    @Override
    public int getNumDirections() {
        return NUM_CONNECTIONS;
    }

    @Override
    public Stream<RouteDirection> getValidDirections() {
        return direct ?
                IntStream.of(0, 1).mapToObj(i -> new RouteDirection(this, i)) :
                IntStream.of(0, 2).mapToObj(i -> new RouteDirection(this, i));
    }

    public boolean isDirect() {
        return direct;
    }

    /**
     * Returns the switch with a direct status
     *
     * @param direct true if direct route
     */
    public Switch setDirect(boolean direct) {
        return new Switch(node, direct);
    }
}
