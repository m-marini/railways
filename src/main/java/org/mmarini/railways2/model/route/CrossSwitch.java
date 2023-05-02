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

import static java.lang.String.format;

/**
 * Switches between two lines (4 edges)
 * Direct routes 0 --> 2 and 1 --> 3
 * Deviated routes 0 --> 3 and 1 --> 2
 */
public class CrossSwitch extends AbstractRoute {
    public static final int NUM_DIRECTIONS = 4;

    /**
     * Returns the direct cross switch
     *
     * @param node the node
     */
    public static CrossSwitch create(Node node) {
        return new CrossSwitch(node, true);
    }

    private final boolean direct;

    /**
     * Create the route
     *
     * @param node   the node
     * @param direct true if direct
     */
    protected CrossSwitch(Node node, boolean direct) {
        super(node);
        if (node.getEdges().length != 4) {
            throw new IllegalArgumentException(format(
                    "Required node %s with %d edges (%d)",
                    node.getId(),
                    NUM_DIRECTIONS,
                    node.getEdges().length));
        }
        this.direct = direct;
    }

    @Override
    public int getConnectedIndex(int index) {
        switch (index) {
            case 0:
                return direct ? 1 : 3;
            case 1:
                return direct ? 0 : 2;
            case 2:
                return direct ? 3 : 1;
            case 3:
                return direct ? 2 : 0;
            default:
                return -1;
        }
    }

    @Override
    public int getNumDirections() {
        return NUM_DIRECTIONS;
    }

    /**
     * Returns true if direct
     */
    public boolean isDirect() {
        return direct;
    }

    /**
     * Returns the switch with a direct status
     *
     * @param direct true if direct route
     */
    public CrossSwitch setDirect(boolean direct) {
        return new CrossSwitch(node, direct);
    }
}
