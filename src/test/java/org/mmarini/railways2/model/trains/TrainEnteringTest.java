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

package org.mmarini.railways2.model.trains;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.SimulationContext;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.route.Entry;
import org.mmarini.railways2.model.route.Exit;
import org.mmarini.railways2.model.route.RoutesConfig;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;

class TrainEnteringTest {
    static final double DT = 0.1;
    static RoutesConfig routes;
    static StationMap stationMap;

    @BeforeAll
    static void beforeAll() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(200, 0), "ab")
                .addEdge(Track.builder("ab"), "a", "b")
                .build();
        routes = RoutesConfig.create(stationMap, Map.of(
                "a", Entry::new,
                "b", Exit::new
        ));
    }

    @Test
    void enteringEnqueued() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("b");
        Train train = Train.create("train2", 1, arrival, destination, 1);
        Train train1 = Train.create("train1", 1, arrival, destination);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train, train1))
                .setTime(ENTRY_TIMEOUT + 1);

        Optional<Train> next = train.entering(new SimulationContext(status, DT));
        assertTrue(next.isPresent());
        assertThat(next.orElseThrow(), hasProperty("state", equalTo(Train.State.ENTERING_TRAIN_STATE)));
        assertThat(next.orElseThrow(), hasProperty("arrivalTime", equalTo(1 + ENTRY_TIMEOUT)));
        assertThat(next.orElseThrow(), hasProperty("speed", equalTo(0D)));
    }

    @Test
    void enteringRedSignal() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("b");
        Edge ab = stationMap.getEdge("ab");
        Train train = Train.create("train", 1, arrival, destination);
        Train train1 = Train.create("train1", 1, arrival, destination)
                .setLocation(
                        new OrientedLocation(ab, true, 100)
                );
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train, train1))
                .setTime(ENTRY_TIMEOUT);

        Optional<Train> nextOpt = train.entering(new SimulationContext(status, DT));
        assertTrue(nextOpt.isPresent());

        Train next = nextOpt.orElseThrow();
        assertEquals(Train.State.ENTERING_TRAIN_STATE, next.getState());
        assertEquals(ENTRY_TIMEOUT, next.getArrivalTime());
        assertEquals(0D, next.getSpeed());
    }

    @Test
    void enteringTimeout() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("b");
        Edge ab = stationMap.getEdge("ab");
        Train train = Train.create("train", 1, arrival, destination);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train))
                .setTime(ENTRY_TIMEOUT);

        Optional<Train> nextOpt = train.entering(new SimulationContext(status, DT));
        assertTrue(nextOpt.isPresent());

        Train next = nextOpt.orElseThrow();
        assertEquals(MAX_SPEED, next.getSpeed());
        assertEquals(Train.State.RUNNING_TRAIN_STATE, next.getState());
        assertThat(next.getLocation(), locatedAt(ab, true, DT * MAX_SPEED));
    }

    @Test
    void enteringWaiting() {
        Entry arrival = routes.getRoute("a");
        Exit destination = routes.getRoute("b");
        Train train = Train.create("train", 1, arrival, destination);
        StationStatus status = StationStatus.create(stationMap, routes, List.of(train));

        Optional<Train> next = train.entering(new SimulationContext(status, DT));
        assertTrue(next.isPresent());
        assertThat(next.orElseThrow(), hasProperty("state", equalTo(Train.State.ENTERING_TRAIN_STATE)));
        assertThat(next.orElseThrow(), hasProperty("speed", equalTo(MAX_SPEED)));
    }
}