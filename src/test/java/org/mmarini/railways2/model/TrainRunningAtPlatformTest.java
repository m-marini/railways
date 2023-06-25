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
import org.mmarini.railways2.model.routes.Junction;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;
import static org.mockito.Mockito.verify;

class TrainRunningAtPlatformTest extends WithStationStatusTest {
    public static final double GAME_DURATION = 300d;
    static final double DT = 0.1;
    private static final double LENGTH = 500;
    private Subscriber<SoundEvent> events;

    /**
     * <pre>
     *     Entry(a) --ab(500m)-- Junction(b) --bcPlatform(500m)-- Junction(c) --cd(500m).. Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(LENGTH * 3, 0), "cd")
                .addTrack("ab", "a", "b")
                .addPlatform("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Junction::create, "b")
                .addRoute(Junction::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void runningPlatform() {
        // Given ...
        Node c = stationMap().getNode("c");
        Entry aRoute = status.getRoute("a");
        Exit dRoute = status.getRoute("d");
        Edge bc = stationMap().getEdge("bc");
        double distance = 3; //  moving distance=3.6m, distance = 3m
        Train train = Train.create("train2", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, distance))
                .setLoaded()
                .setState(Train.STATE_RUNNING);
        status = status.setTrains(train);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train.changeState(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_RUNNING, next.getState());
        assertEquals(MAX_SPEED, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("cd", "d", LENGTH)));

        Performance perf = nextOpt._2;
        double expTime = distance / MAX_SPEED;
        assertEquals(expTime, perf.getElapsedTime());
        assertEquals(expTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
        assertEquals(distance, perf.getTraveledDistance());
    }

    @Test
    void stoppingPlatform() {
        // Given ...
        Node c = stationMap().getNode("c");
        Entry aRoute = status.getRoute("a");
        Exit dRoute = status.getRoute("d");
        Edge bc = stationMap().getEdge("bc");
        double distance = 3; //  moving distance=3.6m, distance = 4m
        Train train = Train.create("train2", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, distance))
                .setState(Train.STATE_RUNNING);
        status = status.setTrains(train);

        // When ...
        Tuple2<Optional<Train>, Performance> nextOpt = train.changeState(new SimulationContext(status), DT);

        // Then ...
        assertTrue(nextOpt._1.isPresent());
        Train next = nextOpt._1.orElseThrow();
        assertEquals(Train.STATE_LOADING, next.getState());
        assertEquals(0, next.getSpeed());
        assertThat(next.getLocation(), optionalOf(locatedAt("bc", "c", 0)));

        Performance perf = nextOpt._2;
        double expTime = distance / MAX_SPEED;
        assertEquals(expTime, perf.getElapsedTime());
        assertEquals(expTime, perf.getTotalTrainTime());
        assertEquals(0, perf.getTrainWaitingTime());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(1, perf.getTrainStopNumber());
        assertEquals(distance, perf.getTraveledDistance());

        verify(events).onNext(SoundEvent.ARRIVED);
    }
}