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

package org.mmarini.railways2.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.section;

class StationStatusSignalTest {

    static StationMap stationMap;
    static StationStatus status;

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
    }

    @Test
    void createSections() {
        createStatus();
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("bc")),
                hasProperty("edges", containsInAnyOrder(bc)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("bc")),
                hasProperty("crossingSections", empty()))));
    }

    void createStatus(Direction... locks) {
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(nodes -> {
                    Signal signal = Signal.create(nodes);
                    for (Direction lock : locks) {
                        signal = signal.lock(lock);
                    }
                    return signal;
                }, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void findSection() {
        createStatus();
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(new Direction(ab, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ab, b), new Direction(ab, a), ab));
        assertThat(section.orElseThrow()._2, empty());

        section = status.findSection(new Direction(ab, a));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ab, a), new Direction(ab, b), ab));
        assertThat(section.orElseThrow()._2, empty());

        section = status.findSection(new Direction(bc, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(bc, b), new Direction(bc, c), bc));
        assertThat(section.orElseThrow()._2, empty());

        section = status.findSection(new Direction(bc, c));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(bc, c), new Direction(bc, b), bc));
        assertThat(section.orElseThrow()._2, empty());
    }

    /*
    @Test
    void isSectionClearWithLock() {
        // Give ...
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        createStatus(new Direction(ab, b), new Direction(bc, b));

        // When ... Than ...
        assertFalse(status.isSectionClear(ab));
        assertFalse(status.isSectionClear(bc));
    }
*/

    @Test
    void isNextRouteClear() {
        // Give ...
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        createStatus();
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ... Than ...
        assertFalse(status.isNextRouteClear(new Direction(ab, b)));
        assertTrue(status.isNextRouteClear(new Direction(bc, b)));
    }

    @Test
    void isNextRouteClearLocked() {
        // Give ...
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        createStatus(new Direction(ab, b));

        // When ... Than ...
        assertFalse(status.isNextRouteClear(new Direction(ab, b)));
        assertTrue(status.isNextRouteClear(new Direction(bc, b)));
    }

    @Test
    void isSectionClearWithTrain() {
        // Give ...
        createStatus();
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        createStatus();
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ... Than ...
        assertTrue(status.isSectionClear(ab));
        assertFalse(status.isSectionClear(bc));
    }

    /*
    @Test
    void findSectionTerminalDirect() {
        createRoutesConfig();
        SingleNodeRoute a = conf.getRoute("a");
        SingleNodeRoute b = conf.getRoute("b");
        SingleNodeRoute c = conf.getRoute("c");
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
        SingleNodeRoute a = conf.getRoute("a");
        SingleNodeRoute b = conf.getRoute("b");
        SingleNodeRoute c = conf.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Collection<Section> sessions = conf.getSections();

        assertThat(sessions, containsInAnyOrder(
                section(new RouteDirection(a, 0), new RouteDirection(b, 0), ab),
                section(new RouteDirection(c, 0), new RouteDirection(b, 1), bc)
        ));
        assertThat(conf.getSection(ab).orElseThrow().getCrossingSections(), empty());
    }

    @Test
    void sessionsByEdge() {
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Map<Edge, Section> sectionByEdge = conf.getSectionByEdge();
        assertThat(sectionByEdge, hasEntry(equalTo(ab), hasProperty("id", equalTo("ab"))));
        assertThat(sectionByEdge, hasEntry(equalTo(bc), hasProperty("id", equalTo("bc"))));
        assertEquals(2, sectionByEdge.size());
    }

     */
}