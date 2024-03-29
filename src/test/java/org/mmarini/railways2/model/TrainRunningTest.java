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
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.Matchers.emptyOptional;
import static org.mmarini.railways2.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.*;
import static org.mockito.Mockito.verify;

class TrainRunningTest extends WithStationStatusTest {
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    static final double LENGTH = 500;
    private Subscriber<SoundEvent> events;

    @Test
    void exitingNotClear() {
        // Given the exiting train at exit point and an other train exiting the node
        double distance = 1;
        status = withTrain()
                .addTrain(3, "a", "c", "bc", "c", distance)
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .exiting("c", 3 * COACH_LENGTH + 1))
                .build();

        // When ...
        Tuple2<Optional<Train>, Performance> next = train("TT0").changeState(new SimulationContext(status), 0, DT);

        // Then ...
        assertEquals(distance / MAX_SPEED, next._2.getElapsedTime());
        assertEquals(distance, next._2.getTraveledDistance());
        assertEquals(distance / MAX_SPEED, next._2.getTotalTrainTime());
        assertTrue(next._1.isPresent());
        Train tt0 = next._1.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_SIGNAL, tt0.getState());
        assertEquals(0, tt0.getSpeed());
        assertThat(tt0.getLocation(), optionalOf(locatedAt("bc", "c", 0)));
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
    void revertTrain() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.revertTrain("TT0");

        // Then ...
        assertSame(status, status1);
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
                .setState(Train.STATE_RUNNING);
        status = status.setTrains(train);
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train.changeState(context, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("bc", "c", LENGTH)));

        Signal b1 = context.getStatus().getRoute("b");
        assertTrue(b1.isLocked(new Direction(ab, b)));
        assertFalse(b1.isLocked(new Direction(bc, b)));

        Performance perf = nextOpt._2;
        double expectedTime = distance / MAX_SPEED;
        assertEquals(expectedTime, perf.getElapsedTime());
        assertEquals(expectedTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(distance, perf.getTraveledDistance());
    }

    @Test
    void runningClear() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        SimulationContext ctx = new SimulationContext(status);
        Tuple2<Optional<Train>, Performance> nextOpt = train("TT0").changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", LENGTH - DT * MAX_SPEED)));

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
    void runningFromStop() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "c")
                        .at("ab", "b", 0)
                        .running(0))
                .build();

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("train2").changeState(new SimulationContext(status), 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(APPROACH_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));

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
    void runningNotClear() {
        // Given ...
        double distance = 4; // moving distance=3.6m, distance = 4
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", distance)
                .addTrain(3, "a", "c", "bc", "c", 0)
                .build();
        SimulationContext ctx = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("TT0").changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
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
    void runningThroughExit() {
        // Given ...
        double distance = 3; // moving distance=3.6m, distance = 3
        status = withTrain()
                .addTrain(3, "a", "c", "bc", "c", distance)
                .build();
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("TT0").changeState(context, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_EXITING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(route("c"), next.getExitingNode());
        assertThat(next.getLocation(), emptyOptional());
        assertEquals(0, next.getExitDistance());

        Performance perf = nextOpt._2;
        double expectedTime = distance / MAX_SPEED;
        assertEquals(expectedTime, perf.getElapsedTime());
        assertEquals(expectedTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(distance, perf.getTraveledDistance());
    }

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void setUp() {
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
    void startTrain() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.startTrain("t1");

        // Then ...
        assertSame(status, status1);
    }

    @Test
    void stopTrain() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.stopTrain("TT0");

        // Then ...
        Train t1 = status1.getTrain("TT0").orElseThrow();
        assertEquals(Train.STATE_BRAKING, t1.getState());
        verify(events).onNext(SoundEvent.BRAKING);
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
        SimulationContext ctx = new SimulationContext(status);
        Tuple2<Optional<Train>, Performance> nextOpt = train.changeState(ctx, 0, DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_SIGNAL, next.getState());
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
    }
}