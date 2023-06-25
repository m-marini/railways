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

package org.mmarini.railways2.model.blocks;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_SIGNAL_GAP;

class BlockStationBuilderCurvesTest {

    public static final int NUM_COACHES = 10;
    public static final double PLATFORM_LENGTH = COACH_LENGTH * NUM_COACHES + PLATFORM_GAP + PLATFORM_SIGNAL_GAP * 2;
    public static final double EPSILON = 1e-3;
    public static final double GAME_DURATION = 300d;
    public static final double FREQUENCY = 0.1;
    private BlockStationBuilder builder;

    @Test
    void getWorldGeometry() {
        // Given ...
        setUp(0);

        // When ...
        OrientedGeometry geo1 = builder.getWorldGeometry("curves.1.e");
        OrientedGeometry geo2 = builder.getWorldGeometry("curves.2.e");

        // Then ...
        assertThat(geo1.getPoint(), pointCloseTo(0, 0, EPSILON));
        assertThat(geo2.getPoint(), pointCloseTo(0, TRACK_GAP, EPSILON));
    }

    /*
     * west.entry --RightCurves -- p.w2 --- p.e2 -- east.exit
     *
     * west.exit  ---RightCurves--- p.e1 -- east.entry
     */
    void setUp(int orientation) {
        Wayout west = Wayout.create("west");
        Platforms platforms = Platforms.create("p", 2, NUM_COACHES);
        Curves curves = Curves.create("curves", 2, -45);
        Wayout east = Wayout.create("east");
        List<Block> blocks = List.of(east, platforms, curves, west);
        Map<String, String> links = Map.of(
                "curves.1.e", "p.1.w",
                "west.entry", "curves.1.w",
                "east.entry", "p.1.e");
        StationDef station = StationDef.create("station", orientation, blocks, links);
        this.builder = new BlockStationBuilder(station, GAME_DURATION, FREQUENCY, null, null);
    }
}