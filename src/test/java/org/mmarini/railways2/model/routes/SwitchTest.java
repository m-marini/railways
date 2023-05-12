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

class SwitchTest {

    StationMap station;
    Switch route;

    /*
      a -- Switch(b) -- c
                     -- d
     */
    @BeforeEach
    void beforeEach() {
        station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addNode("d", new Point2D.Double(200, 100), "bd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("bd"), "b", "d")
                .build();
    }

    void createSwitch(boolean through) {
        route = Switch.create(through).apply(new Node[]{station.getNode("b")});
    }

    @Test
    void divergingDiverging() {
        createSwitch(false);
        assertSame(route, route.diverging());
    }

    @Test
    void divergingThrough() {
        createSwitch(true);
        assertTrue(route.through().isThrough());
    }

    @Test
    void getCrossingEdgesDiverging() {
        createSwitch(false);
        Node b = station.getNode("b");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge bd = station.getEdge("bd");

        assertThat(route.getCrossingEdges(new Direction(ab, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(bd, b)), empty());
    }

    @Test
    void getCrossingEdgesThrough() {
        createSwitch(true);
        Node b = station.getNode("b");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge bd = station.getEdge("bd");

        assertThat(route.getCrossingEdges(new Direction(ab, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(bd, b)), empty());
    }

    @Test
    void getExitDiverging() {
        createSwitch(false);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node d = station.getNode("d");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge bd = station.getEdge("bd");

        Direction a_b = new Direction(ab, b);
        Direction b_a = new Direction(ab, a);
        Direction c_b = new Direction(bc, b);
        Direction b_d = new Direction(bd, d);
        Direction d_b = new Direction(bd, b);

        assertFalse(route.isThrough());

        Optional<Direction> dirOpt = route.getExit(a_b);
        assertTrue(dirOpt.isPresent());
        assertEquals(b_d, dirOpt.orElseThrow());

        dirOpt = route.getExit(d_b);
        assertTrue(dirOpt.isPresent());
        assertEquals(b_a, dirOpt.orElseThrow());

        dirOpt = route.getExit(c_b);
        assertFalse(dirOpt.isPresent());
    }

    @Test
    void getExitThrow() {
        createSwitch(true);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge bd = station.getEdge("bd");

        Direction a_b = new Direction(ab, b);
        Direction b_a = new Direction(ab, a);
        Direction c_b = new Direction(bc, b);
        Direction b_c = new Direction(bc, c);
        Direction d_b = new Direction(bd, b);

        assertTrue(route.isThrough());

        Optional<Direction> dirOpt = route.getExit(a_b);
        assertTrue(dirOpt.isPresent());
        assertEquals(b_c, dirOpt.orElseThrow());

        dirOpt = route.getExit(c_b);
        assertTrue(dirOpt.isPresent());
        assertEquals(b_a, dirOpt.orElseThrow());

        dirOpt = route.getExit(d_b);
        assertFalse(dirOpt.isPresent());
    }

    @Test
    void getExitsDiverging() {
        createSwitch(false);
        Node a = station.getNode("a");
        Node d = station.getNode("d");
        Edge ab = station.getEdge("ab");
        Edge bd = station.getEdge("bd");

        Collection<Direction> exits = route.getExits();
        assertThat(exits, containsInAnyOrder(
                new Direction(ab, a),
                new Direction(bd, d)
        ));
    }

    @Test
    void getExitsThrough() {
        createSwitch(true);
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
    void throughDiverging() {
        createSwitch(true);
        assertFalse(route.diverging().isThrough());
    }

    @Test
    void throughThrough() {
        createSwitch(true);
        assertSame(route, route.through());
    }
}