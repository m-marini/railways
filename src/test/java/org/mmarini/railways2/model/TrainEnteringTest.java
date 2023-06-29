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
import org.mmarini.railways2.swing.WithTrain;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways2.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainEnteringTest extends WithStationStatusTest {
    public static final int GAME_DURATION = 300;
    public static final double EPSILON_DT = 4.4e-15;
    static final double DT = 0.1;

    @Test
    void enteringCleared() {
        // Given the entering train t1 but stopped
        // And the time 1 seconds after the entry timeout
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b")
                        .setSpeed(0))
                .build()
                .setTime(ENTRY_TIMEOUT + 1);

        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, ENTRY_TIMEOUT + 1, DT);

        // Than the train t1 should enter the entry tracks at 0 speed
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(0, next.getSpeed());

        // And the elapsed time should be 0
        Performance perf = nextOpt._2;
        assertEquals(0, perf.getElapsedTime());
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringEnqueued() {
        // Given
        // train t2 entering a second after the entering train t1
        // and a status just before DT/2 seconds of t2 entering timeout
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .setArrivalTime(ENTRY_TIMEOUT + 1))
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .build()
                .setTime(ENTRY_TIMEOUT + 1 - DT / 2);

        // When ...
        Tuple2<Optional<Train>, Performance> next = train("t2").changeState(new SimulationContext(status), ENTRY_TIMEOUT + 1 - DT / 2, DT);

        // Than train t2 should be stopped in entering state
        assertTrue(next._1.isPresent());
        Train train = next._1.orElseThrow();
        assertThat(train, hasProperty("state", equalTo(Train.STATE_ENTERING)));
        assertThat(train, hasProperty("arrivalTime", equalTo(ENTRY_TIMEOUT + 1)));
        assertThat(train, hasProperty("speed", equalTo(0D)));

        // And the elapsed time should be DT/2 and stop counter incremented by 1
        Performance perf = next._2;
        assertThat(perf.getElapsedTime(), closeTo(DT / 2, 1e-3));
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringNotClear() {
        // Given the entering train t1 and the train t2 running on the entry track (entry not clear)
        // and the status just before DT/2 seconds before entry timout
        double t0 = ENTRY_TIMEOUT - DT / 2;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .at("ab", "b", 0)
                        .running())
                .build()
                .setTime(t0);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, t0, DT);

        // Than train t2 should be stopped in entering state
        assertTrue(nextOpt._1.isPresent());

        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_ENTERING, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());

        // And the elapsed time should be DT/2 and stop counter incremented by 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT / 2, 1e-3));
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringNotClear1() {
        // Given the entering train t1 and the train t2 running on the entry track (entry not clear)
        // and the status just before DT/2 seconds before entry timout
        double t0 = ENTRY_TIMEOUT - EPSILON_DT;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .at("ab", "b", 0)
                        .running())
                .build()
                .setTime(t0);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, t0, DT);

        // Than train t2 should be stopped in entering state
        assertTrue(nextOpt._1.isPresent());

        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_ENTERING, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());

        // And the elapsed time should be DT/2 and stop counter incremented by 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), equalTo(MIN_TIME_INTERVAL));
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringStoppedNotClear() {
        // Given the entering train t1 stopped and the train t2 running on the entry track (entry not clear)
        // and the status just after a second after entry timout
        double t0 = ENTRY_TIMEOUT + 1;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b")
                        .setSpeed(0))
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .at("ab", "b", 0)
                        .running())
                .build()
                .setTime(t0);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, t0, DT);

        // Than train t2 should be stopped in entering state
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_ENTERING, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());

        // And the elapsed time and wait time should be DT and stop counter incremented by 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT, 1e-3));
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(DT, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringTickClear1() {
        // Given the entering train t1 and the train t2 running on the entry track (entry not clear)
        // and the status just before DT/2 seconds before entry timout
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .at("ab", "b", 0)
                        .running())
                .build()
                .setTime(ENTRY_TIMEOUT - EPSILON_DT);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").tick(ctx, DT);

        // Than train t2 should be stopped in entering state
        assertTrue(nextOpt._1.isPresent());

        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_ENTERING, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());

        // And the elapsed time should be DT/2 and stop counter incremented by 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT, EPSILON_DT / 4));
        assertThat(perf.getTotalTrainTime(), closeTo(DT - MIN_TIME_INTERVAL, EPSILON_DT / 4));
        assertThat(perf.getTrainWaitingTime(), closeTo(DT - MIN_TIME_INTERVAL, EPSILON_DT / 4));
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringTickTimeout1() {
        // Given the entering train t1
        // and the status just before DT/2 seconds before entry timout
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .build()
                .setTime(ENTRY_TIMEOUT - EPSILON_DT);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").tick(ctx, DT);

        // Than train t1 should enter in entry track at full speed
        assertTrue(nextOpt._1.isPresent());
        Train train = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, train.getState());
        assertEquals(MAX_SPEED, train.getSpeed());
        assertThat(train.getLocation(), optionalOf(locatedAt("ab", "b", 200 - MAX_SPEED * (DT - EPSILON_DT))));

        // And the elapsed time should be DT/2
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT, EPSILON_DT / 4));
        assertThat(perf.getTotalTrainTime(), closeTo(DT - MIN_TIME_INTERVAL, EPSILON_DT / 4));
        assertThat(perf.getTrainWaitingTime(), equalTo(0d));
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertThat(perf.getTraveledDistance(), closeTo(MAX_SPEED * (DT - MIN_TIME_INTERVAL), MAX_SPEED * EPSILON_DT));
    }

    @Test
    void enteringTimeout() {
        // Given the entering train t1
        // and the status just before DT/2 seconds before entry timout
        double t0 = ENTRY_TIMEOUT - DT / 2;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .build()
                .setTime(t0);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, t0, DT);

        // Than train t1 should enter in entry track at full speed
        assertTrue(nextOpt._1.isPresent());
        Train train = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, train.getState());
        assertEquals(MAX_SPEED, train.getSpeed());
        assertThat(train.getLocation(), optionalOf(locatedAt("ab", "b", 200)));

        // And the elapsed time should be DT/2
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT / 2, 1e-3));
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringTimeout1() {
        // Given the entering train t1
        // and the status just before DT/2 seconds before entry timout
        double t0 = ENTRY_TIMEOUT - EPSILON_DT;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .build()
                .setTime(t0);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, t0, DT);

        // Than train t1 should enter in entry track at full speed
        assertTrue(nextOpt._1.isPresent());
        Train train = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, train.getState());
        assertEquals(MAX_SPEED, train.getSpeed());
        assertThat(train.getLocation(), optionalOf(locatedAt("ab", "b", 200)));

        // And the elapsed time should be DT/2
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), equalTo(MIN_TIME_INTERVAL));
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    @Test
    void enteringWaiting() {
        // Given the entering train t1
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b"))
                .build();
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, 0, DT);

        // Than the train t2 should remain in entering state at full speed
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_ENTERING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());

        // And the elapsed time should be DT
        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }

    /**
     * Entry(a) -- 200m -- Exit(b)
     */
    @BeforeEach
    void setUp() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(200, 0), "ab")
                .addTrack("ab", "a", "b")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .build();
    }
}