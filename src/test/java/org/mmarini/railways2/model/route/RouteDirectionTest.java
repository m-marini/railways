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
import org.mmarini.railways2.model.geometry.*;

import java.awt.geom.Point2D;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

class RouteDirectionTest {
    static StationMap stationMap;
    static RoutesConfig routeConf;

    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
        routeConf = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Junction::new,
                "c", Exit::new
        ));
    }

    @Test
    void getEdgePoint() {
        Route b = routeConf.getRoute("b");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        RouteDirection dir0 = new RouteDirection(b, 0);

        OrientedLocation ep0 = dir0.getLocation().orElseThrow();
        assertThat(ep0, hasProperty("edge", equalTo(ab)));
        assertThat(ep0, hasProperty("direct", equalTo(false)));

        RouteDirection dir1 = new RouteDirection(b, 1);

        OrientedLocation ep1 = dir1.getLocation().orElseThrow();
        assertThat(ep1, hasProperty("edge", equalTo(bc)));
        assertThat(ep1, hasProperty("direct", equalTo(true)));
    }

    @Test
    void getTerminal() {
        Route b = routeConf.getRoute("b");
        Node aNode = stationMap.getNode("a");
        Node cNode = stationMap.getNode("c");

        RouteDirection dir0 = new RouteDirection(b, 0);
        assertThat(dir0.getTerminal().orElseThrow(), equalTo(aNode));

        RouteDirection dir1 = new RouteDirection(b, 1);
        assertThat(dir1.getTerminal().orElseThrow(), equalTo(cNode));
    }
}