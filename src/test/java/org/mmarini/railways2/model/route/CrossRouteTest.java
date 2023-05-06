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
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.geometry.Track;

import java.awt.geom.Point2D;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mmarini.railways.TestFunctions.routeDirection;

class CrossRouteTest {

    static CrossRoute route;
    private static StationMap station;

    /*
     * ac -- cd
     *
     * bc -- ce
     *
     * ac \ / cd
     *     x
     * bc / \ ce

     */
    @BeforeAll
    static void beforeAll() {
        station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ac")
                .addNode("b", new Point2D.Double(0, 100), "bc")
                .addNode("c", new Point2D.Double(100, 0), "ac", "cd", "bc", "ce")
                .addNode("d", new Point2D.Double(200, 0), "cd")
                .addNode("e", new Point2D.Double(200, 100), "ce")
                .addEdge(Track.builder("ac"), "a", "c")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .addEdge(Track.builder("ce"), "c", "e")
                .build();

        route = new CrossRoute(station.getNode("c"));
    }

    @Test
    void connectedDirection() {
        RouteDirection dir1 = route.getConnectedDirection(0).orElseThrow();
        assertThat(dir1, routeDirection(route, 1));
    }

    @Test
    void connectiedIndex() {
        assertEquals(-1, route.getConnectedIndex(-1));
        assertEquals(1, route.getConnectedIndex(0));
        assertEquals(0, route.getConnectedIndex(1));
        assertEquals(3, route.getConnectedIndex(2));
        assertEquals(2, route.getConnectedIndex(3));
    }


    @Test
    void direction() {
        assertThat(route.getDirection(0).orElseThrow(), routeDirection(route, 0));
        assertFalse(route.getDirection(-1).isPresent());
    }

    @Test
    void edge() {
        Edge ac = station.getEdge("ac");
        assertEquals(ac, route.getEdge(0).orElseThrow());
        assertFalse(route.getEdge(-1).isPresent());
    }

    @Test
    void indexOf() {
        assertEquals(0, route.indexOf(station.getEdge("ac")));
        assertEquals(1, route.indexOf(station.getEdge("cd")));
        assertEquals(2, route.indexOf(station.getEdge("bc")));
        assertEquals(3, route.indexOf(station.getEdge("ce")));
        assertEquals(-1, route.indexOf(null));
    }
}