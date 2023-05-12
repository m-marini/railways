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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.CrossRoute;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.TestFunctions.section;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;

class StationStatusCrossRouteTest {

    StationMap stationMap;
    StationStatus status;

    /**
     * Station map
     * <pre>
     * Entry(a) --ae-- CrossRoute(e) --be-- Exit(b)
     * Entry(c) --ce--               --de-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
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
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Entry::create, "b")
                .addRoute(Exit::create, "c")
                .addRoute(Exit::create, "d")
                .addRoute(CrossRoute::create, "e")
                .build();
    }

    @Test
    void createFirstTrainByEntry() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Entry bRoute = status.getRoute("b");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        Train t3 = Train.create("t3", 1, bRoute, cRoute);
        status = status.setTrains(t1, t2, t3);

        // When ...
        Map<Entry, Train> map = status.createFirstTrainByEntry();

        // Than ...
        assertThat(map, hasEntry(aRoute, t2));
        assertThat(map, hasEntry(bRoute, t3));
    }

    @Test
    void createSections() {
        Edge ae = stationMap.getEdge("ae");
        Edge be = stationMap.getEdge("be");
        Edge ce = stationMap.getEdge("ce");
        Edge de = stationMap.getEdge("de");
        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ae")),
                hasProperty("edges", containsInAnyOrder(ae, be)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ae")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("ce"))
                )))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ce")),
                hasProperty("edges", containsInAnyOrder(ce, de)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ce")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("ae"))
                )))));
    }

    @Test
    void findSection() {
        Node e = stationMap.getNode("e");
        Edge ae = stationMap.getEdge("ae");
        Edge be = stationMap.getEdge("be");
        Edge ce = stationMap.getEdge("ce");
        Edge de = stationMap.getEdge("de");

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(new Direction(ae, e));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ae, e), new Direction(be, e), ae, be));
        assertThat(section.orElseThrow()._2, containsInAnyOrder(ce, de));

        section = status.findSection(new Direction(be, e));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(be, e), new Direction(ae, e), ae, be));
        assertThat(section.orElseThrow()._2, containsInAnyOrder(ce, de));

        section = status.findSection(new Direction(ce, e));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ce, e), new Direction(de, e), ce, de));
        assertThat(section.orElseThrow()._2, containsInAnyOrder(ae, be));

        section = status.findSection(new Direction(de, e));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(de, e), new Direction(ce, e), ce, de));
        assertThat(section.orElseThrow()._2, containsInAnyOrder(ae, be));
    }

    @Test
    void getExit() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Node e = stationMap.getNode("e");
        Edge ae = stationMap.getEdge("ae");
        Edge be = stationMap.getEdge("be");
        Edge ce = stationMap.getEdge("ce");
        Edge de = stationMap.getEdge("de");

        // When ...
        Optional<Direction> exitAE = status.getExit(new Direction(ae, e));
        Optional<Direction> exitBE = status.getExit(new Direction(be, e));
        Optional<Direction> exitCE = status.getExit(new Direction(ce, e));
        Optional<Direction> exitDE = status.getExit(new Direction(de, e));
        Optional<Direction> exitEA = status.getExit(new Direction(ae, a));
        Optional<Direction> exitEB = status.getExit(new Direction(be, b));
        Optional<Direction> exitEC = status.getExit(new Direction(ce, c));
        Optional<Direction> exitED = status.getExit(new Direction(de, d));

        // Then ...
        assertTrue(exitAE.isPresent());
        assertTrue(exitBE.isPresent());
        assertTrue(exitCE.isPresent());
        assertTrue(exitDE.isPresent());
        assertFalse(exitEA.isPresent());
        assertFalse(exitEB.isPresent());
        assertFalse(exitEC.isPresent());
        assertFalse(exitED.isPresent());

        assertEquals(new Direction(be, b), exitAE.orElseThrow());
        assertEquals(new Direction(ae, a), exitBE.orElseThrow());
        assertEquals(new Direction(de, d), exitCE.orElseThrow());
        assertEquals(new Direction(ce, c), exitDE.orElseThrow());
    }

    @Test
    void getFirstTrainFrom() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Entry bRoute = status.getRoute("b");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        Train t3 = Train.create("t3", 1, bRoute, cRoute);
        status = status.setTrains(t1, t2, t3);

        // When ...
        Optional<Train> fta = status.getFirstTrainFrom(aRoute);
        Optional<Train> ftb = status.getFirstTrainFrom(bRoute);

        // Than ...
        assertTrue(fta.isPresent());
        assertEquals(t2, fta.orElseThrow());

        assertTrue(ftb.isPresent());
        assertEquals(t3, ftb.orElseThrow());
    }
}