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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.geometry.Track;
import org.mmarini.railways2.model.route.Entry;
import org.mmarini.railways2.model.route.Exit;
import org.mmarini.railways2.model.route.RoutesConfig;
import org.mmarini.railways2.model.route.Signal;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainExitingTest {
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
    void exiting() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, 0);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.exiting(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.EXITING_TRAIN_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(MAX_SPEED * DT, next.getExitDistance());
    }

    @Test
    void exitingStopped() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, 0).setSpeed(0);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.exiting(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.EXITING_TRAIN_STATE, next.getState());
        assertEquals(ACCELERATION * DT, next.getSpeed());
        assertEquals(0, next.getExitDistance());
    }

    @Test
    void leaving() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, COACH_LENGTH);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.exiting(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.EXITING_TRAIN_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(COACH_LENGTH + MAX_SPEED * DT, next.getExitDistance());
    }

    @Test
    void left() {
        Entry a = routes.getRoute("a");
        Exit c = routes.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, COACH_LENGTH + EXIT_DISTANCE);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> nextOpt = train.exiting(new SimulationContext(status, DT));

        assertFalse(nextOpt.isPresent());
    }
}