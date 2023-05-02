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
 * Dissects two edges in boh direction
 */
public class Signal extends AbstractRoute implements SectionTerminal {

    public static final int NUM_CONNECTIONS = 2;

    /**
     * Returns the unlocked signal
     *
     * @param node the node
     */
    public static Signal create(Node node) {
        return new Signal(node, false, false);
    }

    private final boolean locked0;
    private final boolean locked1;

    /**
     * Create the signal
     *
     * @param node    the node
     * @param locked0 true if locks direction 0 --> 1
     * @param locked1 true if locks direction 1 --> 0
     */
    protected Signal(Node node, boolean locked0, boolean locked1) {
        super(node);
        if (node.getEdges().length != 2) {
            throw new IllegalArgumentException(format(
                    "Required node %s with %d edges (%d)",
                    node.getId(),
                    NUM_CONNECTIONS,
                    node.getEdges().length));
        }
        this.locked0 = locked0;
        this.locked1 = locked1;
    }

    @Override
    public int getConnectedIndex(int index) {
        switch (index) {
            case 0:
                return 1;
            case 1:
                return 0;
            default:
                return -1;
        }
    }

    @Override
    public int getNumDirections() {
        return NUM_CONNECTIONS;
    }

    @Override
    public boolean isLocked(int index) {
        switch (index) {
            case 0:
                return locked0;
            case 1:
                return locked1;
            default:
                return true;
        }
    }

    /**
     * Returns true if locks direction 0 --> 1
     */
    public boolean isLocked0() {
        return locked0;
    }

    /**
     * Returns the signal with set locked in direction 0 --> 1
     *
     * @param locked0 true if locked
     */
    public Signal setLocked0(boolean locked0) {
        return new Signal(node, locked0, locked1);
    }

    /**
     * Returns true if locks direction 1 --> 0
     */
    public boolean isLocked1() {
        return locked1;
    }

    /**
     * Returns the signal with set locked in direction 1 --> 0
     *
     * @param locked1 true if locked
     */
    public Signal setLocked1(boolean locked1) {
        return new Signal(node, locked0, locked1);
    }
}
