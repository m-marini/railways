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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.TestFunctions.routeDirection;

class RoutesConfigSwitchTest {

    static StationMap stationMap;
    static RoutesConfig conf;

    /**
     * Station map
     * <pre>
     *                           --bd-- Exit(d)
     * Entry(a) --ab-- Switch(b) --bc-- Exit(c)
     * </pre>
     */
    @BeforeAll
    static void createRoutesConfig() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addNode("d", new Point2D.Double(200, 10), "bd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("bd"), "b", "d")
                .build();
    }

    void createSwitch(boolean direct) {
        Map<String, Function<Node, Route>> builders = Map.of(
                "a", Entry::new,
                "b", node -> new Switch(node, direct),
                "c", Exit::new,
                "d", Exit::new
        );
        conf = RoutesConfig.create(stationMap, builders);

        Route route = conf.getRoute("a");
        assertThat(route, isA(Entry.class));

        route = conf.getRoute("b");
        assertThat(route, isA(Switch.class));

        route = conf.getRoute("c");
        assertThat(route, isA(Exit.class));

        route = conf.getRoute("d");
        assertThat(route, isA(Exit.class));
    }

    @Test
    void findSectionDeviated() {
        createSwitch(false);
        Route a = conf.getRoute("a");
        Route d = conf.getRoute("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bd = stationMap.getEdge("bd");

        Section section = conf.findSection(new RouteDirection(a, 0));
        assertNotNull(section);

        Collection<RouteDirection> terminals = section.getTerminals();
        assertThat(terminals, containsInAnyOrder(
                routeDirection(a, 0),
                routeDirection(d, 0)
        ));

        Collection<Edge> edges = section.getEdges();
        assertThat(edges, containsInAnyOrder(ab, bd));
    }

    @Test
    void findSectionDirect() {
        createSwitch(true);
        Route a = conf.getRoute("a");
        Route c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Section section = conf.findSection(new RouteDirection(a, 0));
        assertNotNull(section);

        Collection<RouteDirection> terminals = section.getTerminals();
        assertThat(terminals, containsInAnyOrder(
                routeDirection(a, 0),
                routeDirection(c, 0)
        ));

        Collection<Edge> edges = section.getEdges();
        assertThat(edges, containsInAnyOrder(ab, bc));
    }

    @Test
    void findSectionTerminalDeviated() {
        createSwitch(false);
        Route a = conf.getRoute("a");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Optional<RouteDirection> dirOpt = conf.findSectionTerminal(new OrientedLocation(bc, false, 0));
        assertFalse(dirOpt.isPresent());

        dirOpt = conf.findSectionTerminal(new OrientedLocation(bd, false, 0));
        assertTrue(dirOpt.isPresent());

        RouteDirection dir = dirOpt.orElseThrow();
        assertThat(dir, routeDirection(a, 0));
    }
}