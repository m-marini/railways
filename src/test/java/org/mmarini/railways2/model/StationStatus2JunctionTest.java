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
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.swing.WithTrain;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;

class StationStatus2JunctionTest extends WithStationStatusTest {

    public static final double LENGTH = 100;
    public static final int GAME_DURATION = 300;

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab(100)-- Junction(b) --bc(100)-- Junction(c) --cd(10m)-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(2 * LENGTH + 10, 0), "cd")
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
    void getTrainEdges() {
        // Given ...
        Train t = Train.create("t", 10, route("a"), route("d"))
                .setLocation(EdgeLocation.create(edge("cd"), node("d"), 1));
        status = status.setTrains(t);

        // When ...
        List<Edge> edges = status.getTrainEdges(t).collect(Collectors.toList());

        // Then ...
        assertThat(edges, hasSize(3));
        assertThat(edges, contains(equalTo(edge("cd")),
                equalTo(edge("bc")),
                equalTo(edge("ab"))));
    }

    @Test
    void getTrainSegmentsAmongeEdges() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "d", "bc", "c", LENGTH - COACH_LENGTH / 2)
                .build();

        // When ...
        List<EdgeSegment> segments = status.getTrainSegments(train(("TT0"))).collect(Collectors.toList());

        // Then ...
        // Then ...
        assertThat(segments, hasSize(2));
        EdgeSegment segment = segments.get(0);
        assertEquals(this.<Edge>edge("bc"), segment.getEdge());
        assertEquals(0, segment.getDistance0());
        assertEquals(LENGTH - COACH_LENGTH / 2, segment.getDistance1());
        segment = segments.get(1);
        assertEquals(this.<Edge>edge("ab"), segment.getEdge());
        assertEquals(0, LENGTH - COACH_LENGTH * 2.5, segment.getDistance0());
        assertEquals(0, segment.getDistance1());
    }

    @Test
    void getTrainSegmentsEntering() {
        // Given ...
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", LENGTH - COACH_LENGTH / 2)
                .build();

        // When ...
        List<EdgeSegment> segments = status.getTrainSegments(train(("TT0"))).collect(Collectors.toList());

        // Then ...
        assertThat(segments, hasSize(1));
        EdgeSegment segment = segments.get(0);
        assertEquals(this.<Edge>edge("ab"), segment.getEdge());
        assertEquals(0d, segment.getDistance0());
        assertEquals(LENGTH - COACH_LENGTH / 2, segment.getDistance1());
    }

    @Test
    void getTrainSegmentsExiting() {
        // Given ...
        status = withTrain()
                .addTrain(new WithTrain.TrainBuilder("t", 3, "a", "d")
                        .exiting("d", COACH_LENGTH / 2))
                .build();

        // When ...
        List<EdgeSegment> segments = status.getTrainSegments(train(("t"))).collect(Collectors.toList());

        // Then ...
        // Then ...
        assertThat(segments, hasSize(2));
        EdgeSegment segment = segments.get(0);
        assertEquals(this.<Edge>edge("cd"), segment.getEdge());
        assertEquals(0, segment.getDistance0());
        assertEquals(0, segment.getDistance1());
        segment = segments.get(1);
        assertEquals(this.<Edge>edge("bc"), segment.getEdge());
        assertEquals(LENGTH - (COACH_LENGTH * 2.5 - 10), segment.getDistance0());
        assertEquals(0, segment.getDistance1());
    }
}