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

package org.mmarini.railways2.model.geometry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EdgeSegmentTest {
    public static final double LENGTH = 80;
    private Node a;
    private Node b;
    private Node c;
    private Track ab;
    private Track bc;

    @Test
    void create() {
        // Given ...

        // When ...
        EdgeSegment segAB = EdgeSegment.create(EdgeLocation.create(ab, b, 30), 10);
        EdgeSegment segBA = EdgeSegment.create(EdgeLocation.create(ab, a, 30), 10);

        // Then ...
        assertEquals(new EdgeSegment(ab, LENGTH - 30, 20), segAB);
        assertEquals(new EdgeSegment(ab, 20, LENGTH - 30), segBA);
    }

    @BeforeEach
    void setUp() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
        this.a = stationMap.getNode("a");
        this.b = stationMap.getNode("b");
        this.c = stationMap.getNode("c");
        this.ab = stationMap.getEdge("ab");
        this.bc = stationMap.getEdge("bc");
    }
}