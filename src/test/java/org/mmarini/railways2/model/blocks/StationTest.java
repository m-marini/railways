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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.TestFunctions.text;

class StationTest {

    public static final String DOC = text("---",
            "name: station",
            "orientation: 10",
            "blocks:",
            "  Platform:",
            "    class: org.mmarini.railways2.model.blocks.Platforms",
            "    numPlatforms: 8",
            "    length: 10",
            "links: ",
            "  Platform.1: Platform.2",
            "  Platform.3: Platform.4");

    private StationDef station;
    private Platforms platform;

    @Test
    void createBlocks() throws IOException {
        JsonNode root = Utils.fromText(DOC);
        this.station = StationDef.create(root, Locator.root());
        assertThat(station.getBlocksById(), hasEntry(
                equalTo("Platform"),
                allOf(
                        isA(Platforms.class),
                        hasProperty("id", equalTo("Platform"))
                )));

    }

    /**
     * Returns the junctions by block
     */
    private Map<Block, List<BlockJunction>> createJunctionsByBlock() {
        Map<Block, List<Tuple2<Block, BlockJunction>>> groupBy = station.getDeclaredJunctions().stream()
                .flatMap(j -> Stream.of(
                        Tuple2.of(j.getFrom().<Block>getBlock(), j),
                        Tuple2.of(j.getTo().<Block>getBlock(), j)
                ))
                .collect(Collectors.groupingBy(Tuple2::getV1));
        // Creates the junction by block
        return Tuple2.stream(groupBy)
                .map(t -> t.setV2(
                        t._2.stream()
                                .map(Tuple2::getV2)
                                .collect(Collectors.toList())
                ))
                .collect(Tuple2.toMap());
    }

    @Test
    void decodeConnection() {
        // Given ...

        // When ...
        BlockPoint w1 = station.decodeConnection("platforms.1.w");
        BlockPoint e2 = station.decodeConnection("platforms.2.e");

        // Then ...
        assertEquals(new BlockPoint(platform, "1.w"), w1);
        assertEquals(new BlockPoint(platform, "2.e"), e2);
    }

    @Test
    void duplicatedLinks() {
        // Given ...
        List<? extends Block> blocks = List.of(
                Wayout.create("left"),
                Tracks.create("leftTracks", 2, 1),
                Signals.create("leftSignals", 2),
                Platforms.create("platforms", 2, 10),
                Signals.create("rightSignals", 2),
                Tracks.create("rightTracks", 2, 1),
                Wayout.create("right"));
        Map<String, String> links = Map.of(
                "left.entry", "leftTracks.w.1",
                "leftTracks.w.2", "left.entry",
                "leftTrack.e.1", "leftSignals.1.w",
                "leftSignal.1.e", "Platform.w.1",
                "Platform.e.1", "rightSignal.w.1",
                "rightSignal.e.1", "rightTracks.w.1",
                "rightTracks.e.1", "right.exit"
        );

        // When ...
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () ->
                StationDef.create("id", 0, blocks, links)
        );

        // Then ...
        assertThat(ex.getMessage(), matchesRegex("Duplicated links \\[left\\.entry]"));
    }

    @Test
    void getBlockPoints() {
        // Given ...

        // When ...
        Collection<BlockPoint> junctions = station.getBlockPoints();

        // Then ...
        assertThat(junctions, containsInAnyOrder(
                hasToString("left.entry"),
                hasToString("left.exit"),
                hasToString("leftTracks.1.e"),
                hasToString("leftTracks.1.w"),
                hasToString("leftTracks.2.w"),
                hasToString("leftTracks.2.e"),
                hasToString("leftSignals.1.w"),
                hasToString("leftSignals.1.e"),
                hasToString("leftSignals.2.w"),
                hasToString("leftSignals.2.e"),
                hasToString("platforms.1.e"),
                hasToString("platforms.1.w"),
                hasToString("platforms.2.e"),
                hasToString("platforms.2.w"),
                hasToString("rightSignals.1.e"),
                hasToString("rightSignals.1.w"),
                hasToString("rightSignals.2.e"),
                hasToString("rightSignals.2.w"),
                hasToString("rightTracks.1.e"),
                hasToString("rightTracks.1.w"),
                hasToString("rightTracks.2.e"),
                hasToString("rightTracks.2.w"),
                hasToString("right.entry"),
                hasToString("right.exit")
        ));
    }

    @Test
    void getDeclaredJunctions() {
        // Given ...

        // When ...
        Collection<BlockJunction> junctions = station.getDeclaredJunctions();

        // Then ...
        assertThat(junctions, containsInAnyOrder(
                hasToString("leftSignals.1.w - leftTracks.1.e"),
                hasToString("left.entry - leftTracks.1.w"),
                hasToString("rightSignals.1.e - rightTracks.1.w"),
                hasToString("leftSignals.1.e - platforms.1.w"),
                hasToString("platforms.1.e - rightSignals.1.w"),
                hasToString("right.exit - rightTracks.1.e")
        ));
    }

    /*
     * left -- leftTracks -- leftSignals -- platforms -- rightSignals -- rightTracks -- right
     */
    @BeforeEach
    void setUp() throws IOException {
        List<? extends Block> blocks = List.of(
                Wayout.create("left"),
                Tracks.create("leftTracks", 2, 1),
                Signals.create("leftSignals", 2),
                Platforms.create("platforms", 2, 10),
                Signals.create("rightSignals", 2),
                Tracks.create("rightTracks", 2, 1),
                Wayout.create("right"));
        Map<String, String> links = Map.of(
                "left.entry", "leftTracks.1.w",
                "leftTracks.1.e", "leftSignals.1.w",
                "leftSignals.1.e", "platforms.1.w",
                "platforms.1.e", "rightSignals.1.w",
                "rightSignals.1.e", "rightTracks.1.w",
                "rightTracks.1.e", "right.exit"
        );
        this.station = StationDef.create("id", 0, blocks, links);
        this.platform = (Platforms) station.getBlock("platforms").orElseThrow();
    }
}