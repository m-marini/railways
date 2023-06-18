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

import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.isSectionWith;

class StationStatusSignalTest extends WithStationStatusTest {

    @Test
    void createSections() {
        createStatus();

        // When ...
        Collection<Section> sections = status.createSections();

        // Then ...
        Edge ab = edge("ab");
        Edge bc = edge("bc");
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

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab-- Signals(b) --bc-- Exit(c)
     * </pre>
     */
    void createStatus(String... parms) {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .build();
        Direction[] locks = IntStream.range(0, parms.length / 2)
                .mapToObj(i -> new Direction(
                        stationMap.getEdge(parms[i * 2]),
                        stationMap.getNode(parms[i * 2 + 1])))
                .toArray(Direction[]::new);
        status = new StationStatus.Builder(stationMap, 1, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal.createLocks(locks), "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void findSection() {
        createStatus();

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(direction("ab", "b"));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "ab", "a", "ab"),
                empty()
        )));

        section = status.findSection(direction("ab", "a"));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "ab", "b", "ab"),
                empty()
        )));

        section = status.findSection(direction("bc", "b"));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bc", "b", "bc", "c", "bc"),
                empty()
        )));

        section = status.findSection(direction("bc", "c"));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bc", "c", "bc", "b", "bc"),
                empty()
        )));
    }

    @Test
    void isNextRouteClear() {
        // Give ...
        createStatus();
        status = withTrain()
                .addTrain(3, "a", "c", "bc", "c", 0)
                .build();

        // When ... Than ...
        assertFalse(status.isNextRouteClear(direction("ab", "b")));
        assertTrue(status.isNextRouteClear(direction("bc", "b")));
    }

    @Test
    void isNextRouteClearLocked() {
        // Give ...
        createStatus("ab", "b");

        // When ... Than ...
        assertFalse(status.isNextRouteClear(direction("ab", "b")));
        assertTrue(status.isNextRouteClear(direction("bc", "b")));
    }

    @Test
    void isSectionClearWithTrain() {
        // Give ...
        createStatus();
        status = withTrain()
                .addTrain(3, "a", "c", "bc", "c", 0)
                .build();

        // When ... Than ...
        assertTrue(status.isSectionClear(edge("ab")));
        assertFalse(status.isSectionClear(edge("bc")));
    }

    @Test
    void isSectionLocked1() {
        // Given ...
        createStatus("ab", "b");

        // When ... Then ...
        assertFalse(status.isSectionLocked(edge("ab")));
        assertTrue(status.isSectionLocked(edge("bc")));
    }

    @Test
    void isSectionLocked2() {
        // Given ...
        createStatus("bc", "b");

        // When ... Then ...
        assertTrue(status.isSectionLocked(edge("ab")));
        assertFalse(status.isSectionLocked(edge("bc")));
    }

    @Test
    void isSectionLockedFalse() {
        // Given ...
        createStatus();

        // When ... Then ...
        assertFalse(status.isSectionLocked(edge("ab")));
        assertFalse(status.isSectionLocked(edge("bc")));
    }

    @Test
    void isSectionLockedTrue() {
        // Given ...
        createStatus("ab", "b",
                "bc", "b");

        // When ... Then ...
        assertTrue(status.isSectionLocked(edge("ab")));
        assertTrue(status.isSectionLocked(edge("bc")));
    }

    @Test
    void isSectionLockedWithTrain() {
        // Given ...
        createStatus();
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", 10)
                .build();

        // When ... Then ...
        assertTrue(status.isSectionLocked(edge("ab")));
        assertFalse(status.isSectionLocked(edge("bc")));
    }

    @Test
    void lockSection() {
        // Given ...
        createStatus();

        // When ...
        StationStatus status1 = status.lockSection("ab");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertFalse(signal.isLocked(direction("ab", "b")));
        assertTrue(signal.isLocked(direction("bc", "b")));
    }

    @Test
    void lockSignalAB() {
        // Given ...
        createStatus();

        // When ...
        StationStatus status1 = status.lockSignal("b", "ab");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertTrue(signal.isLocked(direction("ab", "b")));
        assertFalse(signal.isLocked(direction("bc", "b")));
    }

    @Test
    void lockSignalBC() {
        // Given ...
        createStatus();

        // When ...
        StationStatus status1 = status.lockSignal("b", "bc");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertFalse(signal.isLocked(direction("ab", "b")));
        assertTrue(signal.isLocked(direction("bc", "b")));
    }

    @Test
    void unlockSection() {
        // Given ...
        createStatus("ab", "b", "bc", "b");

        // When ...
        StationStatus status1 = status.unlockSection("ab");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertTrue(signal.isLocked(direction("ab", "b")));
        assertFalse(signal.isLocked(direction("bc", "b")));
    }

    @Test
    void unlockSignalAB() {
        // Given ...
        createStatus("ab", "b", "bc", "b");

        // When ...
        StationStatus status1 = status.unlockSignal("b", "ab");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertFalse(signal.isLocked(direction("ab", "b")));
        assertTrue(signal.isLocked(direction("bc", "b")));
    }

    @Test
    void unlockSignalBC() {
        // Given ...
        createStatus("ab", "b", "bc", "b");

        // When ...
        StationStatus status1 = status.unlockSignal("b", "bc");

        // Then ...
        Signal signal = status1.getRoute("b");
        assertTrue(signal.isLocked(direction("ab", "b")));
        assertFalse(signal.isLocked(direction("bc", "b")));
    }
}