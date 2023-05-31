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
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.*;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;

class StationStatusCrossRouteTest extends WithStationStatusTest {

    private Node e;
    private Track ae;
    private Track be;
    private Track ce;
    private Track de;
    private Entry aRoute;
    private Exit bRoute;
    private Entry cRoute;
    private Exit dRoute;

    /**
     * Station map
     * <pre>
     * Entry(a) --ae-- CrossRoute(e) --be-- Exit(b)
     * Entry(c) --ce--               --de-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ae")
                .addNode("b", new Point2D.Double(200, 0), "be")
                .addNode("c", new Point2D.Double(0, -10), "ce")
                .addNode("d", new Point2D.Double(200, -10), "de")
                .addNode("e", new Point2D.Double(100, 0), "ae", "be", "ce", "de")
                .addTrack("ae", "a", "e")
                .addTrack("be", "b", "e")
                .addTrack("ce", "c", "e")
                .addTrack("de", "d", "e")
                .build();
        this.status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .addRoute(Entry::create, "c")
                .addRoute(Exit::create, "d")
                .addRoute(CrossRoute::create, "e")
                .build();

        Node a = node("a");
        Node b = node("b");
        Node c = node("c");
        Node d = node("d");
        this.e = node("e");
        this.ae = edge("ae");
        this.be = edge("be");
        this.ce = edge("ce");
        this.de = edge("de");
        this.aRoute = route("a");
        this.bRoute = route("b");
        this.cRoute = route("c");
        this.dRoute = route("d");
    }

    @Test
    void createFirstTrainByEntry() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        Train t2 = Train.create("t2", 1, aRoute, bRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        Train t3 = Train.create("t3", 1, cRoute, dRoute);
        status = status.setTrains(t1, t2, t3);

        // When ...
        Map<Entry, Train> map = status.createFirstTrainByEntry();

        // Than ...
        assertThat(map, hasEntry(aRoute, t2));
        assertThat(map, hasEntry(cRoute, t3));
    }

    @Test
    void createSections() {
        // Given ...

        // When ...
        Collection<Section> sections = status.createSections();

        // Then ...
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
        // Given ...

        // When ...
        Optional<Tuple2<Section, Set<Edge>>> sectionAE = status.findSection(direction("ae", "e"));
        Optional<Tuple2<Section, Set<Edge>>> sectionBE = status.findSection(direction("be", "e"));
        Optional<Tuple2<Section, Set<Edge>>> sectionCE = status.findSection(direction("ce", "e"));
        Optional<Tuple2<Section, Set<Edge>>> sectionDE = status.findSection(direction("de", "e"));

        // Then ...
        assertThat(sectionAE, optionalOf(tupleOf(
                isSectionWith("ae", "e", "be", "e", "ae", "be"),
                containsInAnyOrder(isEdge("ce"), isEdge("de")))));

        assertThat(sectionBE, optionalOf(tupleOf(
                isSectionWith("be", "e", "ae", "e", "ae", "be"),
                containsInAnyOrder(isEdge("ce"), isEdge("de")))));

        assertThat(sectionCE, optionalOf(tupleOf(
                isSectionWith("ce", "e", "de", "e", "ce", "de"),
                containsInAnyOrder(isEdge("ae"), isEdge("be")))));

        assertThat(sectionDE, optionalOf(tupleOf(
                isSectionWith("de", "e", "ce", "e", "ce", "de"),
                containsInAnyOrder(isEdge("ae"), isEdge("be")))));
    }

    @Test
    void getExit() {
        // Given ...

        // When ...
        Optional<Direction> exitAE = status.getExit(new Direction(ae, e));
        Optional<Direction> exitBE = status.getExit(new Direction(be, e));
        Optional<Direction> exitCE = status.getExit(new Direction(ce, e));
        Optional<Direction> exitDE = status.getExit(new Direction(de, e));
        Optional<Direction> exitEA = status.getExit(direction("ae", "a"));
        Optional<Direction> exitEB = status.getExit(direction("be", "b"));
        Optional<Direction> exitEC = status.getExit(direction("ce", "c"));
        Optional<Direction> exitED = status.getExit(direction("de", "d"));

        // Then ...
        assertTrue(exitAE.isPresent());
        assertTrue(exitBE.isPresent());
        assertTrue(exitCE.isPresent());
        assertTrue(exitDE.isPresent());
        assertFalse(exitEA.isPresent());
        assertFalse(exitEB.isPresent());
        assertFalse(exitEC.isPresent());
        assertFalse(exitED.isPresent());

        assertThat(exitAE, optionalOf(isDirection("be", "b")));
        assertThat(exitBE, optionalOf(isDirection("ae", "a")));
        assertThat(exitCE, optionalOf(isDirection("de", "d")));
        assertThat(exitDE, optionalOf(isDirection("ce", "c")));
    }

    @Test
    void getFirstTrainFrom() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        Train t2 = Train.create("t2", 1, aRoute, bRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        Train t3 = Train.create("t3", 1, cRoute, dRoute);
        status = status.setTrains(t1, t2, t3);

        // When ...
        Optional<Train> fta = status.getFirstTrainFrom(aRoute);
        Optional<Train> ftb = status.getFirstTrainFrom(cRoute);

        // Than ...
        assertTrue(fta.isPresent());
        assertEquals(t2, fta.orElseThrow());

        assertTrue(ftb.isPresent());
        assertEquals(t3, ftb.orElseThrow());
    }

    @Test
    void isSectionLockedFalse() {
        // Given ...

        // When ... Then ...
        assertFalse(status.isSectionLocked(ae));
        assertFalse(status.isSectionLocked(ce));
    }

    @Test
    void isSectionLockedWithTrain() {
        // Given ...
        Train t = Train.create("t", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(ae, e, 1));
        status = status.setTrains(t);

        // When ... Then ...
        assertTrue(status.isSectionLocked(ae));
        assertTrue(status.isSectionLocked(ce));
    }
}