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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.DEACCELERATION;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainBrakingTest {
    static final double DT = 0.1;
    private static final double LENGTH = 500;
    StationStatus status;
    StationMap stationMap;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .build();
        status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void decelerate() {
        // Given ...
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        double speed = MAX_SPEED / 4;
        Train t1 = Train.create("train2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, LENGTH))
                .setState(Train.BRAKING_STATE)
                .setSpeed(speed);
        status = status.setTrains(t1);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.BRAKING_STATE, next.getState());
        assertEquals(DEACCELERATION * DT + speed, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", LENGTH - speed * DT)));
    }

    @Test
    void runningNotClear() {
        // Given ...
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        double distance = 4; // edge length=500m, moving distance=3.6m, distance = 4m
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.BRAKING_STATE)
                .setSpeed(MAX_SPEED);
        Train t2 = Train.create("t2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, b, 0));
        status = status.setTrains(t1, t2);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.BRAKING_STATE, next.getState());
        assertEquals(MAX_SPEED + DEACCELERATION * DT, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", distance - DT * MAX_SPEED)));
    }

    @Test
    void stop() {
        // Given ...
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        double speed = -DEACCELERATION * DT * 0.9;
        double distance = 10;
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.BRAKING_STATE)
                .setSpeed(speed);
        status = status.setTrains(t1);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.WAITING_FOR_RUN_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", distance - speed * DT)));
    }

    @Test
    void stoppedNotClear() {
        // Given ...
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        double distance = 3; // edge length=500m, moving distance=3.6m, distance = 3m
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.BRAKING_STATE);
        Train t2 = Train.create("t2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, b, 0));
        status = status.setTrains(t1, t2);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.WAITING_FOR_RUN_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));
    }
}