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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.geometry.Track;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalTest {

    private static StationMap station;

    @BeforeAll
    static void beforeAll() {
        station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
    }

    private Signal route;

    @BeforeEach
    void beforEach() {
        route = Signal.create(station.getNode("b"));
    }

    @Test
    void connection() {
        assertEquals(1, route.getConnectedIndex(0));
        assertEquals(0, route.getConnectedIndex(1));
        assertEquals(-1, route.getConnectedIndex(-1));
    }

    @Test
    void getNumDirections() {
        assertEquals(2, route.getNumDirections());
    }

    @Test
    void indexOf() {
        assertEquals(0, route.indexOf(station.getEdge("ab")));
        assertEquals(1, route.indexOf(station.getEdge("bc")));
    }
}