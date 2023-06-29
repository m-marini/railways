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
import org.mmarini.railways2.model.routes.Signal;
import org.mmarini.railways2.swing.WithTrain;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainExitingTest extends WithStationStatusTest {
    public static final int LENGTH = 500;
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;

    @Test
    void exiting() {
        // Given the exiting train at 10 m from exit point
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .exiting("c", 10d))
                .build();
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("train2").changeState(ctx, 0, DT);

        // Then the train should move at max speed
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(10 + MAX_SPEED * DT, next.getExitDistance());

        // And the elapsed time should be DT
        // And the total time should be DT
        // And the traveled distance should be the train movement
        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(MAX_SPEED * DT, perf.getTraveledDistance());
    }

    @Test
    void exitingStopped() {
        // Given the exiting train at exit point at speed 0
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .exiting("c", 0d)
                        .setSpeed(0))
                .build();

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("train2").changeState(new SimulationContext(status), 0, DT);

        // Then the train should not move but speed should accelerate
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(ACCELERATION * DT, next.getSpeed());
        assertEquals(0, next.getExitDistance());

        // And the elapsed time should be DT
        // And the total time should be DT
        // And the traveled distance should be 0
        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void leaving() {
        // Giving the train completely exited from exit point
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .exiting("c", COACH_LENGTH * 3))
                .build();
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("train2").changeState(ctx, 0, DT);

        // Then the train should move at max speed
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(COACH_LENGTH * 3 + MAX_SPEED * DT, next.getExitDistance());

        // And the elapsed time should be DT
        // And the total time should be DT
        // And the traveled distance should be 0
        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(MAX_SPEED * DT, perf.getTraveledDistance());
    }

    @Test
    void left() {
        // Giving exiting train just 1 meter before leaving completely the map
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .exiting("c", COACH_LENGTH * 3 + EXIT_DISTANCE - 1))
                .build();
        SimulationContext ctx = new SimulationContext(status);


        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("train2").changeState(ctx, 0, DT);

        // Then the train should disappear
        assertTrue(nextOpt._1.isEmpty());

        // And the elapsed time should be the time to reach end of map (1/max speed)
        // And the total time should be the time to reach end of map (1/max speed)
        // And the traveled distance should be 1m
        // And the right outgoing train should be 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(1 / MAX_SPEED, 1e-3));
        assertThat(perf.getTotalTrainTime(), closeTo(1 / MAX_SPEED, 1e-3));
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(1, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(1, perf.getTraveledDistance());
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
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }
}