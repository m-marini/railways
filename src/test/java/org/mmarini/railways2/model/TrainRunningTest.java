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
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.Matchers.emptyOptional;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainRunningTest extends WithStationStatusTest {
    static final double DT = 0.1;
    static final double LENGTH = 500;

    @Test
    void accelerate() {
        // Given ...
        Node b = node("b");
        Edge ab = edge("ab");
        Entry aRoute = route("a");
        Exit bRoute = route("c");
        double speed = MAX_SPEED / 8;
        Train train = Train.create("train2", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(ab, b, LENGTH))
                .setSpeed(speed)
                .setState(Train.RUNNING_STATE);
        status = status.setTrains(train);

        // When ...
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.RUNNING_STATE, next.getState());
        assertEquals(ACCELERATION * DT + speed, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", LENGTH - speed * DT)));
    }

    @Test
    void getStopDistance() {
        // Given ...
        Entry a = route("a");
        Exit c = route("c");
        Train train = Train.create("train2", 1, a, c);

        // When ... Then ...
        assertEquals(MAX_SPEED * MAX_SPEED / DEACCELERATION * -0.5, train.getStopDistance());
    }

    @Test
    void runningAmongSections() {
        // Given ...
        Node b = node("b");
        Node c = node("c");
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Entry aRoute = route("a");
        Exit cRoute = route("c");
        double distance = 3; // moving distance=3.6m, distance = 3
        Train train = Train.create("train2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.RUNNING_STATE);
        status = status.setTrains(train);
        SimulationContext context = new SimulationContext(status);

        // When ...
        Optional<Train> nextOpt = train.tick(context, DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.RUNNING_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("bc", "c", LENGTH - DT * MAX_SPEED + distance)));

        Signal b1 = context.getStatus().getRoute("b");
        assertTrue(b1.isLocked(new Direction(ab, b)));
        assertFalse(b1.isLocked(new Direction(bc, b)));
    }

    @Test
    void runningClear() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        Optional<Train> nextOpt = train("TT0").tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.RUNNING_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", LENGTH - DT * MAX_SPEED)));
    }

    @Test
    void runningNotClear() {
        // Given ...
        Node b = node("b");
        Node c = node("c");
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Entry aRoute = route("a");
        Exit bRoute = route("c");
        double distance = 4; // moving distance=3.6m, distance = 4
        Train t1 = Train.create("t1", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.RUNNING_STATE);
        Train t2 = Train.create("t2", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(bc, c, 0))
                .setState(Train.RUNNING_STATE);
        status = status.setTrains(t1, t2);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.RUNNING_STATE, next.getState());
        assertEquals(MAX_SPEED + DEACCELERATION * DT, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", distance - DT * MAX_SPEED)));
    }

    @Test
    void runningThroughExit() {
        // Given ...
        Node b = node("b");
        Node c = node("c");
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Entry aRoute = route("a");
        Exit cRoute = route("c");
        double distance = 3; // moving distance=3.6m, distance = 3
        Train train = Train.create("train2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, distance))
                .setState(Train.RUNNING_STATE);
        status = status.setTrains(train);
        SimulationContext context = new SimulationContext(status);

        // When ...
        Optional<Train> nextOpt = train.tick(context, DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.EXITING_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), emptyOptional());
        assertThat(next.getExitDistance(), closeTo(DT * MAX_SPEED - distance, 1e-3));
    }

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void setUp() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .build();
        status = new StationStatus.Builder(stationMap, 1, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void speedPhysics0() {
        // Given ...
        Entry a = route("a");
        Exit b = route("c");
        Train train = Train.create("train2", 1, a, b).setSpeed(0);

        // When ... Then ...
        assertEquals(0, train.speedPhysics(0, DT));
        assertEquals(ACCELERATION * DT, train.speedPhysics(MAX_SPEED, DT));
        assertEquals(0.05, train.speedPhysics(0.05, DT));
    }

    @Test
    void speedPhysicsFast() {
        Entry a = route("a");
        Exit b = route("c");
        Train train = Train.create("train2", 1, a, b);

        assertEquals(MAX_SPEED + DEACCELERATION * DT, train.speedPhysics(0, DT));
        assertEquals(MAX_SPEED, train.speedPhysics(MAX_SPEED, DT));
        assertEquals(MAX_SPEED, train.speedPhysics(MAX_SPEED * 2, DT));
        assertEquals(38.7, train.speedPhysics(38.7, DT));
    }

    @Test
    void stoppingNotClear() {
        // Given ...
        double distance = 3; // moving distance=3.6m, distance = 3m
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", distance)
                .addTrain(3, "a", "c", "bc", "c", 0)
                .build();

        // When ...
        Train train = train("TT0");
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.WAITING_FOR_SIGNAL_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));
    }
}