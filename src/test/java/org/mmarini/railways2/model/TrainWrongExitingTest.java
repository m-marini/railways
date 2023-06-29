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
import org.mmarini.railways2.model.routes.Switch;
import org.mmarini.railways2.swing.WithTrain;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways2.model.RailwayConstants.*;

class TrainWrongExitingTest extends WithStationStatusTest {
    public static final int LENGTH = 500;
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Switch(b) --bc(500m)-- Exit(c)
     *                                     --bd(500m)-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc")
                .addNode("d", new Point2D.Double(LENGTH * 2, 10), "bd")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("bd", "b", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Switch::through, "b")
                .addRoute(Exit::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void left() {
        // Giving exiting train just 1 meter before leaving completely the map from the wrong exit
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train2", 3, "a", "d")
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
        // And the wrong outgoing train should be 1
        Performance perf = nextOpt._2;
        assertThat(perf.getElapsedTime(), closeTo(1 / MAX_SPEED, 1e-3));
        assertThat(perf.getTotalTrainTime(), closeTo(1 / MAX_SPEED, 1e-3));
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(1, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(1, perf.getTraveledDistance());
    }
}