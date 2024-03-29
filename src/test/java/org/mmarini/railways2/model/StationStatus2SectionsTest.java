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
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.swing.WithTrain;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.model.Matchers.isEdge;
import static org.mmarini.railways2.model.Matchers.isRoute;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.ENTRY_TIMEOUT;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class StationStatus2SectionsTest extends WithStationStatusTest {

    public static final double GAME_DURATION = 300d;
    private Subscriber<SoundEvent> events;

    @Test
    void createSections() {
        // Given ...

        // When ...
        Collection<Section> sections = status.createSections();

        // Then ...
        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(isEdge("ab"))))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("edges", containsInAnyOrder(isEdge("cd"))))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("crossingSections", empty()))));
    }

    @Test
    void createTrainByExit() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t1", 3, "a", "b")
                        .exiting("b"))
                .addTrain(new WithTrain.TrainBuilder("t2", 3, "a", "b")
                        .exiting("d"))
                .addTrain(new WithTrain.TrainBuilder("t3", 3, "a", "b"))
                .build();

        // When ...
        Map<Exit, Train> map = status.createTrainByExit();

        // Than ...
        assertNotNull(map);
        assertEquals(2, map.size());
        assertThat(map, hasEntry(isRoute("b"), equalTo(train("t1"))));
        assertThat(map, hasEntry(isRoute("d"), equalTo(train("t2"))));
    }

    @Test
    void createTrainByExitExiting() {
        // Give ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("ex", 3, "a", "b")
                        .exiting("b", 3 * COACH_LENGTH - 1))
                .addTrain(3, "a", "b", "cd", "d", 0)
                .build();

        // When ...
        Map<Section, Train> map = status.createTrainBySection();

        // Than ...
        assertNotNull(map);
        assertEquals(2, map.size());
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("ab")),
                equalTo(train("ex"))));
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("cd")),
                equalTo(train("TT1"))));
    }

    @Test
    void createTrainBySection() {
        // Give ...
        status = withTrain()
                .addTrain(3, "a", "b", "ab", "b", 0)
                .addTrain(3, "a", "b", "cd", "d", 0)
                .addTrain(new WithTrain.TrainBuilder("ex", 3, "a", "b")
                        .exiting("b", 3 * COACH_LENGTH + 1))
                .build();

        // When ...
        Map<Section, Train> map = status.createTrainBySection();

        // Than ...
        assertNotNull(map);
        assertEquals(2, map.size());
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("ab")),
                equalTo(train("TT0"))));
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("cd")),
                equalTo(train("TT1"))));
    }

    @Test
    void generate2Train() {
        // Given ...
        status = status.setTime(12);
        double lambda = 1;
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(1d, 1d, 0d); // 2 train
        when(random.nextInt(anyInt()))
                .thenReturn(0, 0, 0, 0) // arriva, idl, len, destination
                .thenReturn(1, 1, 3, 1); // arrival, id, len, destination

        // When ...
        List<Train> trains = status.createNewTrains(new ArrayList<>(), lambda, random);

        // Then ...
        assertThat(trains, hasSize(2));
        assertEquals("T100", trains.get(0).getId());
        assertEquals(3, trains.get(0).getNumCoaches());
        assertEquals(route("a"), trains.get(0).getArrival());
        assertEquals(route("b"), trains.get(0).getDestination());
        assertEquals(12 + ENTRY_TIMEOUT, trains.get(0).getArrivalTime());

        assertEquals("T101", trains.get(1).getId());
        assertEquals(6, trains.get(1).getNumCoaches());
        assertEquals(route("c"), trains.get(1).getArrival());
        assertEquals(route("d"), trains.get(1).getDestination());
        assertEquals(12 + ENTRY_TIMEOUT, trains.get(1).getArrivalTime());
        verify(events, times(2)).onNext(SoundEvent.ARRIVING);
    }

    @Test
    void generateDuplicatedTrain() {
        // Given ...
        status = status.setTime(12);
        double lambda = 1;
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(1d, 1d, 0d); // 2 train
        when(random.nextInt(anyInt()))
                .thenReturn(0, 0, 0, 0) // arrival, id, len, destination
                .thenReturn(1, 0, 1, 3, 1); // arrival, dupid, id, len, destination

        // When ...
        List<Train> trains = status.createNewTrains(new ArrayList<>(), lambda, random);

        // Then ...
        assertThat(trains, hasSize(2));
        assertEquals("T100", trains.get(0).getId());
        assertEquals(3, trains.get(0).getNumCoaches());
        assertEquals(route("a"), trains.get(0).getArrival());
        assertEquals(route("b"), trains.get(0).getDestination());
        assertEquals(12 + ENTRY_TIMEOUT, trains.get(0).getArrivalTime());

        assertEquals("T101", trains.get(1).getId());
        assertEquals(6, trains.get(1).getNumCoaches());
        assertEquals(route("c"), trains.get(1).getArrival());
        assertEquals(route("d"), trains.get(1).getDestination());
        assertEquals(12 + ENTRY_TIMEOUT, trains.get(1).getArrivalTime());
        verify(events, times(2)).onNext(SoundEvent.ARRIVING);
    }

    @Test
    void generateNoTrain() {
        // Given ...
        double lambda = 1;
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(0d);

        // When ...
        List<Train> trains = status.createNewTrains(new ArrayList<>(), lambda, random);

        // Then ...
        assertThat(trains, empty());
    }

    @Test
    void generateTrain() {
        // Given ...
        status = status.setTime(12);
        double lambda = 1;
        Random random = mock(Random.class);
        when(random.nextDouble()).thenReturn(1d, 0d); // 1 train
        when(random.nextInt(anyInt())).thenReturn(0, 0, 0, 0); // id, len, arrival, destination

        // When ...
        List<Train> trains = status.createNewTrains(new ArrayList<>(), lambda, random);

        // Then ...
        assertThat(trains, hasSize(1));
        assertEquals("T100", trains.get(0).getId());
        assertEquals(3, trains.get(0).getNumCoaches());
        assertEquals(route("a"), trains.get(0).getArrival());
        assertEquals(route("b"), trains.get(0).getDestination());
        assertEquals(12 + ENTRY_TIMEOUT, trains.get(0).getArrivalTime());
        verify(events, times(1)).onNext(SoundEvent.ARRIVING);
    }

    @Test
    void isExitClear() {
        // Given ...
        Train t1 = Train.create("t1", 1, route("a"), route("b"))
                .setState(Train.STATE_EXITING)
                .setExitingNode(route("b"));
        Train t2 = Train.create("t2", 1, route("a"), route("b"));
        status = status.setTrains(t1, t2);

        // When ... Then ...
        assertFalse(status.isExitClear(route("b")));
        assertTrue(status.isExitClear(route("d")));
    }

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab--  Exit(b)
     * Entry(c) --cd--  Exit(d)
     * </pre>
     */
    @BeforeEach
    void setUp() {
        this.events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab")
                .addNode("c", new Point2D.Double(200, 0), "cd")
                .addNode("d", new Point2D.Double(300, 0), "cd")
                .addTrack("ab", "a", "b")
                .addTrack("cd", "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .addRoute(Entry::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }
}