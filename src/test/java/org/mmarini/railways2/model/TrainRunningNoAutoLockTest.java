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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainRunningNoAutoLockTest extends WithStationStatusTest {
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    static final double LENGTH = 500;

    @Test
    void runningAmongJunction() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", 1)
                .build();
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("TT0").changeState(context, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("bc", "c", LENGTH)));

        Signal c = context.getStatus().getRoute("c");
        assertFalse(c.isLocked(direction("bc", "c")));
        assertFalse(c.isLocked(direction("cd", "c")));

        Performance perf = nextOpt._2;
        double expectedTime = 1 / MAX_SPEED;
        assertEquals(expectedTime, perf.getElapsedTime());
        assertEquals(expectedTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(1, perf.getTraveledDistance());
    }

    @Test
    void runningAmongSections() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "d", "bc", "c", 1)
                .build()
                .setAutoLock(false);
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("TT0").changeState(context, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("cd", "d", LENGTH)));

        Signal c = context.getStatus().getRoute("c");
        assertFalse(c.isLocked(direction("bc", "c")));
        assertFalse(c.isLocked(direction("cd", "c")));

        Performance perf = nextOpt._2;
        double expectedTime = 1 / MAX_SPEED;
        assertEquals(expectedTime, perf.getElapsedTime());
        assertEquals(expectedTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(1, perf.getTraveledDistance());
    }

    /**
     * <pre>
     *     Entry(a) --ab(100m)-- Junction(b) --bc(100m)-- Signals(c) --cd(100m)-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void setUp() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(LENGTH * 3, 0), "cd")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Junction::create, "b")
                .addRoute(Signal::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }
}