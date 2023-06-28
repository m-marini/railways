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
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.*;

import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways2.model.Matchers.isSectionWith;

class StationStatusDeadEndTest extends WithStationStatusTest {

    public static final double LENGTH = 100;
    public static final double GAME_DURATION = 300d;

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab-- Switch(b) --bc-- DeadEnd(c)
     *                           --bd-- Exit(d)
     * </pre>
     */
    @BeforeEach
    void Setup() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc")
                .addNode("d", new Point2D.Double(2 * LENGTH, 10), "bd")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("bd", "b", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, null)
                .addRoute(Entry::create, "a")
                .addRoute(Switch::through, "b")
                .addRoute(DeadEnd::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void findSection() {
        // Given ...

        // When ...
        Optional<Tuple2<Section, Set<Edge>>> sectionAB = status.findSection(direction("ab", "b"));
        Optional<Tuple2<Section, Set<Edge>>> sectionCB = status.findSection(direction("bc", "b"));

        // Then ...
        assertThat(sectionAB, optionalOf(tupleOf(
                isSectionWith("ab", "b", "bc", "b", "ab", "bc"),
                empty()
        )));
        assertThat(sectionCB, optionalOf(tupleOf(
                isSectionWith("bc", "b", "ab", "b", "ab", "bc"),
                empty()
        )));
    }

    @Test
    void isNextRouteClear() {
        // Given ... When ... Than...
        assertFalse(status.isNextRouteClear(direction("bc", "c")));
    }
}