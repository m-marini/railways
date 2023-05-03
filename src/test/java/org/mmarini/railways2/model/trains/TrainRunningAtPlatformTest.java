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

package org.mmarini.railways2.model.trains;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.SimulationContext;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.route.Entry;
import org.mmarini.railways2.model.route.Exit;
import org.mmarini.railways2.model.route.Junction;
import org.mmarini.railways2.model.route.RoutesConfig;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainRunningAtPlatformTest {
    static final double DT = 0.1;
    static RoutesConfig routes;
    static StationMap stationMap;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Junction(b) --bcPlatform(500m)-- Junction(c) --cd(500m).. Exit(d)
     * </pre>
     */
    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(500, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(1000, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(1500, 0), "cd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Platform.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .build();
        routes = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Junction::new,
                "c", Junction::new,
                "d", Exit::new
        ));
    }

    @Test
    void runningPlatform() {
        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");
        double distance = 497; // edge length=500m, moving distance=3.6m, distance = 500-3 = 497m
        Train train = Train.create("train2", 1, a, d)
                .setLocation(new OrientedLocation(bc, true, distance))
                .setLoaded(true)
                .setState(Train.State.RUNNING_TRAIN_STATE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.running(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.RUNNING_TRAIN_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(cd, true, distance - bc.getLength() + DT * MAX_SPEED));
    }

    @Test
    void stoppingPlatform() {
        Entry a = routes.getRoute("a");
        Exit d = routes.getRoute("d");
        Edge bc = stationMap.getEdge("bc");
        double distance = 497; // edge length=500m, moving distance=3.6m, distance = 500-3 = 497m
        Train train = Train.create("train2", 1, a, d)
                .setLocation(new OrientedLocation(bc, true, distance))
                .setState(Train.State.RUNNING_TRAIN_STATE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.running(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.LOADING_TRAIN_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(bc, true, bc.getLength()));
    }
}