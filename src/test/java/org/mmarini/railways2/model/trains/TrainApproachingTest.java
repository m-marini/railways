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
import org.mmarini.railways2.model.route.RoutesConfig;
import org.mmarini.railways2.model.route.Signal;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainApproachingTest {
    static final double DT = 0.1;
    static RoutesConfig routes;
    static StationMap stationMap;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signal(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(500, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(1000, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .build();
        routes = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Signal::create,
                "c", Exit::new
        ));
    }

    @Test
    void decelerate() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        double speed = MAX_SPEED / 8;
        Train train = Train.create("train2", 1, arrival, destination)
                .setLocation(new OrientedLocation(ab, true, 0))
                .setState(Train.State.APPROACHING_STATE)
                .setSpeed(speed);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.approaching(new SimulationContext(status, DT));
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.APPROACHING_STATE, next.getState());
        assertEquals(DEACCELERATION * DT + speed, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(ab, true, speed * DT));
    }

    @Test
    void runningClear() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Train train = Train.create("train2", 1, arrival, destination)
                .setLocation(new OrientedLocation(ab, true, 0))
                .setSpeed(APPROACH_SPEED)
                .setState(Train.State.APPROACHING_STATE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.approaching(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.APPROACHING_STATE, next.getState());
        assertEquals(APPROACH_SPEED, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(ab, true, DT * APPROACH_SPEED));
    }

    @Test
    void runningNotClear() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        double distance = 499.94; // edge length=500m, moving distance=0.05m, distance = 500-0.06 = 499.94m
        Train train = Train.create("train2", 1, arrival, destination)
                .setLocation(new OrientedLocation(ab, false, distance))
                .setSpeed(APPROACH_SPEED)
                .setState(Train.State.APPROACHING_STATE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.approaching(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.APPROACHING_STATE, next.getState());
        assertEquals(APPROACH_SPEED, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(ab, false, distance + DT * APPROACH_SPEED));
    }

    @Test
    void stoppingNotClear() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        double distance = 499.96; // edge length=500m, moving distance=0.05m, distance = 500-0.04 = 499.96m
        Train train = Train.create("train2", 1, arrival, destination)
                .setLocation(new OrientedLocation(ab, false, distance))
                .setSpeed(APPROACH_SPEED)
                .setState(Train.State.APPROACHING_STATE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.approaching(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.WAITING_FOR_SIGNAL_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), locatedAt(ab, false, ab.getLength()));
    }
}