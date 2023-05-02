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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.geometry.Track;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitchTest {

    private static StationMap station;

    @BeforeAll
    static void beforeAll() {
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

    private Switch route;

    @Test
    void connectionDeviated() {
        createSwitch(false);
        assertEquals(2, route.getConnectedIndex(0));
        assertEquals(-1, route.getConnectedIndex(1));
        assertEquals(0, route.getConnectedIndex(2));
    }

    @Test
    void connectionDirect() {
        createSwitch(true);
        assertEquals(-1, route.getConnectedIndex(-1));
        assertEquals(1, route.getConnectedIndex(0));
        assertEquals(0, route.getConnectedIndex(1));
        assertEquals(-1, route.getConnectedIndex(2));
    }

    void createSwitch(boolean direct) {
        route = new Switch(station.getNode("b"), direct);
    }

    @Test
    void deviatedValidDirections() {
        createSwitch(false);
        List<RouteDirection> dirs = route.getValidDirections().collect(Collectors.toList());
        assertThat(dirs, hasSize(2));
        assertThat(dirs, hasItem(new RouteDirection(route, 0)));
        assertThat(dirs, hasItem(new RouteDirection(route, 2)));
    }

    @Test
    void directValidDirections() {
        createSwitch(true);
        List<RouteDirection> dirs = route.getValidDirections().collect(Collectors.toList());
        assertThat(dirs, hasSize(2));
        assertThat(dirs, hasItem(new RouteDirection(route, 0)));
        assertThat(dirs, hasItem(new RouteDirection(route, 1)));
    }

    @Test
    void directions() {
        createSwitch(true);
        List<RouteDirection> dirs = route.getDirections().collect(Collectors.toList());
        assertThat(dirs, hasSize(3));
        assertThat(dirs, hasItem(new RouteDirection(route, 0)));
        assertThat(dirs, hasItem(new RouteDirection(route, 1)));
        assertThat(dirs, hasItem(new RouteDirection(route, 2)));
    }

    @Test
    void getNumDirections() {
        createSwitch(true);
        assertEquals(3, route.getNumDirections());
    }

    @Test
    void indexOf() {
        createSwitch(true);
        assertEquals(0, route.indexOf(station.getEdge("ab")));
        assertEquals(1, route.indexOf(station.getEdge("bc")));
        assertEquals(2, route.indexOf(station.getEdge("bd")));
    }
}