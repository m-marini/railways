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
import static org.mmarini.railways2.Matchers.pointCloseTo;
import static org.mmarini.railways2.Matchers.tupleOf;
import static org.mmarini.railways2.TestFunctions.text;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.RailwayConstants.*;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class DownCrossTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.DownCross");
    private static final double EPSILON = 1e-3;
    private DownCross block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(DownCross.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilderParams> builders = block.getEdgeParams();

        // Then ...
        assertThat(builders, hasSize(8));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("w.ws")),
                hasProperty("node0", equalTo("w")),
                hasProperty("node1", equalTo("ws"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("ws.es")),
                hasProperty("node0", equalTo("ws")),
                hasProperty("node1", equalTo("es"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("es.e")),
                hasProperty("node0", equalTo("es")),
                hasProperty("node1", equalTo("e"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("nw.nws")),
                hasProperty("node0", equalTo("nw")),
                hasProperty("node1", equalTo("nws"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("nws.ses")),
                hasProperty("node0", equalTo("nws")),
                hasProperty("node1", equalTo("ses"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("ses.se")),
                hasProperty("node0", equalTo("ses")),
                hasProperty("node1", equalTo("se"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("ws.ses")),
                hasProperty("node0", equalTo("ws")),
                hasProperty("node1", equalTo("ses"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("nws.es")),
                hasProperty("node0", equalTo("nws")),
                hasProperty("node1", equalTo("es"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String w = block.getEdgeId("w");
        String nw = block.getEdgeId("nw");
        String e = block.getEdgeId("e");
        String se = block.getEdgeId("se");

        // Then ...
        assertEquals("w.ws", w);
        assertEquals("nw.nws", nw);
        assertEquals("es.e", e);
        assertEquals("ses.se", se);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry w = block.getEntryGeometry("w");
        OrientedGeometry nw = block.getEntryGeometry("nw");
        OrientedGeometry e = block.getEntryGeometry("e");
        OrientedGeometry se = block.getEntryGeometry("se");

        // Then ...
        assertThat(w, orientedGeometry(
                pointCloseTo(0, 0, EPSILON),
                equalTo(0)));
        assertThat(nw, orientedGeometry(
                pointCloseTo(0, TRACK_GAP / 2, EPSILON),
                equalTo(-SWITCH_ANGLE_DEG)));
        assertThat(e, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH, 0, EPSILON),
                equalTo(-180)));
        assertThat(se, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH, -TRACK_GAP / 2, EPSILON),
                equalTo(180 - SWITCH_ANGLE_DEG)));
    }

    @Test
    void getInnerNodes() {
        // GIven
        // When ...
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity()).collect(Collectors.toList());

        // Then ...
        assertThat(nodes, hasSize(4));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.ws")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.es")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.nws")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP,
                        PLATFORM_SWITCH_Y,
                        EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.ses")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP,
                        -PLATFORM_SWITCH_Y,
                        EPSILON))
        )));
    }

    @Test
    void getInnerRouteParams() {
        // Given ...
        // When ...
        Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = block.getInnerRouteParams();

        // Then ...
        assertThat(routes, contains(tupleOf(isA(Function.class),
                contains("ws", "es", "nws", "ses"))));
    }

    @BeforeEach
    void setUp() {
        this.block = DownCross.create("id");
    }
}