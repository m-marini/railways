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
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_SIGNAL_GAP;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class TerminalPlatformsTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.TerminalPlatforms",
            "numPlatforms: 8",
            "length: 10");
    public static final double EPSILON = 1e-3;
    private TerminalPlatforms block;

    @Test
    void create() throws IOException {
        block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"platforms"}, new Class[]{String.class});
        assertThat(block, isA(TerminalPlatforms.class));
        assertThat(block, hasProperty("id", equalTo("platforms")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilderParams> builders = block.getEdgeParams();

        // Then ...
        assertThat(builders, hasSize(4));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("1.platform")),
                hasProperty("node0", equalTo("1.deadEnd")),
                hasProperty("node1", equalTo("1.signal"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("2.platform")),
                hasProperty("node0", equalTo("2.deadEnd")),
                hasProperty("node1", equalTo("2.signal"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("1.track")),
                hasProperty("node0", equalTo("1.signal")),
                hasProperty("node1", equalTo("1.e"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("2.track")),
                hasProperty("node0", equalTo("2.signal")),
                hasProperty("node1", equalTo("2.e"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String e1 = block.getEdgeId("1.e");
        String e2 = block.getEdgeId("2.e");

        // Then ...
        assertEquals("1.track", e1);
        assertEquals("2.track", e2);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry e1 = block.getEntryGeometry("1.e");
        OrientedGeometry e2 = block.getEntryGeometry("2.e");

        // Then
        assertThat(e1, orientedGeometry(
                pointCloseTo(10 * COACH_LENGTH + PLATFORM_GAP + PLATFORM_SIGNAL_GAP, 0, 10e-3),
                equalTo(-180)
        ));
        assertThat(e2, orientedGeometry(
                pointCloseTo(10 * COACH_LENGTH + PLATFORM_GAP + PLATFORM_SIGNAL_GAP, TRACK_GAP, 10e-3),
                equalTo(-180)
        ));
    }

    @Test
    void getInnerNodes() {
        // Given ...
        // When ...
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity())
                .collect(Collectors.toList());

        assertThat(nodes, hasSize(4));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.1.deadEnd")),
                hasProperty("location", pointCloseTo(0, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.2.deadEnd")),
                hasProperty("location", pointCloseTo(0, -TRACK_GAP, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.1.signal")),
                hasProperty("location", pointCloseTo(10 * COACH_LENGTH + PLATFORM_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.2.signal")),
                hasProperty("location", pointCloseTo(10 * COACH_LENGTH + PLATFORM_GAP, -TRACK_GAP, EPSILON))
        )));
    }

    @Test
    void getInnerRouteParams() {
        // Given ...
        // When ...
        Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = block.getInnerRouteParams();

        // Then ...
        assertThat(routes, hasSize(4));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("1.deadEnd"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("2.deadEnd"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("1.signal"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("2.signal"))));
    }

    @BeforeEach
    void setUp() {
        this.block = TerminalPlatforms.create("id", 2, 10);
    }
}