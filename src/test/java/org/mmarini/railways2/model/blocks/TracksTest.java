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
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.EdgeBuilderParams;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class TracksTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.Tracks",
            "numTracks: 2",
            "length: 10");
    private Tracks block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(Tracks.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilderParams> builders = block.getEdgeParams();

        // Then ...
        assertThat(builders, hasSize(2));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("1.track")),
                hasProperty("node0", equalTo("1.w")),
                hasProperty("node1", equalTo("1.e"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("2.track")),
                hasProperty("node0", equalTo("2.w")),
                hasProperty("node1", equalTo("2.e"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String w1 = block.getEdgeId("1.w");
        String e1 = block.getEdgeId("1.e");
        String w2 = block.getEdgeId("2.w");
        String e2 = block.getEdgeId("2.e");

        // Then ...
        assertEquals("1.track", w1);
        assertEquals("2.track", w2);
        assertEquals("1.track", e1);
        assertEquals("2.track", e2);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry w1 = block.getEntryGeometry("1.w");
        OrientedGeometry w2 = block.getEntryGeometry("2.w");
        OrientedGeometry e1 = block.getEntryGeometry("1.e");
        OrientedGeometry e2 = block.getEntryGeometry("2.e");

        // Then
        assertThat(w1, orientedGeometry(
                pointCloseTo(0, 0, 10e-3),
                equalTo(0)
        ));
        assertThat(w2, orientedGeometry(
                pointCloseTo(0, TRACK_GAP, 10e-3),
                equalTo(0)
        ));
        assertThat(e1, orientedGeometry(
                pointCloseTo(10 * SEGMENT_LENGTH, 0, 10e-3),
                equalTo(180)
        ));
        assertThat(e2, orientedGeometry(
                pointCloseTo(10 * SEGMENT_LENGTH, TRACK_GAP, 10e-3),
                equalTo(180)
        ));
    }

    @Test
    void getInnerParams() {
        // Given ...
        // When ...
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity())
                .collect(Collectors.toList());

        // Then ...
        assertThat(nodes, empty());
    }

    @Test
    void getInnerRouteParams() {
        // Given ...
        // When ...
        Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = block.getInnerRouteParams();

        // Then ...
        assertThat(routes, empty());
    }

    @BeforeEach
    void setUp() {
        this.block = Tracks.create("id", 2, 10);
    }
}