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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class SignalTest {

    private static StationMap station;

    /*
     * a --ab-- Signal(b) --bc-- c
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

    private Signal route;

    @BeforeEach
    void beforeEach() {
        route = Signal.create(station.getNode("b"));
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
    void getExit() {
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

    @Test
    void lock() {
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Direction a_b = new Direction(ab, b);
        Direction c_b = new Direction(bc, b);
        Direction b_c = new Direction(bc, c);

        assertFalse(route.isLocked(a_b));
        assertFalse(route.isLocked(c_b));

        Signal route1 = route.lock(a_b);
        assertTrue(route1.isLocked(a_b));
        assertFalse(route1.isLocked(c_b));

        Signal route2 = route.lock(c_b);
        assertFalse(route2.isLocked(a_b));
        assertTrue(route2.isLocked(c_b));

        Signal route3 = route1.lock(c_b);
        assertTrue(route3.isLocked(a_b));
        assertTrue(route3.isLocked(c_b));

        assertSame(route2, route2.lock(c_b));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> route.lock(b_c));
        assertThat(ex, hasProperty("message",
                matchesPattern("Invalid direction .* for route b")));
    }

    @Test
    void unlock() {
        Node b = station.getNode("b");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Direction a_b = new Direction(ab, b);
        Direction c_b = new Direction(bc, b);

        Signal routeLocked = route.lock(a_b).lock(c_b);
        assertTrue(routeLocked.isLocked(a_b));
        assertTrue(routeLocked.isLocked(c_b));

        Signal route1 = routeLocked.unlock(a_b);
        assertFalse(route1.isLocked(a_b));
        assertTrue(route1.isLocked(c_b));

        Signal route2 = routeLocked.unlock(c_b);
        assertTrue(route2.isLocked(a_b));
        assertFalse(route2.isLocked(c_b));

        Signal route3 = route1.unlock(c_b);
        assertFalse(route3.isLocked(a_b));
        assertFalse(route3.isLocked(c_b));

        assertSame(route, route.unlock(a_b));
    }
}