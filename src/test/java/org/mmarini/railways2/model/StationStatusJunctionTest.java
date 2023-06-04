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
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Section;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.isSectionWith;
import static org.mmarini.railways2.model.MathUtils.RAD180;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;

class StationStatusJunctionTest {

    public static final double LENGTH = 100;
    StationMap stationMap;
    StationStatus status;
    private Node a;
    private Node b;
    private Node c;
    private Track ab;
    private Track bc;
    private Entry aRoute;
    private Exit cRoute;

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab-- Junction(b) --bc-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .build();
        this.a = stationMap.getNode("a");
        this.b = stationMap.getNode("b");
        this.c = stationMap.getNode("c");
        this.ab = stationMap.getEdge("ab");
        this.bc = stationMap.getEdge("bc");
        status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Junction::create, "b")
                .addRoute(Exit::create, "c")
                .build();
        this.aRoute = status.getRoute(a);
        this.cRoute = status.getRoute(c);
    }

    @Test
    void computeCoachLocation() {
        // Given ...

        // When ...
        Optional<Tuple2<Point2D, Double>> loc1 = status.computeCoachLocation(EdgeLocation.create(ab, b, LENGTH / 2));
        Optional<Tuple2<Point2D, Double>> loc2 = status.computeCoachLocation(EdgeLocation.create(ab, b, COACH_LENGTH / 2));
        Optional<Tuple2<Point2D, Double>> loc3 = status.computeCoachLocation(EdgeLocation.create(bc, c, COACH_LENGTH / 2));
        Optional<Tuple2<Point2D, Double>> loc4 = status.computeCoachLocation(EdgeLocation.create(bc, b, COACH_LENGTH / 2));

        // Then ...
        assertThat(loc1, optionalOf(
                Tuple2.of(new Point2D.Double(LENGTH / 2 + COACH_LENGTH / 2, 0), RAD180)
        ));
        assertThat(loc2, optionalOf(
                Tuple2.of(new Point2D.Double(LENGTH, 0), RAD180)
        ));
        assertThat(loc3, org.mmarini.railways.Matchers.emptyOptional());
        assertThat(loc4, optionalOf(
                Tuple2.of(new Point2D.Double(LENGTH, 0), 0d)
        ));
    }

    @Test
    void createSectionByEdge() {
        // Given ...

        // When ...
        Map<? extends Edge, Section> map = status.createSectionByEdge();

        // Then ...
        assertEquals(2, map.size());
        assertThat(map, hasEntry(equalTo(ab),
                hasProperty("id", equalTo("ab"))));
        assertThat(map, hasEntry(equalTo(bc),
                hasProperty("id", equalTo("ab"))));
    }

    @Test
    void createSections() {
        // Given ...

        // When ...
        Collection<Section> sections = status.createSections();

        // Then ...
        assertThat(sections, hasSize(1));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bc)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
    }

    @Test
    void createTrainByEdge1() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        Map<Edge, Train> map = status.createTrainByEdge();

        // Than...
        assertEquals(1, map.size());
        assertThat(map, hasEntry(
                bc, t1
        ));
    }

    @Test
    void createTrainByEdge2() {
        // Given ...
        Train t1 = Train.create("t1", 5, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        Map<Edge, Train> map = status.createTrainByEdge();

        // Than...
        assertEquals(2, map.size());
        assertThat(map, hasEntry(
                bc, t1
        ));
        assertThat(map, hasEntry(
                ab, t1
        ));
    }

    @Test
    void findForwardEdges() {
        // Given ...

        // When ...
        List<Edge> edges50 = status.findForwardEdges(EdgeLocation.create(bc, c, 100), 50).collect(Collectors.toList());
        List<Edge> edges100 = status.findForwardEdges(EdgeLocation.create(ab, b, 100), 100).collect(Collectors.toList());
        List<Edge> edges150 = status.findForwardEdges(EdgeLocation.create(ab, b, 100), 150).collect(Collectors.toList());
        List<Edge> edges200 = status.findForwardEdges(EdgeLocation.create(ab, b, 100), 200).collect(Collectors.toList());
        List<Edge> edges201 = status.findForwardEdges(EdgeLocation.create(ab, b, 100), 201).collect(Collectors.toList());

        // Than...
        assertThat(edges50, contains(bc));
        assertThat(edges100, contains(ab));
        assertThat(edges150, contains(ab, bc));
        assertThat(edges200, contains(ab, bc));
        assertThat(edges201, contains(ab, bc));
    }

    @Test
    void findSection() {
        // Given ...

        // When ...
        Optional<Tuple2<Section, Set<Edge>>> sectionAB = status.findSection(new Direction(ab, b));
        Optional<Tuple2<Section, Set<Edge>>> sectionCB = status.findSection(new Direction(bc, b));

        // Then ...
        assertThat(sectionAB, optionalOf(tupleOf(
                isSectionWith("ab", "b", "bc", "b", "ab", "bc"),
                empty()
        )));
        assertThat(sectionAB, optionalOf(tupleOf(
                isSectionWith("bc", "b", "ab", "b", "ab", "bc"),
                empty()
        )));
    }

    @Test
    void getLocationAt() {
        // Given ..

        // When ...
        Optional<EdgeLocation> ab_10 = status.getLocationAt(EdgeLocation.create(ab, b, LENGTH), 10);
        Optional<EdgeLocation> ab0 = status.getLocationAt(EdgeLocation.create(ab, b, LENGTH), LENGTH);
        Optional<EdgeLocation> ab10 = status.getLocationAt(EdgeLocation.create(ab, b, LENGTH), LENGTH + 10);
        Optional<EdgeLocation> none = status.getLocationAt(EdgeLocation.create(ab, b, LENGTH), 2 * LENGTH + 10);

        // Then ...
        assertThat(ab_10, optionalOf(EdgeLocation.create(ab, b, LENGTH - 10)));
        assertThat(ab0, optionalOf(EdgeLocation.create(ab, b, 0)));
        assertThat(ab10, optionalOf(EdgeLocation.create(bc, c, LENGTH - 10)));
        assertThat(none, org.mmarini.railways.Matchers.emptyOptional());
    }

    @Test
    void getTrain1() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        Optional<Train> trainBC = status.getTrain(bc);
        Optional<Train> trainAB = status.getTrain(ab);

        // Than...
        assertTrue(trainBC.isPresent());
        assertSame(t1, trainBC.orElseThrow());
        assertFalse(trainAB.isPresent());
    }

    @Test
    void getTrain5() {
        // Given ...
        Train t1 = Train.create("t1", 5, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        Optional<Train> trainBC = status.getTrain(bc);
        Optional<Train> trainAB = status.getTrain(ab);

        // Than...
        assertTrue(trainBC.isPresent());
        assertSame(t1, trainBC.orElseThrow());
        assertTrue(trainAB.isPresent());
        assertSame(t1, trainAB.orElseThrow());
    }

    @Test
    void getTrainCoachesEntering() {
        // Given ...
        Train train = Train.create("train", 4, aRoute, cRoute)
                .setState(Train.ENTERING_STATE)
                .setLocation(EdgeLocation.create(ab, b, LENGTH - COACH_LENGTH * 2.5));
        status = status.setTrains(train);

        // When ...
        TrainComposition coaches = status.getTrainCoaches(train);

        // Then
        assertNotNull(coaches);
        assertThat(coaches.getHead(), optionalOf(
                Tuple2.of(new Point2D.Double(2 * COACH_LENGTH, 0), 0d)
        ));
        assertThat(coaches.getCoaches(), contains(
                Tuple2.of(new Point2D.Double(COACH_LENGTH, 0), 0d)
        ));
        assertThat(coaches.getTail(), org.mmarini.railways.Matchers.emptyOptional());
    }

    @Test
    void getTrainCoachesExiting() {
        // Given ...
        Train train = Train.create("train", 4, aRoute, cRoute)
                .exit(cRoute, COACH_LENGTH * 1.5);
        status = status.setTrains(train);

        // When ...
        TrainComposition coaches = status.getTrainCoaches(train);

        // Then
        assertNotNull(coaches);
        assertThat(coaches.getHead(), org.mmarini.railways.Matchers.emptyOptional());
        assertThat(coaches.getCoaches(), contains(
                Tuple2.of(new Point2D.Double(2 * LENGTH - COACH_LENGTH, 0), 0d)
        ));
        assertThat(coaches.getTail(), optionalOf(
                Tuple2.of(new Point2D.Double(2 * LENGTH - 2 * COACH_LENGTH, 0), 0d)
        ));
    }

    @Test
    void getTrainCoachesFull() {
        // Given ...
        Train train = Train.create("train", 4, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(train);

        // When ...
        TrainComposition coaches = status.getTrainCoaches(train);

        // Then
        assertNotNull(coaches);
        assertThat(coaches.getHead(), optionalOf(
                Tuple2.of(new Point2D.Double(2 * LENGTH - COACH_LENGTH / 2, 0), 0d)
        ));
        assertThat(coaches.getCoaches(), contains(
                Tuple2.of(new Point2D.Double(2 * LENGTH - 3 * COACH_LENGTH / 2, 0), 0d),
                Tuple2.of(new Point2D.Double(2 * LENGTH - 5 * COACH_LENGTH / 2, 0), 0d)
        ));
        assertThat(coaches.getTail(), optionalOf(
                Tuple2.of(new Point2D.Double(2 * LENGTH - 7 * COACH_LENGTH / 2, 0), 0d)
        ));
    }

    @Test
    void getTrainEdges1() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        List<Edge> edges = status.getTrainEdges(t1).collect(Collectors.toList());

        // Than...
        assertThat(edges, contains(bc));
    }

    @Test
    void getTrainEdges5() {
        // Given ...
        Train t1 = Train.create("t1", 5, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1);

        // When ...
        List<Edge> edges = status.getTrainEdges(t1).collect(Collectors.toList());

        // Than...
        assertThat(edges, contains(bc, ab));
    }

    @Test
    void isEntryClearEntryNotClear() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));
        status = status.setTrains(t1, t2);

        // When ... Than...
        assertFalse(status.isEntryClear(t1));
    }

    @Test
    void isEntryClearTrainEnqueued() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        status = status.setTrains(t1, t2);

        // When ... Than...
        assertFalse(status.isEntryClear(t1));
    }

    @Test
    void isEntryClearTrue() {
        // Given ...
        Train train = Train.create("t1", 1, aRoute, cRoute);
        status = status.setTrains(train);


        // When ... Than...
        assertTrue(status.isEntryClear(train));
    }

    @Test
    void isNextRouteClear() {
        // Given ... When ... Than...
        assertFalse(status.isNextRouteClear(new Direction(ab, a)));
        assertTrue(status.isNextRouteClear(new Direction(bc, c)));
        assertTrue(status.isNextRouteClear(new Direction(ab, b)));
    }

    @Test
    void isNextRouteClearFalse() {
        // Given ...
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setState(Train.EXITING_STATE)
                .setExitingNode(cRoute);
        status = status.setTrains(t1);

        // When ... Than...
        assertFalse(status.isNextRouteClear(new Direction(bc, c)));
    }
}