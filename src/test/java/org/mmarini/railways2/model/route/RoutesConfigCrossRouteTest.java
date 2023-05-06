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
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.routeDirection;
import static org.mmarini.railways.TestFunctions.section;

class RoutesConfigCrossRouteTest {

    static StationMap stationMap;
    static RoutesConfig conf;

    /**
     * Station map
     * <pre>
     * Entry(a) --ae-- CrossRoute(e) --be-- Exit(b)
     * Entry(c) --ce--               --de-- Exit(d)
     * </pre>
     */
    @BeforeAll
    static void createRoutesConfig() {
        stationMap = new StationBuilder("station")
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
        Map<String, Function<Node, Route>> builders = Map.of(
                "a", Entry::new,
                "b", Entry::new,
                "c", Exit::new,
                "d", Exit::new,
                "e", CrossRoute::new
        );
        conf = RoutesConfig.create(stationMap, builders);
    }

    @Test
    void findSection() {
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Route d = conf.getRoute("d");
        Edge ae = stationMap.getEdge("ae");
        Edge be = stationMap.getEdge("be");
        Edge ce = stationMap.getEdge("ce");
        Edge de = stationMap.getEdge("de");

        Section section = conf.findSection(new RouteDirection(a, 0));
        assertThat(section, section(new RouteDirection(a, 0), new RouteDirection(b, 0), ae, be));

        section = conf.findSection(new RouteDirection(b, 0));
        assertThat(section, section(new RouteDirection(a, 0), new RouteDirection(b, 0), ae, be));

        section = conf.findSection(new RouteDirection(c, 0));
        assertThat(section, section(new RouteDirection(c, 0), new RouteDirection(d, 0), ce, de));

        section = conf.findSection(new RouteDirection(d, 0));
        assertThat(section, section(new RouteDirection(c, 0), new RouteDirection(d, 0), ce, de));
    }

    @Test
    void findSectionTerminalDirect() {
        Route a = conf.getRoute("a");
        Route b = conf.getRoute("b");
        Route c = conf.getRoute("c");
        Route d = conf.getRoute("d");
        Edge ae = stationMap.getEdge("ae");
        Edge ce = stationMap.getEdge("ce");

        Optional<RouteDirection> dirOpt = conf.findSectionTerminal(new OrientedLocation(ae, true, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(b, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(ae, false, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(a, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(ce, true, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(d, 0));

        dirOpt = conf.findSectionTerminal(new OrientedLocation(ce, false, 0));
        assertTrue(dirOpt.isPresent());
        assertThat(dirOpt.orElseThrow(), routeDirection(c, 0));
    }

    @Test
    void sections() {
        Edge ae = stationMap.getEdge("ae");
        Optional<Section> sectionOpt = conf.getSection(ae);
        assertTrue(sectionOpt.isPresent());
        Section sectionAB = sectionOpt.orElseThrow();

        assertThat(sectionAB, hasProperty("crossingSections",
                containsInAnyOrder(
                        hasProperty("id", equalTo("ce"))
                )));
    }
}