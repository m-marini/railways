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

import java.awt.geom.Point2D;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;

class TrainWaitingForSignalLockedTest extends WithStationStatusTest {
    static final double DT = 0.1;
    private static final double LENGTH = 500;

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
        Edge bc = stationMap.getEdge("bc");
        Node b = stationMap.getNode("b");
        status = new StationStatus.Builder(stationMap, 1, null)
                .addRoute(org.mmarini.railways2.model.routes.Entry::create, "a")
                .addRoute(Signal.createLocks(new Direction(bc, b)), "b")
                .addRoute(org.mmarini.railways2.model.routes.Exit::create, "c")
                .build();
    }

    @Test
    void waitForSignalClear() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("train", 3, "a", "c")
                        .at("ab", "b", 0)
                        .waitForSignal())
                .build();

        // When ...
        Tuple2<Train, Double> transition = train("train").changeState(new SimulationContext(status), DT).orElseThrow();

        // Then ...
        Train train = transition._1;
        assertEquals(Train.RUNNING_STATE, train.getState());
        assertEquals(0d, train.getSpeed());
        assertThat(train.getLocation(), optionalOf(locatedAt("ab", "b", 0)));
        assertEquals(DT, transition._2);
    }

    @Test
    void waitForSignalNotClear() {
        // Given ...
        Node b = node("b");
        Node c = node("c");
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Entry aRoute = route("a");
        Exit cRoute = route("c");
        Train t1 = Train.create("train", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 0))
                .setSpeed(0)
                .setState(Train.WAITING_FOR_SIGNAL_STATE);
        Train t2 = Train.create("train1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(bc, c, 0));
        status = status.setTrains(t1, t2);

        // When ...
        Tuple2<Train, Double> next = t1.changeState(new SimulationContext(status), DT).orElseThrow();

        // Then ...
        assertEquals(Train.WAITING_FOR_SIGNAL_STATE, next._1.getState());
        assertEquals(0, next._1.getSpeed());
        assertThat(next._1.getLocation(), optionalOf(locatedAt("ab", "b", 0)));
        assertEquals(0d, next._2);
    }
}