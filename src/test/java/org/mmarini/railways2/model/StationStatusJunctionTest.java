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
import org.mmarini.railways2.model.routes.*;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.TestFunctions.section;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;

class StationStatusJunctionTest {

    StationMap stationMap;
    StationStatus status;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab-- Junction(b) --bc-- Exit(c)
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
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Junction::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void create() {
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");

        Route ar = status.getRoute(a);

        assertThat(ar, isA(Entry.class));
        assertEquals("a", ar.getId());

        Route br = status.getRoute(b);
        assertThat(br, isA(Junction.class));
        assertEquals("b", br.getId());

        Route cr = status.getRoute(c);
        assertThat(cr, isA(Exit.class));
        assertEquals("c", cr.getId());
    }

    @Test
    void createSectionByEdge() {
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Map<? extends Edge, Section> map = status.createSectionByEdge();

        assertEquals(2, map.size());
        assertThat(map, hasEntry(equalTo(ab),
                hasProperty("id", equalTo("ab"))));
        assertThat(map, hasEntry(equalTo(bc),
                hasProperty("id", equalTo("ab"))));
    }

    @Test
    void createSections() {
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Collection<Section> sections = status.createSections();

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
        // Give ...
        Node c = stationMap.getNode("c");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
        // Give ...
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
        // Give ...
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

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
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        Optional<Tuple2<Section, Set<Edge>>> sectionOpt = status.findSection(new Direction(ab, b));
        assertTrue(sectionOpt.isPresent());
        assertThat(sectionOpt.orElseThrow()._1, section(new Direction(ab, b), new Direction(bc, b), ab, bc));
        assertThat(sectionOpt.orElseThrow()._2, empty());

        sectionOpt = status.findSection(new Direction(bc, b));
        assertTrue(sectionOpt.isPresent());
        assertThat(sectionOpt.orElseThrow()._1, section(new Direction(bc, b), new Direction(ab, b), ab, bc));
        assertThat(sectionOpt.orElseThrow()._2, empty());
    }

    @Test
    void getTrain1() {
        // Give ...
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
        // Give ...
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
    void getTrainEdges1() {
        // Give ...
        Node c = stationMap.getNode("c");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
        // Give ...
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
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
        // Give ...
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));
        status = status.setTrains(t1, t2);

        // When ... Than...
        assertFalse(status.isEntryClear(t1));
    }

    @Test
    void isEntryClearTrainEnqueued() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train t1 = Train.create("t1", 1, aRoute, cRoute);
        Train t2 = Train.create("t2", 1, aRoute, cRoute).setArrivalTime(ENTRY_TIMEOUT - 1);
        status = status.setTrains(t1, t2);

        // When ... Than...
        assertFalse(status.isEntryClear(t1));
    }

    @Test
    void isEntryClearTrue() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train train = Train.create("t1", 1, aRoute, cRoute);
        status = status.setTrains(train);


        // When ... Than...
        assertTrue(status.isEntryClear(train));
    }

    @Test
    void isNextRouteClear() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        // When ... Than...
        assertFalse(status.isNextRouteClear(new Direction(ab, a)));
        assertTrue(status.isNextRouteClear(new Direction(bc, c)));
        assertTrue(status.isNextRouteClear(new Direction(ab, b)));
    }

    @Test
    void isNextRouteClearFalse() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node c = stationMap.getNode("c");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute(a);
        Exit cRoute = status.getRoute(c);
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setState(Train.EXITING_STATE)
                .setExitingNode(cRoute);
        status = status.setTrains(t1);

        // When ... Than...
        assertFalse(status.isNextRouteClear(new Direction(bc, c)));
    }
}