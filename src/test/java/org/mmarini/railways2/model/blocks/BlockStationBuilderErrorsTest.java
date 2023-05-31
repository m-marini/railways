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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockStationBuilderErrorsTest {

    public static final int NUM_COACHES = 10;

    private BlockStationBuilder builder;
    private List<? extends Block> blocks;

    @Test
    void missingConnections1() {
        // Given ...
        Map<String, String> links = Map.of(
                "west.entry", "p.w1",
                "east.exit", "p.e1");
        Station station = Station.create("station", 0, blocks, links);
        this.builder = new BlockStationBuilder(station);

        // When ...
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                builder.getNodeIdByBlockPointId()
        );

        // Then ...
        assertThat(ex.getMessage(), matchesRegex("\\[east\\.entry, p\\.e2, p\\.w2, west\\.exit] have no connection"));
    }

    @Test
    void missingBlock() {
        // Given ...
        Map<String, String> links = Map.of(
                "west.entry", "p.e1");
        Station station = Station.create("station", 0, blocks, links);
        this.builder = new BlockStationBuilder(station);

        // When ...
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                builder.getWorldBlockGeometries()
        );

        // Then ...
        assertThat(ex.getMessage(), matchesRegex("Missing connection with blocks \\[east]"));
    }

    @BeforeEach
    void setUp() {
        Wayout east = Wayout.create("east");
        Platforms platforms = Platforms.create("p", 2, NUM_COACHES);
        Wayout west = Wayout.create("west");
        this.blocks = List.of(east, platforms, west);
    }

    @Test
    void wrongBlockId() {
        // Given ...
        Map<String, String> links = Map.of(
                "west.entry", "none.e1",
                "east.exit", "p.e2");
        Station station = Station.create("station", 0, blocks, links);
        this.builder = new BlockStationBuilder(station);

        // When ...
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            builder.getWorldBlockGeometries();
        });

        // Then ...
        assertThat(ex.getMessage(), matchesRegex("Block \\[none] not found"));
    }

    @Test
    void wrongConnectionId() {
        // Given ...
        Map<String, String> links = Map.of(
                "west.entry", "p.none",
                "east.exit", "p.e2");
        Station station = Station.create("station", 0, blocks, links);
        this.builder = new BlockStationBuilder(station);

        // When ...
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                builder.getWorldBlockGeometries()
        );

        // Then ...
        assertThat(ex.getMessage(), matchesRegex("Block p does not contain connection none"));
    }

}