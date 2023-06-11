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
import static org.hamcrest.Matchers.*;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.LOADING_TIME;

class TrainLoadingTest extends WithStationStatusTest {
    static final double DT = 0.1;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signals(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(500, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(1000, 0), "bc")
                .addTrack("ab", "a", "b")
                .addPlatform("bc", "b", "c")
                .build();

        status = new StationStatus.Builder(stationMap, 1, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void loaded() {
        // Given ...
        status = withTrain()
                .addTrain(
                        new WithTrain.TrainBuilder("train2", 3, "a", "c")
                                .loading(0)
                                .at("ab", "b", 0))
                .build()
                .setTime(LOADING_TIME);

        // When ...
        Optional<Tuple2<Train, Double>> transitionOpt = train("train2").loading(new SimulationContext(status), DT);

        // Then ...
        assertThat(transitionOpt, optionalOf(tupleOf(allOf(
                        hasProperty("state", equalTo(Train.WAITING_FOR_RUN_STATE)),
                        hasProperty("speed", equalTo(0d)),
                        hasProperty("unloaded", equalTo(false)),
                        hasProperty("location", optionalOf(locatedAt("ab", "b", 0)))),
                equalTo(DT)
        )));
    }


    @Test
    void overTimeLoaded() {
        // Given ...
        status = withTrain()
                .addTrain(
                        new WithTrain.TrainBuilder("train2", 3, "a", "c")
                                .loading(0)
                                .at("ab", "b", 0))
                .build()
                .setTime(LOADING_TIME - DT / 4);

        // When ...
        Optional<Tuple2<Train, Double>> transitionOpt = train("train2").loading(new SimulationContext(status), DT);

        // Then ...
        assertThat(transitionOpt, optionalOf(tupleOf(allOf(
                        hasProperty("state", equalTo(Train.WAITING_FOR_RUN_STATE)),
                        hasProperty("speed", equalTo(0d)),
                        hasProperty("unloaded", equalTo(false)),
                        hasProperty("location", optionalOf(locatedAt("ab", "b", 0)))),
                closeTo(DT * 3 / 4, 1e-3)
        )));
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

        // When ...
        Optional<Tuple2<Train, Double>> transitionOpt = train("train2").loading(new SimulationContext(status), DT);

        // Then ...
        assertThat(transitionOpt, optionalOf(tupleOf(
                hasProperty("state", equalTo(Train.LOADING_STATE)),
                anything()
        )));
        assertThat(transitionOpt, optionalOf(tupleOf(
                hasProperty("unloaded", equalTo(true)),
                anything()
        )));
        assertThat(transitionOpt, optionalOf(tupleOf(
                hasProperty("location", optionalOf(locatedAt("ab", "b", 0))),
                anything()
        )));
        assertThat(transitionOpt, optionalOf(tupleOf(
                anything(),
                equalTo(0d)
        )));
    }
}