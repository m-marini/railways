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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.TestFunctions.routeDirection;

class RoutesConfigSignalTest {

    static StationMap stationMap;
    static RoutesConfig conf;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab-- Signal(b) --bc-- Exit(c)
     * </pre>
     */
    @BeforeAll
    static void createRoutesConfig() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
        Map<String, Function<Node, Route>> builders = Map.of(
                "a", Entry::new,
                "b", Signal::create,
                "c", Exit::new
        );
        conf = RoutesConfig.create(stationMap, builders);
    }

    @Test
    void direction() {
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Route b = conf.getRoute("b");

        Optional<RouteDirection> routeDirectionOpt = conf.getTerminalDirection(new OrientedLocation(ab, true, 0));
        assertTrue(routeDirectionOpt.isPresent());
        assertThat(routeDirectionOpt.orElseThrow(), routeDirection(b, 0));

        Optional<RouteDirection> routeDirectionOpt1 = conf.getTerminalDirection(new OrientedLocation(bc, false, 0));
        assertTrue(routeDirectionOpt1.isPresent());
        assertThat(routeDirectionOpt1.orElseThrow(), routeDirection(b, 1));

    }

    @Test
    void findSection() {
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Section section = conf.findSection(new RouteDirection(a, 0));
        assertNotNull(section);

        assertThat(section.getTerminals(), containsInAnyOrder(
                routeDirection(a, 0),
                routeDirection(b, 0)
        ));

        assertThat(section.getEdges(), containsInAnyOrder(ab));

        section = conf.findSection(new RouteDirection(c, 0));
        assertNotNull(section);

        assertThat(section.getTerminals(), containsInAnyOrder(
                routeDirection(b, 1),
                routeDirection(c, 0)
        ));

        assertThat(section.getEdges(), containsInAnyOrder(bc));
    }

    @Test
    void findSectionTerminalDirect() {
        createRoutesConfig();
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Optional<RouteDirection> dirOpt = conf.findSectionTerminal(new OrientedLocation(ab, true, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(b, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(ab, false, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(a, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(bc, true, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(c, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(bc, false, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(b, 1));
    }

    @Test
    void sessions() {
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Collection<Section> sessions = conf.getSections();

        assertThat(sessions, containsInAnyOrder(
                allOf(
                        hasProperty("terminals", containsInAnyOrder(
                                routeDirection(a, 0),
                                routeDirection(b, 0)
                        )),
                        hasProperty("edges", containsInAnyOrder(ab))
                ),
                allOf(
                        hasProperty("terminals", containsInAnyOrder(
                                routeDirection(b, 1),
                                routeDirection(c, 0)
                        )),
                        hasProperty("edges", containsInAnyOrder(bc))
                )
        ));
    }

    @Test
    void sessionsByEdge() {
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Map<Edge, Section> sectionByEdge = conf.getSectionByEdge();
        assertThat(sectionByEdge, hasEntry(equalTo(ab), hasProperty("id", equalTo("ab"))));
        assertThat(sectionByEdge, hasEntry(equalTo(bc), hasProperty("id", equalTo("bc"))));
        assertEquals(2, sectionByEdge.size());
    }
}