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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationStatusPlatformTest {

    static StationMap stationMap;
    static StationStatus status;

    /*
     *  Entry(a) -- ab -- Signal(b) -- Platform(bc) -- Signal(c) -- cd -- Exit(d)
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(300, 0), "cd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .build();
    }

    void createStatus(Direction... locks) {
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(nodes -> {
                    Signal signal = Signal.create(nodes);
                    for (Direction lock : locks) {
                        signal = signal.lock(lock);
                    }
                    return signal;
                }, "b")
                .addRoute(Signal::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void isNextTracksClearAtClearSignal() {
        // Given ...
        createStatus();
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Entry aRoute = status.getRoute(a);
        Exit dRoute = status.getRoute(d);
        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 0))
                .setSpeed(10);

        // When ...
        boolean nextTracksClear = status.isNextTracksClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }

    @Test
    void isNextTracksClearAtNotClearSignal() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");

        createStatus(new Direction(ab, b));
        Entry aRoute = status.getRoute(a);
        Exit dRoute = status.getRoute(d);

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));

        // When ...
        boolean nextTracksClear = status.isNextTracksClear(t1);

        //  Then ...
        assertFalse(nextTracksClear);
    }

    @Test
    void isNextTracksClearAtPlatformLoaded() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        createStatus(new Direction(ab, b));
        Entry aRoute = status.getRoute(a);
        Exit dRoute = status.getRoute(d);

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, 1))
                .setSpeed(10);

        // When ...
        boolean nextTracksClear = status.isNextTracksClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }

    @Test
    void isNextTracksClearForLoad() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");

        createStatus(new Direction(ab, b));
        Entry aRoute = status.getRoute(a);
        Exit dRoute = status.getRoute(d);

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(bc, c, 1));

        // When ...
        boolean nextTracksClear = status.isNextTracksClear(t1);

        //  Then ...
        assertFalse(nextTracksClear);
    }

    @Test
    void isNextTracksClearInTrack() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");

        createStatus(new Direction(ab, b));
        Entry aRoute = status.getRoute(a);
        Exit dRoute = status.getRoute(d);

        Train t1 = Train.create("t1", 1, aRoute, dRoute)
                .setLocation(EdgeLocation.create(ab, b, 100))
                .setSpeed(1);

        // When ...
        boolean nextTracksClear = status.isNextTracksClear(t1);

        //  Then ...
        assertTrue(nextTracksClear);
    }
}