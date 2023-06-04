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
import static org.mmarini.railways2.model.RailwayConstants.*;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class LeftSwitchTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.LeftSwitch");
    public static final double EPSILON = 1e-3;
    private LeftSwitch block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(LeftSwitch.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilderParams> builders = block.getEdgeParams();

        // Then ...
        assertThat(builders, hasSize(4));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("entry.switch")),
                hasProperty("node0", equalTo("entry")),
                hasProperty("node1", equalTo("switch"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("switch.through")),
                hasProperty("node0", equalTo("switch")),
                hasProperty("node1", equalTo("through"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("switch.d")),
                hasProperty("node0", equalTo("switch")),
                hasProperty("node1", equalTo("d"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("d.diverged")),
                hasProperty("node0", equalTo("d")),
                hasProperty("node1", equalTo("diverged"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String entry = block.getEdgeId("entry");
        String through = block.getEdgeId("through");
        String diverged = block.getEdgeId("diverged");

        // Then ...
        assertEquals("entry.switch", entry);
        assertEquals("switch.through", through);
        assertEquals("d.diverged", diverged);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry entry = block.getEntryGeometry("entry");
        OrientedGeometry through = block.getEntryGeometry("through");
        OrientedGeometry diverged = block.getEntryGeometry("diverged");

        // Then ...
        assertThat(entry, orientedGeometry(
                pointCloseTo(0, 0, EPSILON),
                equalTo(0)));
        assertThat(through, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH, 0, EPSILON),
                equalTo(-180)));
        assertThat(diverged, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH, TRACK_GAP / 2, EPSILON),
                equalTo(SWITCH_ANGLE_DEG - 180)));
    }

    @Test
    void getInnerNodes() {
        // GIven
        // When ...
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity()).collect(Collectors.toList());

        // Then ...
        assertThat(nodes, hasSize(2));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.switch")),
                hasProperty("location", pointCloseTo(SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.d")),
                hasProperty("location", pointCloseTo(SWITCH_LENGTH - SWITCH_GAP, PLATFORM_SWITCH_Y, EPSILON))
        )));
    }

    @Test
    void getInnerRouteParams() {
        // Given ...
        // When ...
        Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = block.getInnerRouteParams();

        // Then ...
        assertThat(routes, hasSize(2));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("switch"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("d"))));
    }

    @BeforeEach
    void setUp() {
        this.block = new LeftSwitch("id");
    }
}