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

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainEnteringTest {
    static final double DT = 0.1;
    StationStatus status;
    StationMap stationMap;

    /**
     * Entry(a) -- 200m -- Exit(b)
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(200, 0), "ab")
                .addTrack("ab", "a", "b")
                .build();
        status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .build();
    }

    @Test
    void enteringEnqueued() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Entry aRoute = status.getRoute(a);
        Exit bRoute = status.getRoute(b);
        Train t2 = Train.create("t2", 1, aRoute, bRoute).setArrivalTime(ENTRY_TIMEOUT + 1);
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        status = status.setTrains(t2, t1)
                .setTime(ENTRY_TIMEOUT + 1);

        Optional<Train> next = t2.tick(new SimulationContext(status, DT));
        assertTrue(next.isPresent());
        assertThat(next.orElseThrow(), hasProperty("state", equalTo(Train.ENTERING_STATE)));
        assertThat(next.orElseThrow(), hasProperty("arrivalTime", equalTo(ENTRY_TIMEOUT + 1)));
        assertThat(next.orElseThrow(), hasProperty("speed", equalTo(0D)));
    }

    @Test
    void enteringNotClear() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute(a);
        Exit bRoute = status.getRoute(b);
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        Train t2 = Train.create("t2", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));
        status = status.setTrains(t1, t2).setTime(ENTRY_TIMEOUT);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Than ...
        assertTrue(nextOpt.isPresent());

        Train next = nextOpt.orElseThrow();
        assertEquals(Train.ENTERING_STATE, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());
    }

    @Test
    void enteringTimeout() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute(a);
        Exit bRoute = status.getRoute(b);
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        status = status.setTrains(t1)
                .setTime(ENTRY_TIMEOUT);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Than ...
        assertTrue(nextOpt.isPresent());

        Train next = nextOpt.orElseThrow();
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(Train.RUNNING_STATE, next.getState());
        assertThat(next.getLocation(), optionalOf(locatedAt("ab", "b", 200 - DT * MAX_SPEED)));
    }

    @Test
    void enteringWaiting() {
        // Give ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Entry aRoute = status.getRoute(a);
        Exit bRoute = status.getRoute(b);
        Train t1 = Train.create("t1", 1, aRoute, bRoute);
        status = status.setTrains(t1);

        // When ...
        Optional<Train> nextOpt = t1.tick(new SimulationContext(status, DT));

        // Than ...
        assertTrue(nextOpt.isPresent());
        Train next = nextOpt.orElseThrow();
        assertEquals(Train.ENTERING_STATE, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
    }
}