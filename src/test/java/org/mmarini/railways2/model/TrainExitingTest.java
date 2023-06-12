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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainExitingTest {
    public static final int LENGTH = 500;
    static final double DT = 0.1;
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
        status = new StationStatus.Builder(stationMap, 1, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void exiting() {
        // Given ...
        Entry a = status.getRoute("a");
        Exit c = status.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, 10);
        status = status.setTrains(train);

        // When ...
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(10 + MAX_SPEED * DT, next.getExitDistance());
    }

    @Test
    void exitingStopped() {
        Entry a = status.getRoute("a");
        Exit c = status.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, 0).setSpeed(0);
        status = status.setTrains(train);

        // When ...
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(ACCELERATION * DT, next.getSpeed());
        assertEquals(0, next.getExitDistance());
    }

    @Test
    void leaving() {
        Entry a = status.getRoute("a");
        Exit c = status.getRoute("c");
        Train train = Train.create("train2", 1, a, c).exit(c, 0).exit(c, COACH_LENGTH);
        status = status.setTrains(train);

        // When ...
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(COACH_LENGTH + MAX_SPEED * DT, next.getExitDistance());
    }

    @Test
    void left() {
        Entry a = status.getRoute("a");
        Exit c = status.getRoute("c");
        Train train = Train.create("train2", 1, a, c)
                .exit(c, COACH_LENGTH + EXIT_DISTANCE);
        status = status.setTrains(train);

        // When ...
        Optional<Train> nextOpt = train.tick(new SimulationContext(status), DT);

        assertFalse(nextOpt.isPresent());
    }
}