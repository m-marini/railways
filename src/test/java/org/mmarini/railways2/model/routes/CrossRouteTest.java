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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossRouteTest {

    StationMap station;
    private CrossRoute route;

    /**
     * Station map
     * <pre>
     * Entry(a) --ae-- CrossRoute(e) --be-- Exit(b)
     * Entry(c) --ce--               --de-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ae")
                .addNode("b", new Point2D.Double(200, 0), "be")
                .addNode("c", new Point2D.Double(0, -10), "ce")
                .addNode("d", new Point2D.Double(200, -10), "de")
                .addNode("e", new Point2D.Double(100, 0), "ae", "be", "ce", "de")
                .addEdge(Track.builder("ae"), "a", "e")
                .addEdge(Track.builder("be"), "b", "e")
                .addEdge(Track.builder("ce"), "c", "e")
                .addEdge(Track.builder("de"), "d", "e")
                .build();
        route = CrossRoute.create(station.getNode("e"));
    }

    @Test
    void getCrossingEdges() {
        Node e = station.getNode("e");
        Edge ae = station.getEdge("ae");
        Edge be = station.getEdge("be");
        Edge ce = station.getEdge("ce");
        Edge de = station.getEdge("de");

        assertThat(route.getCrossingEdges(new Direction(ae, e)), containsInAnyOrder(ce, de));
        assertThat(route.getCrossingEdges(new Direction(be, e)), containsInAnyOrder(ce, de));
        assertThat(route.getCrossingEdges(new Direction(ce, e)), containsInAnyOrder(ae, be));
        assertThat(route.getCrossingEdges(new Direction(de, e)), containsInAnyOrder(ae, be));
    }

    @Test
    void getExit() {
        Node e = station.getNode("e");
        Edge ae = station.getEdge("ae");
        Edge be = station.getEdge("be");
        Edge ce = station.getEdge("ce");
        Edge de = station.getEdge("de");

        Direction a_e = new Direction(ae, e);
        Direction b_e = new Direction(be, e);
        Direction c_e = new Direction(ce, e);
        Direction d_e = new Direction(de, e);
        Direction e_a = a_e.opposite();
        Direction e_b = b_e.opposite();
        Direction e_c = c_e.opposite();
        Direction e_d = d_e.opposite();

        Optional<Direction> dirOpt = route.getExit(a_e);
        assertTrue(dirOpt.isPresent());
        assertEquals(e_b, dirOpt.orElseThrow());

        dirOpt = route.getExit(b_e);
        assertTrue(dirOpt.isPresent());
        assertEquals(e_a, dirOpt.orElseThrow());

        dirOpt = route.getExit(c_e);
        assertTrue(dirOpt.isPresent());
        assertEquals(e_d, dirOpt.orElseThrow());

        dirOpt = route.getExit(d_e);
        assertTrue(dirOpt.isPresent());
        assertEquals(e_c, dirOpt.orElseThrow());
    }

    @Test
    void getExits() {
        Node e = station.getNode("e");
        Edge ae = station.getEdge("ae");
        Edge be = station.getEdge("be");
        Edge ce = station.getEdge("ce");
        Edge de = station.getEdge("de");

        Direction a_e = new Direction(ae, e);
        Direction b_e = new Direction(be, e);
        Direction c_e = new Direction(ce, e);
        Direction d_e = new Direction(de, e);
        Direction e_a = a_e.opposite();
        Direction e_b = b_e.opposite();
        Direction e_c = c_e.opposite();
        Direction e_d = d_e.opposite();

        Collection<Direction> exits = route.getValidExits();
        assertThat(exits, containsInAnyOrder(e_a, e_b, e_c, e_d));
    }
}