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

import org.mmarini.railways2.model.geometry.Node;

import java.util.Optional;

import static java.lang.String.format;

/**
 * Dissects for edges in paired directions
 */
public class CrossRoute extends AbstractRoute {

    public static final int NUM_CONNECTIONS = 4;

    /**
     * Create the semaphore
     *
     * @param node the node
     */
    public CrossRoute(Node node) {
        super(node);
        if (node.getEdges().length != NUM_CONNECTIONS) {
            throw new IllegalArgumentException(format(
                    "Required node %s with %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    node.getEdges().length));
        }
    }

    @Override
    public int getConnectedIndex(int index) {
        switch (index) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 3;
            case 3:
                return 2;
            default:
                return -1;
        }
    }

    /**
     * Returns the crossing route direction
     *
     * @param index the index
     */
    public Optional<RouteDirection> getCrossingDirection(int index) {
        return getDirection(getCrossingIndex(index));
    }

    /**
     * Returns the crossing index
     *
     * @param index the index
     */
    int getCrossingIndex(int index) {
        switch (index) {
            case 0:
            case 1:
                return 2;
            case 2:
            case 3:
                return 0;
            default:
                return -1;
        }
    }


    @Override
    public int getNumDirections() {
        return NUM_CONNECTIONS;
    }
}
