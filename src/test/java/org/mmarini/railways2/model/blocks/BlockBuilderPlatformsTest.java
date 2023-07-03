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
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.routes.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mmarini.railways2.Matchers.tupleOf;

class BlockBuilderPlatformsTest {

    public static final double EPSILON = 1e-3;
    public static final int NUM_COACHES = 10;
    public static final double GAME_DURATION = 300d;
    public static final double FREQUENCY = 0.1;

    private BlockBuilder builder;

    @Test
    void build() {
        // Given ...
        setUp(0);

        // When ...
        StationStatus status = builder.buildStatus(GAME_DURATION, FREQUENCY, null, null);

        // Then ...
        assertThat(status.getRoute("west.in"), allOf(isA(Entry.class),
                hasToString("Entry[west.in]")
        ));
        assertThat(status.getRoute("west.out"), allOf(isA(Exit.class),
                hasToString("Exit[west.out]")
        ));
        assertThat(status.getRoute("east.in"), allOf(isA(Entry.class),
                hasToString("Entry[east.in]")
        ));
        assertThat(status.getRoute("east.out"), allOf(isA(Exit.class),
                hasToString("Exit[east.out]")
        ));
        assertThat(status.getRoute("p.1.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signalw]")
        ));
        assertThat(status.getRoute("p.2.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.2.signalw]")
        ));
        assertThat(status.getRoute("p.1.signale"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signale]")
        ));
        assertThat(status.getRoute("p.2.signale"), allOf(isA(Signal.class),
                hasToString("Signal[p.2.signale]")
        ));
        assertThat(status.getRoute("p.1.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signalw]")
        ));
        assertThat(status.getRoute("p.1.w"), allOf(isA(Junction.class),
                hasToString("Junction[p.1.w]")
        ));
        assertThat(status.getRoute("p.2.w"), allOf(isA(Junction.class),
                hasToString("Junction[p.2.w]")
        ));
        assertThat(status.getRoute("east.entry"), allOf(isA(Junction.class),
                hasToString("Junction[east.entry]")
        ));
        assertThat(status.getRoute("east.exit"), allOf(isA(Junction.class),
                hasToString("Junction[east.exit]")
        ));
    }

    @Test
    void createInnerRoutes() {
        // Given ...
        setUp(0);

        // When ...
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = builder.createInnerRoutes().collect(Collectors.toList());

        // Then ...
        assertThat(routes, hasSize(8));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("west.in"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("west.out"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("east.in"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("east.out"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.1.signalw"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.1.signale"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.2.signalw"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.2.signale"))));
    }

    @Test
    void createJunctionRoutes() {
        // Given ...
        setUp(0);

        // When ...
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> junctions = builder.createJunctionRoutes().collect(Collectors.toList());

        // Then ...
        assertThat(junctions, hasSize(4));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("p.1.w"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("p.2.w"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("east.entry"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("east.exit"))));
    }

    /*
     * west.entry --- p.2.w --- p.2.e --- east.exit
     *
     * west.exit  --- p.1.w --- p.1.e --- east.entry
     */
    void setUp(int orientation) {
        Wayout east = Wayout.create("east");
        Platforms platforms = Platforms.create("p", 2, NUM_COACHES);
        Wayout west = Wayout.create("west");
        List<Block> blocks = List.of(east, platforms, west);
        Map<String, String> links = Map.of(
                "west.entry", "p.2.w",
                "east.entry", "p.1.e");
        StationDef station = StationDef.create("station", orientation, blocks, links);
        this.builder = new BlockBuilder(station);
    }
}