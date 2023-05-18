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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.LOADING_TIME;

class TrainLoadingTest {
    static final double DT = 0.1;
    StationStatus status;
    StationMap stationMap;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Signal(b) --bc(500m)-- Exit(c)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(500, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(1000, 0), "bc")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Platform.builder("bc"), "b", "c")
                .build();

        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Exit::create, "c")
                .build();
    }

    @Test
    void loaded() {
        Node b = stationMap.getNode("b");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Edge ab = stationMap.getEdge("ab");
        Train t1 = Train.create("t1", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 0))
                .load(0);
        status = status.setTrains(t1)
                .setTime(LOADING_TIME);

        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.WAITING_FOR_RUN_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), locatedAt("ab", "b", 0));
        assertTrue(next.isLoaded());
    }

    @Test
    void waitForLoad() {
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute("a");
        Exit cRoute = status.getRoute("c");
        Train train = Train.create("train2", 1, aRoute, cRoute)
                .setLocation(EdgeLocation.create(ab, b, 0))
                .load(0);
        status = status.setTrains(train);

        Optional<Train> nextOpt = train.tick(new SimulationContext(status, DT));

        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.LOADING_STATE, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), locatedAt("ab", "b", 0));
        assertFalse(next.isLoaded());
    }
}