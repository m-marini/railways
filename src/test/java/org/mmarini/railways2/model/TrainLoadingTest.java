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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.LOADING_TIME;
import static org.mockito.Mockito.verify;

class TrainLoadingTest extends WithStationStatusTest {
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    private Subscriber<SoundEvent> events;

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
                .addNode("b", new Point2D.Double(500, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(1000, 0), "bc")
                .addTrack("ab", "a", "b")
                .addPlatform("bc", "b", "c")
                .build();

        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void loaded() {
        // Given the loading train
        // And the time at DT/2 before loading time
        status = withTrain()
                .addTrain(
                        new WithTrain.TrainBuilder("train2", 3, "a", "c")
                                .loading(0)
                                .at("ab", "b", 0))
                .build()
                .setTime(LOADING_TIME - DT / 4);
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> transitionOpt = train("train2").changeState(context, DT);

        // Then the train should be loaded in waiting for run
        assertThat(transitionOpt, tupleOf(optionalOf(allOf(
                        hasProperty("state", equalTo(Train.STATE_WAITING_FOR_RUN)),
                        hasProperty("speed", equalTo(0d)),
                        hasProperty("unloaded", equalTo(false)),
                        hasProperty("location", optionalOf(locatedAt("ab", "b", 0))))),
                anything()));

        // And the elapsed time should be DT/2
        // And the total time should be DT/2
        // And the waiting time should be DT/2
        Performance perf = transitionOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(DT / 4, 1e-3));
        assertThat(perf.getTotalTrainTime(), closeTo(DT / 4, 1e-3));
        assertThat(perf.getTrainWaitingTime(), closeTo(DT / 4, 1e-3));
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());

        verify(events).onNext(SoundEvent.STOPPED);
    }

    @Test
    void waitForLoad() {
        // Given ...
        status = withTrain()
                .addTrain(
                        new WithTrain.TrainBuilder("train2", 3, "a", "c")
                                .loading(0)
                                .at("ab", "b", 0))
                .build();
        SimulationContext context = new SimulationContext(status);

        // When ...
        Tuple2<Optional<Train>, Performance> transitionOpt = train("train2").changeState(context, DT);

        // Then ...
        assertThat(transitionOpt, tupleOf(optionalOf(
                        hasProperty("state", equalTo(Train.STATE_LOADING))),
                anything()));
        assertThat(transitionOpt, tupleOf(optionalOf(
                        hasProperty("unloaded", equalTo(true))),
                anything()));
        assertThat(transitionOpt, tupleOf(optionalOf(
                        hasProperty("location", optionalOf(locatedAt("ab", "b", 0)))),
                anything()));

        Performance perf = transitionOpt._2;
        assertEquals(DT, perf.getElapsedTime());
        assertEquals(DT, perf.getTotalTrainTime());
        assertEquals(DT, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(0, perf.getTraveledDistance());
    }
}