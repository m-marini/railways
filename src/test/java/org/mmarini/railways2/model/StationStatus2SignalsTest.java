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
import org.mmarini.railways2.model.route.Entry;
import org.mmarini.railways2.model.route.Exit;
import org.mmarini.railways2.model.route.RoutesConfig;
import org.mmarini.railways2.model.route.Signal;
import org.mmarini.railways2.model.trains.Train;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationStatus2SignalsTest {

    private static StationMap stationMap;
    private static RoutesConfig routes;

    /**
     * Entry(a) -- ab -- Signal(b) -- Platform(bc) -- Signal(c) -- cd -- Exit(d)
     */
    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Platform.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(200, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(400, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(600, 0), "cd")
                .build();
        routes = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Signal::create,
                "c", Signal::create,
                "d", Exit::new
        ));
    }

    @Test
    void isNextSignalClearEntry() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge ab = stationMap.getEdge("ab");
        OrientedLocation location = new OrientedLocation(ab, false, 0);

        assertFalse(status.isNextSignalClear(location));
    }

    @Test
    void isNextSignalClearExitTrain() {
        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge cd = stationMap.getEdge("cd");
        Train train = Train.create("train", 1, a, d)
                .exit(d);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));
        OrientedLocation location = new OrientedLocation(cd, true, 0);

        assertFalse(status.isNextSignalClear(location));
    }

    @Test
    void isNextSignalClearTrain() {
        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 1, a, d)
                .setLocation(new OrientedLocation(bc, true, bc.getLength()));
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));
        OrientedLocation location = new OrientedLocation(ab, true, 0);

        assertFalse(status.isNextSignalClear(location));
    }

    @Test
    void isNextSignalClearTrue() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge ab = stationMap.getEdge("ab");
        OrientedLocation location = new OrientedLocation(ab, true, 0);

        assertTrue(status.isNextSignalClear(location));
    }

    @Test
    void isNextSignalClearTrueExit() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge cd = stationMap.getEdge("cd");
        OrientedLocation location = new OrientedLocation(cd, true, 0);

        assertTrue(status.isNextSignalClear(location));
    }

    @Test
    void isNextTrackClearEntry() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge ab = stationMap.getEdge("ab");
        OrientedLocation location = new OrientedLocation(ab, false, 100);

        assertTrue(status.isNextTracksClear(location, 100));
        assertFalse(status.isNextTracksClear(location, 101));
    }

    @Test
    void isNextTrackClearExit() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge cd = stationMap.getEdge("cd");
        OrientedLocation location = new OrientedLocation(cd, false, 100);

        assertTrue(status.isNextTracksClear(location, 100));
        assertTrue(status.isNextTracksClear(location, 101));
    }

    @Test
    void isNextTrackClearExitNotClear() {
        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge cd = stationMap.getEdge("cd");
        Train train = Train.create("train", 1, a, d)
                .exit(d);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        OrientedLocation location = new OrientedLocation(cd, true, 100);

        assertTrue(status.isNextTracksClear(location, 100));
        assertFalse(status.isNextTracksClear(location, 101));
    }

    @Test
    void isNextTrackClearSignal2NotClear() {

        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");
        Train train = Train.create("train", 1, a, d)
                .setLocation(new OrientedLocation(cd, true, bc.getLength()));
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));
        OrientedLocation location = new OrientedLocation(ab, true, 100);

        assertFalse(status.isNextTracksClear(location, 301));
        assertTrue(status.isNextTracksClear(location, 300));
    }

    @Test
    void isNextTrackClearSignalClear() {
        StationStatus status = StationStatus.create(stationMap, routes, List.of());

        Edge ab = stationMap.getEdge("ab");
        OrientedLocation location = new OrientedLocation(ab, true, 100);

        assertTrue(status.isNextTracksClear(location, 200));
    }

    @Test
    void isNextTrackClearSignalNotClear() {

        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Train train = Train.create("train", 1, a, d)
                .setLocation(new OrientedLocation(bc, true, bc.getLength()));
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));
        OrientedLocation location = new OrientedLocation(ab, true, 100);

        assertFalse(status.isNextTracksClear(location, 200));
        assertTrue(status.isNextTracksClear(location, 100));
    }
}