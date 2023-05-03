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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.route.*;
import org.mmarini.railways2.model.trains.Train;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;
import static org.mmarini.railways2.model.StationStatus.create;

class StationStatusTest {

    private static StationMap stationMap;
    private static RoutesConfig routes;

    /**
     * Entry(a) -- ab -- Signal(b) -- bc -- Exit(c)
     */
    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .build();
        routes = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Signal::create,
                "c", Exit::new
        ));
    }

    @Test
    void findBackwardEdges() {
        StationStatus status = create(stationMap, routes, List.of());

        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        OrientedLocation start = new OrientedLocation(bc, true, 51);

        List<Edge> edges = status.findBackwardEdges(start, 150).collect(Collectors.toList());
        assertThat(edges, contains(bc, ab));

        edges = status.findBackwardEdges(start, 52).collect(Collectors.toList());
        assertThat(edges, contains(bc, ab));

        edges = status.findBackwardEdges(start, 50).collect(Collectors.toList());
        assertThat(edges, contains(bc));

        edges = status.findBackwardEdges(start, 200).collect(Collectors.toList());
        assertThat(edges, contains(bc, ab));
    }

    @Test
    void findBackwardEdgesReverse() {
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        StationStatus status = create(stationMap, routes, List.of());
        OrientedLocation start = new OrientedLocation(ab, false, 51);

        List<Edge> edges = status.findBackwardEdges(start, 150).collect(Collectors.toList());

        assertThat(edges, contains(ab, bc));

        edges = status.findBackwardEdges(start, 52).collect(Collectors.toList());
        assertThat(edges, contains(ab, bc));

        edges = status.findBackwardEdges(start, 50).collect(Collectors.toList());
        assertThat(edges, contains(ab));

        edges = status.findBackwardEdges(start, 200).collect(Collectors.toList());
        assertThat(edges, contains(ab, bc));
    }

    @Test
    void firstTrainFrom() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train trainA = Train.create("trainA", 1, a, c);
        Train trainB = Train.create("trainB", 1, a, c, -1);
        StationStatus status = create(stationMap, routes, List.of(trainA, trainB));

        Optional<Train> trainOpt = status.firstTrainFrom(a);
        assertTrue(trainOpt.isPresent());
        assertEquals(trainB, trainOpt.orElseThrow());
    }

    @Test
    void getTrainByEdge1() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 1, a, c)
                .setLocation(new OrientedLocation(bc, true, 99));
        StationStatus status = create(stationMap, routes, List.of(train));

        Map<Edge, Train> trainByEdge = status.getTrainByEdge();

        assertThat(trainByEdge.keySet(), hasSize(1));
        assertThat(trainByEdge, hasEntry(bc, train));
    }

    @Test
    void getTrainByEdge2() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 5, a, c)
                .setLocation(new OrientedLocation(bc, true, 99));
        StationStatus status = create(stationMap, routes, List.of(train));

        Map<Edge, Train> trainByEdge = status.getTrainByEdge();

        assertThat(trainByEdge.keySet(), hasSize(2));
        assertThat(trainByEdge, hasEntry(bc, train));
    }

    @Test
    void getTrainByExit() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train", 1, a, c)
                .exit(c, 0);
        StationStatus status = create(stationMap, routes, List.of(train));

        Map<Exit, Train> trainByExit = status.getTrainByExit();

        assertThat(trainByExit, hasEntry(
                equalTo(c), equalTo(train)
        ));
    }

    @Test
    void getTrainBySection() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 1, a, c)
                .setLocation(new OrientedLocation(bc, true, 99));
        StationStatus status = create(stationMap, routes, List.of(train));

        Map<Section, Train> trainBySection = status.getTrainBySection();

        assertThat(trainBySection.keySet(), containsInAnyOrder(
                hasProperty("id", equalTo("bc"))
        ));
        assertThat(trainBySection.values(), containsInAnyOrder(
                train
        ));
    }

    @Test
    void getTrainBySection2() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 5, a, c)
                .setLocation(new OrientedLocation(bc, true, 99));
        StationStatus status = create(stationMap, routes, List.of(train));

        Map<Section, Train> trainBySection = status.getTrainBySection();

        assertThat(trainBySection.keySet(), containsInAnyOrder(
                hasProperty("id", equalTo("ab")),
                hasProperty("id", equalTo("bc"))
        ));
        assertThat(trainBySection.values(), containsInAnyOrder(
                train, train
        ));
    }

    @Test
    void getTrainsByEntry() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train trainA = Train.create("trainA", 1, a, c, ENTRY_TIMEOUT + 1);
        Train trainB = Train.create("trainB", 1, a, c);
        StationStatus status = create(stationMap, routes, List.of(trainA, trainB));

        Map<String, List<Train>> trainsByEntry = status.getTrainsByEntry();
        assertThat(trainsByEntry, hasKey("a"));
        List<Train> queue = trainsByEntry.get("a");
        assertThat(queue, contains(trainB, trainA));
    }

    @Test
    void isExitClearFalse() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train", 1, a, c)
                .exit(c, 0);
        StationStatus status = create(stationMap, routes, List.of(train));

        assertFalse(status.isExitClear(c));
    }

    @Test
    void isExitClearTrue() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        StationStatus status = create(stationMap, routes, List.of());

        assertTrue(status.isExitClear(c));
    }

    @Test
    void isSectionClearFalse() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Train train = Train.create("train", 1, a, c)
                .setLocation(new OrientedLocation(ab, true, 50));
        StationStatus status = create(stationMap, routes, List.of(train));
        ab = stationMap.getEdge("ab");
        Section section = routes.getSection(ab).orElseThrow();

        assertFalse(status.isSectionClear(section));
    }

    @Test
    void isSectionClearTrue() {
        StationStatus status = create(stationMap, routes, List.of());
        Edge ab = stationMap.getEdge("ab");
        Section section = routes.getSection(ab).orElseThrow();

        assertTrue(status.isSectionClear(section));
    }
}