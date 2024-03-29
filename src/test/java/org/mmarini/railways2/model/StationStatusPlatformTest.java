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
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationStatusPlatformTest {

    public static final double BLOCK = 10;
    public static final double LENGTH = 6 * BLOCK;
    public static final double GAME_DURATION = 300d;
    public static final double DT = 0.1;
    public static final int SEED = 1234;
    StationMap stationMap;
    StationStatus status;
    private Node a;
    private Node b;
    private Node c;
    private Node d;
    private Track ab;
    private Track bc;
    private Track cd;
    private Entry aRoute;
    private Exit dRoute;

    void createStatus(Direction... locks) {
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal.createLocks(locks), "b")
                .addRoute(Signal::create, "c")
                .addRoute(Exit::create, "d")
                .build();
        this.aRoute = status.getRoute("a");
        this.dRoute = status.getRoute("d");
    }

    @Test
    void getSegments() {
        // Given ...
        createStatus();

        // When ...
        // |--+--+--+--+oo+--|
        List<EdgeSegment> segs1 = status.getSegments(EdgeLocation.create(ab, b, 2 * BLOCK),
                BLOCK).collect(Collectors.toList());
        // |--+--+--+--+oo+oo|oo+--+--+--+--+--|
        List<EdgeSegment> segs2 = status.getSegments(EdgeLocation.create(ab, b, 2 * BLOCK),
                3 * BLOCK).collect(Collectors.toList());
        // |--+--+--+--+oo+oo|oo+oo+oo+oo+oo+oo|oo+--+--+--+--+--|
        List<EdgeSegment> segs3 = status.getSegments(EdgeLocation.create(ab, b, 2 * BLOCK),
                9 * BLOCK).collect(Collectors.toList());

        // Then ...
        assertThat(segs1, contains(new EdgeSegment(ab, 4 * BLOCK, BLOCK)));
        assertThat(segs2, contains(
                new EdgeSegment(ab, 4 * BLOCK, 0),
                new EdgeSegment(bc, 0, 5 * BLOCK)));
        assertThat(segs3, contains(
                new EdgeSegment(ab, 4 * BLOCK, 0),
                new EdgeSegment(bc, 0, 0),
                new EdgeSegment(cd, 0, 5 * BLOCK)));
    }

    @Test
    void isGameFinished() {
        // Given ...
        createStatus();
        status = status.setTime(GAME_DURATION - DT / 2);

        // When ...
        StationStatus status1 = status.tick(DT, new Random(SEED));

        assertTrue(status1.isGameFinished());
    }

    @Test
    void isNextTracksClearAtClearSignal() {
        // Given ...
        createStatus();
        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 0))
                .setSpeed(10);

        // When ...
        boolean nextTracksClear = status.isNextSignalClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }

    @Test
    void isNextTracksClearAtNotClearSignal() {
        // Given ...
        createStatus(new Direction(ab, b));

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));

        // When ...
        boolean nextTracksClear = status.isNextSignalClear(t1);

        //  Then ...
        assertFalse(nextTracksClear);
    }

    @Test
    void isNextTracksClearAtPlatformLoaded() {
        // Given ...

        createStatus(new Direction(ab, b));

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, 1))
                .setLoaded()
                .setSpeed(10);

        // When ...
        boolean nextTracksClear = status.isNextSignalClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }

    @Test
    void isNextTracksClearForLoad() {
        // Given ...

        createStatus(new Direction(ab, b));

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, 1));

        // When ...
        boolean nextTracksClear = status.isNextSignalClear(t1);

        //  Then ...
        assertFalse(nextTracksClear);
    }

    @Test
    void isNextTracksClearInTrack() {
        // Given ...
        createStatus(new Direction(ab, b));

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 100))
                .setSpeed(1);

        // When ...
        boolean nextTracksClear = status.isNextSignalClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }

    /*
     *  Entry(a) -- ab -- Signals(b) -- Platform(bc) -- Signals(c) -- cd -- Exit(d)
     */
    @BeforeEach
    void setUp() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(3 * LENGTH, 0), "cd")
                .addTrack("ab", "a", "b")
                .addPlatform("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .build();
        this.a = stationMap.getNode("a");
        this.b = stationMap.getNode("b");
        this.c = stationMap.getNode("c");
        this.d = stationMap.getNode("d");
        this.ab = stationMap.getEdge("ab");
        this.bc = stationMap.getEdge("bc");
        this.cd = stationMap.getEdge("cd");
    }
}