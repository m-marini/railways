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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;
import org.mmarini.railways2.swing.WithTrain;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways2.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.DEACCELERATION;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;
import static org.mockito.Mockito.verify;

class TrainBrakingTest extends WithStationStatusTest {
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    private static final double LENGTH = 500;
    Subscriber<SoundEvent> events;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void brakingFromStop() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", 0)
                        .braking(0))
                .build();

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(new SimulationContext(status), 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_RUN, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));

        Performance perf = nextOpt._2;
        assertEquals(0, perf.getElapsedTime());
        assertEquals(0, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());

        verify(events).onNext(SoundEvent.STOPPED);
    }

    @Test
    void brakingStop() {
        // Given ...
        double speed = -DEACCELERATION * DT * 0.9;
        double distance = 10;
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", distance)
                        .braking(speed))
                .build();

        // When ...
        SimulationContext ctx = new SimulationContext(status);
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_RUN, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", distance - speed * DT)));

        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(speed * DT, perf.getTraveledDistance());

        verify(events).onNext(SoundEvent.STOPPED);
    }

    @Test
    void decelerate() {
        // Given ...
        Node b = node("b");
        Edge ab = edge("ab");
        Entry aRoute = route("a");
        Exit cRoute = route("c");
        double speed = MAX_SPEED / 4;
        Train t1 = Train.create("train2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, LENGTH))
                .setState(Train.STATE_BRAKING)
                .setSpeed(speed);
        status = status.setTrains(t1);
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = t1.changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_BRAKING, next.getState());
        assertEquals(DEACCELERATION * DT + speed, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", LENGTH - speed * DT)));

        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(speed * DT, perf.getTraveledDistance());
    }

    @Test
    void runningNotClear() {
        // Given ...
        Node b = node("b");
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Entry aRoute = route("a");
        Exit cRoute = route("c");
        double distance = 4; // edge length=500m, moving distance=3.6m, distance = 4m
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, distance))
                .setState(Train.STATE_BRAKING)
                .setSpeed(MAX_SPEED);
        Train t2 = Train.create("t2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, b, 0));
        status = status.setTrains(t1, t2);

        // When ...
        SimulationContext ctx = new SimulationContext(status);
        Tuple2<Optional<Train>, Performance> nextOpt = t1.changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_BRAKING, next.getState());
        assertEquals(MAX_SPEED + DEACCELERATION * DT, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", distance - DT * MAX_SPEED)));

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
    void startTrain() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", LENGTH)
                        .braking(MAX_SPEED / 4)
                )
                .build();

        // When ...
        StationStatus status1 = status.startTrain("t1");

        // Then ...
        Train t1 = status1.getTrain("t1").orElseThrow();
        assertEquals(Train.STATE_RUNNING, t1.getState());
        verify(events).onNext(SoundEvent.LEAVING);
    }

    @Test
    void stoppedNotClear() {
        // Given ...
        double distance = 3; // edge length=500m, moving distance=3.6m, distance = 3m
        status = withTrain().addTrain(
                        new WithTrain.TrainBuilder("TT0", 3, "a", "c")
                                .at("ab", "b", distance)
                                .braking())
                .addTrain(3, "a", "c", "bc", "b", 0)
                .build();
        Train t0 = status.getTrain("TT0").orElseThrow();

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = t0.changeState(new SimulationContext(status), 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_RUN, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));

        Performance perf = nextOpt._2;
        double expectedTime = distance / MAX_SPEED;
        assertEquals(expectedTime, perf.getElapsedTime());
        assertEquals(expectedTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(distance, perf.getTraveledDistance());

        verify(events).onNext(SoundEvent.STOPPED);
    }
}