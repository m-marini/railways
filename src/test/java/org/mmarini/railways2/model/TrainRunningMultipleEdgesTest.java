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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Junction;

import java.awt.geom.Point2D;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainRunningMultipleEdgesTest extends WithStationStatusTest {
    public static final int SEED = 1234;
    public static final double GAP = 1;
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    static final double LENGTH = 500;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Junction(b) --bc(10m)-- Junction(c) --cd(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void setUp() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH + GAP, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(LENGTH * 2 + GAP, 0), "cd")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Junction::create, "b")
                .addRoute(Junction::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void tick() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", 0)
                .build();

        // When ...
        StationStatus status1 = status.tick(DT, new Random(SEED));

        // Then ...
        Train train = status1.getTrain("TT0").orElseThrow();
        assertEquals(Train.STATE_RUNNING, train.getState());
        assertEquals(MAX_SPEED, train.getSpeed());
        double expDistance = LENGTH - MAX_SPEED * DT + GAP;
        assertThat(train.getLocation(), optionalOf(locatedAt(
                "cd", "d", expDistance)));
    }
}