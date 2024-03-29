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
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mockito.Mockito.verify;

class TrainWaitingForRunTest extends WithStationStatusTest {
    public static final int LENGTH = 500;
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    private Subscriber<SoundEvent> events;

    @Test
    void revertTrain() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", 0)
                        .waitForRun()
                )
                .build();

        // When ...
        StationStatus status1 = status.revertTrain("t1");

        // Then ...
        Train t1 = status1.getTrain("t1").orElseThrow();
        assertEquals(Train.STATE_RUNNING, t1.getState());
        assertThat(t1.getLocation(), optionalOf(locatedAt("ab", "a", LENGTH - COACH_LENGTH * 3)));
        verify(events).onNext(SoundEvent.LEAVING);
    }

    @Test
    void revertTrain1() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("bc", "c", LENGTH)
                        .waitForRun()
                )
                .build();

        // When ...
        StationStatus status1 = status.revertTrain("t1");

        // Then ...
        Train t1 = status1.getTrain("t1").orElseThrow();
        assertEquals(Train.STATE_RUNNING, t1.getState());
        assertThat(t1.getLocation(), optionalOf(locatedAt("ab", "a", LENGTH - COACH_LENGTH * 3)));
        verify(events).onNext(SoundEvent.LEAVING);
    }

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void setUp() {
        this.events = Mockito.mock();
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
    void startTrain() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", LENGTH)
                        .waitForRun()
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
    void stopTrain() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", LENGTH)
                        .waitForRun()
                )
                .build();

        // When ...
        StationStatus status1 = status.stopTrain("t1");

        // Then ...
        assertSame(status, status1);
    }

    @Test
    void waitForRun() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "c")
                        .at("ab", "b", 0)
                        .waitForRun())
                .build();

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train("t1").changeState(new SimulationContext(status), 0, DT);

        // Then ...
        Optional<Train> trainOpt = nextOpt._1;
        assertTrue(trainOpt.isPresent());

        Train next = trainOpt.orElseThrow();
        assertEquals(Train.STATE_WAITING_FOR_RUN, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 0)));

        Performance perf = nextOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(DT, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }
}