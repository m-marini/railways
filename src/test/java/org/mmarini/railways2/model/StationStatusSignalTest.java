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
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.isSectionWith;

class StationStatusSignalTest {

    StationMap stationMap;
    StationStatus status;
    private Node a;
    private Node b;
    private Node c;
    private Track ab;
    private Track bc;
    private Entry aRoute;
    private Signal bRoute;
    private Exit cRoute;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab-- Signal(b) --bc-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
        this.a = stationMap.getNode("a");
        this.b = stationMap.getNode("b");
        this.c = stationMap.getNode("c");
        this.ab = stationMap.getEdge("ab");
        this.bc = stationMap.getEdge("bc");
    }

    @Test
    void createSections() {
        createStatus();

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
        status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Signal.createLocks(locks), "b")
                .addRoute(Exit::create, "c")
                .build();
        this.aRoute = status.getRoute(a);
        this.bRoute = status.getRoute(b);
        this.cRoute = status.getRoute(c);
    }

    @Test
    void findSection() {
        createStatus();

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(new Direction(ab, b));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "ab", "a", "ab"),
                empty()
        )));

        section = status.findSection(new Direction(ab, a));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "ab", "b", "ab"),
                empty()
        )));

        section = status.findSection(new Direction(bc, b));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bc", "b", "bc", "c", "bc"),
                empty()
        )));

        section = status.findSection(new Direction(bc, c));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bc", "c", "bc", "b", "bc"),
                empty()
        )));
    }

    @Test
    void isNextRouteClear() {
        // Give ...
        createStatus();
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
        createStatus(new Direction(ab, b));

        // When ... Than ...
        assertFalse(status.isNextRouteClear(new Direction(ab, b)));
        assertTrue(status.isNextRouteClear(new Direction(bc, b)));
    }

    @Test
    void isSectionClearWithTrain() {
        // Give ...
        createStatus();
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ... Than ...
        assertTrue(status.isSectionClear(ab));
        assertFalse(status.isSectionClear(bc));
    }

    @Test
    void isSectionLocked1() {
        // Given ...
        createStatus(new Direction(ab, b));

        // When ... Then ...
        assertFalse(status.isSectionLocked(ab));
        assertTrue(status.isSectionLocked(bc));
    }

    @Test
    void isSectionLocked2() {
        // Given ...
        createStatus(new Direction(bc, b));

        // When ... Then ...
        assertTrue(status.isSectionLocked(ab));
        assertFalse(status.isSectionLocked(bc));
    }

    @Test
    void isSectionLockedFalse() {
        // Given ...
        createStatus();

        // When ... Then ...
        assertFalse(status.isSectionLocked(ab));
        assertFalse(status.isSectionLocked(bc));
    }

    @Test
    void isSectionLockedTrue() {
        // Given ...
        createStatus(new Direction(ab, b), new Direction(bc, b));

        // When ... Then ...
        assertTrue(status.isSectionLocked(ab));
        assertTrue(status.isSectionLocked(bc));
    }

    @Test
    void isSectionLockedWithTrain() {
        // Given ...
        createStatus();
        Train t = Train.create("t", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 10));
        status = status.setTrains(t);

        // When ... Then ...
        assertTrue(status.isSectionLocked(ab));
        assertFalse(status.isSectionLocked(bc));
    }
}