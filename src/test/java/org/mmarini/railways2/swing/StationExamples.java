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

package org.mmarini.railways2.swing;

import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.*;

import java.awt.geom.Point2D;
import java.util.Set;

import static org.mmarini.railways2.model.MathUtils.RAD90;
import static org.mmarini.railways2.model.MathUtils.RAD_90;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;

public interface StationExamples {
    double GAP = 10;
    double LENGTH = 500;
    double CROSS_LENGTH = 100;
    double GAME_DURATION = 300d;

    /**
     * <pre>
     * Entry(a) --platform-- Signals(b) --track-- Switch(c) --track-- DeadEnd(d)
     *                                                      --track-- Exit(e)
     * </pre>
     */
    static StationStatus createDeadEndStation() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, 0), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH + GAP, 0), "bc", "cd", "ce")
                .addNode("d", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH, 0), "cd")
                .addNode("e", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH + GAP, TRACK_GAP), "ce")
                .addPlatform("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .addTrack("ce", "c", "e")
                .build();

        return new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Switch::through, "c")
                .addRoute(DeadEnd::create, "d")
                .addRoute(Exit::create, "e")
                .build();
    }

    /**
     * <pre>
     * Entry(a) --platform-- Signals(b) --track-- DoubleSwitch(c) --------track-------- DoubleSwitch(d) --track-- Exit(e)
     *                                                           --track--   --track--
     *                                                                     X
     *                                                           --track--   --track--
     * Entry(f) --platform-- Signals(g) --track-- DoubleSwitch(h) --------track--------- DoubleSwitch(i) --track-- Exit(j)
     * </pre>
     */
    static StationStatus create2CrossExitStation(boolean through) {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, 0), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(LENGTH + GAP, 0), "bc", "cd", "ci")
                .addNode("d", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH, 0), "de", "cd", "hd")
                .addNode("e", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH + GAP, 0), "de")
                .addNode("f", new Point2D.Double(0, -TRACK_GAP), "fg")
                .addNode("g", new Point2D.Double(LENGTH, -TRACK_GAP), "fg", "gh")
                .addNode("h", new Point2D.Double(LENGTH + GAP, -TRACK_GAP), "gh", "hi", "hd")
                .addNode("i", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH, -TRACK_GAP), "ij", "hi", "ci")
                .addNode("j", new Point2D.Double(LENGTH + GAP + CROSS_LENGTH + GAP, -TRACK_GAP), "ij")
                .addPlatform("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .addTrack("de", "d", "e")
                .addPlatform("fg", "f", "g")
                .addTrack("gh", "g", "h")
                .addTrack("hi", "h", "i")
                .addTrack("ij", "i", "j")
                .addTrack("ci", "c", "i")
                .addTrack("hd", "h", "d")
                .build();

        return new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal.createLocks(), "b")
                .addRoute(DoubleSlipSwitch.create(through), "c", "d", "h", "i")
                .addRoute(Exit::create, "e")
                .addRoute(Entry::create, "f")
                .addRoute(Signal.createLocks(), "g")
                .addRoute(Exit::create, "j")
                .build();
    }

    /**
     * <pre>
     * 0         500                     540                    560          580        590
     * |---500---|-----------40----------|----------20----------|-----20-----|----10----|
     * i3 --p3-- s3 ---------t11-------- d1 -------------t12---------------- d2 --t15-- o2
     *                                      \--------\                     /
     *                                                \--t13--\   /--t14--/
     *                                                          c
     *                                                 /--t7--/   \--t9--\
     *                           /-------t5------- ds3                    \
     *                          /          /--t6-/                         \
     * i2 --p2-- s2 --t2--- ds1 ----t4---+---t4--- ds4 ---------t8----------- d3 --t10-- o1
     *                          /--t6--/         /
     *                      ds2 -------t3-------/
     * i1 --p1-- s1 --t1--/
     * |---500---|----30----|----------20----------|-----10-----|------20-----|----10----|
     * 0         500        530                    550          560           580        590
     * </pre>
     */
    static StationStatus create3Entry2ExitStation(String... deviated) {
        StationMap stationMap = new StationBuilder("station")
                .addNode("i1", new Point2D.Double(0, 0), "p1")
                .addNode("s1", new Point2D.Double(500, 0), "p1", "t1")
                .addNode("ds2", new Point2D.Double(530, TRACK_GAP * 3 / 4), "t1", "t6", "t3")
                .addNode("i2", new Point2D.Double(0, TRACK_GAP), "p2")
                .addNode("s2", new Point2D.Double(500, TRACK_GAP), "p2", "t2")
                .addNode("ds1", new Point2D.Double(530, TRACK_GAP), "t2", "t4", "t5")
                .addNode("ds4", new Point2D.Double(550, TRACK_GAP), "t8", "t4", "t3")
                .addNode("ds3", new Point2D.Double(550, TRACK_GAP * 5 / 4), "t7", "t6", "t5")
                .addNode("d3", new Point2D.Double(580, TRACK_GAP), "t10", "t8", "t9")
                .addNode("o1", new Point2D.Double(590, TRACK_GAP), "t10")
                .addNode("c", new Point2D.Double(560, TRACK_GAP * 3 / 2), "t7", "t14", "t13", "t9")
                .addNode("i3", new Point2D.Double(0, TRACK_GAP * 2), "p3")
                .addNode("s3", new Point2D.Double(500, TRACK_GAP * 2), "p3", "t11")
                .addNode("d1", new Point2D.Double(540, TRACK_GAP * 2), "t11", "t12", "t13")
                .addNode("d2", new Point2D.Double(580, TRACK_GAP * 2), "t15", "t12", "t14")
                .addNode("o2", new Point2D.Double(590, TRACK_GAP * 2), "t15")
                .addPlatform("p1", "i1", "s1")
                .addPlatform("p2", "i2", "s2")
                .addPlatform("p3", "i3", "s3")
                .addTrack("t1", "s1", "ds2")
                .addTrack("t2", "s2", "ds1")
                .addTrack("t3", "ds2", "ds4")
                .addTrack("t4", "ds1", "ds4")
                .addTrack("t5", "ds1", "ds3")
                .addTrack("t6", "ds2", "ds3")
                .addTrack("t7", "ds3", "c")
                .addTrack("t8", "ds4", "d3")
                .addTrack("t9", "c", "d3")
                .addTrack("t10", "d3", "o1")
                .addTrack("t11", "s3", "d1")
                .addTrack("t12", "d1", "d2")
                .addTrack("t13", "d1", "c")
                .addTrack("t14", "c", "d2")
                .addTrack("t15", "d2", "o2")
                .build();

        Set<String> deviatedSet = Set.of(deviated);

        return new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "i1")
                .addRoute(Entry::create, "i2")
                .addRoute(Entry::create, "i3")
                .addRoute(Exit::create, "o1")
                .addRoute(Exit::create, "o2")
                .addRoute(Signal.createLocks(), "s1")
                .addRoute(Signal.createLocks(), "s2")
                .addRoute(Signal.createLocks(), "s3")
                .addRoute(Switch.create(!deviatedSet.contains("d1")), "d1")
                .addRoute(Switch.create(!deviatedSet.contains("d2")), "d2")
                .addRoute(Switch.create(!deviatedSet.contains("d3")), "d3")
                .addRoute(DoubleSlipSwitch.create(!deviatedSet.contains("ds1")), "ds1", "ds4", "ds2", "ds3")
                .addRoute(CrossRoute::create, "c")
                .build();
    }

    /**
     * <pre>
     * Entry(a) --curve-- Signals(b) --platform-- Signals(c) --track-- Switch(d) -- track -- Exit(e)
     *                                                                         -- curve -- Exit(f)
     * </pre>
     */
    static StationStatus createSwitchStation() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, LENGTH), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(2 * LENGTH + GAP, 0), "cd", "de", "df")
                .addNode("e", new Point2D.Double(3 * LENGTH + GAP, 0), "de")
                .addNode("f", new Point2D.Double(3 * LENGTH + GAP, -LENGTH), "df")
                .addCurve("ab", RAD90, "a", "b")
                .addPlatform("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .addTrack("de", "d", "e")
                .addCurve("df", RAD_90, "d", "f")
                .build();
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge bc = stationMap.getEdge("bc");
        return new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Signal.createLocks(new Direction(bc, b)), "b")
                .addRoute(Signal.createLocks(new Direction(bc, c)), "c")
                .addRoute(Switch.create(true), "d")
                .addRoute(Exit::create, "e")
                .addRoute(Exit::create, "f")
                .build();
    }
}
