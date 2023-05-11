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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.*;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.*;

class JunctionTest {

    private static StationMap station;

    /*
     * a --ab-- Junction(b) --bc -- c
     */
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

    private Junction route;

    @BeforeEach
    void beforEach() {
        route = Junction.create(station.getNode("b"));
    }

    @Test
    void getConnected() {
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");

        Optional<Direction> dirOpt = route.getExit(new Direction(ab, b));
        assertTrue(dirOpt.isPresent());
        assertEquals(new Direction(bc, c), dirOpt.orElseThrow());

        dirOpt = route.getExit(new Direction(bc, b));
        assertTrue(dirOpt.isPresent());
        assertEquals(new Direction(ab, a), dirOpt.orElseThrow());

        dirOpt = route.getExit(new Direction(bc, c));
        assertFalse(dirOpt.isPresent());
    }

    @Test
    void getCrossingEdges() {
        Node b = station.getNode("b");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");

        assertThat(route.getCrossingEdges(new Direction(ab, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, b)), empty());
    }

    @Test
    void getExits() {
        Node a = station.getNode("a");
        Node c = station.getNode("c");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");

        Collection<Direction> exits = route.getExits();
        assertThat(exits, containsInAnyOrder(
                new Direction(ab, a),
                new Direction(bc, c)
        ));
    }

}