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

class CentralCrossTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.CentralCross");
    private static final double EPSILON = 1e-3;
    private CentralCross block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(CentralCross.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilderParams> builders = block.getEdgeParams();

        // Then ...
        assertThat(builders, hasSize(30));
        // #1
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("sw.s1")),
                hasProperty("node0", equalTo("sw")),
                hasProperty("node1", equalTo("s1"))
        )));
        // #2
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("w1.s2")),
                hasProperty("node0", equalTo("w1")),
                hasProperty("node1", equalTo("s2"))
        )));
        // #3
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("w2.s3")),
                hasProperty("node0", equalTo("w2")),
                hasProperty("node1", equalTo("s3"))
        )));
        // #4
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("nw.s4")),
                hasProperty("node0", equalTo("nw")),
                hasProperty("node1", equalTo("s4"))
        )));
        // #5
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s1.s5")),
                hasProperty("node0", equalTo("s1")),
                hasProperty("node1", equalTo("s5"))
        )));
        // #6
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s1.s6")),
                hasProperty("node0", equalTo("s1")),
                hasProperty("node1", equalTo("s6"))
        )));
        // #7
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s2.s5")),
                hasProperty("node0", equalTo("s2")),
                hasProperty("node1", equalTo("s5"))
        )));
        // #8
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s2.s6")),
                hasProperty("node0", equalTo("s2")),
                hasProperty("node1", equalTo("s6"))
        )));
        // #9
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s3.s7")),
                hasProperty("node0", equalTo("s3")),
                hasProperty("node1", equalTo("s7"))
        )));
        // #10
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s3.s8")),
                hasProperty("node0", equalTo("s3")),
                hasProperty("node1", equalTo("s8"))
        )));
        // #11
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s4.s7")),
                hasProperty("node0", equalTo("s4")),
                hasProperty("node1", equalTo("s7"))
        )));
        // #12
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s4.s8")),
                hasProperty("node0", equalTo("s4")),
                hasProperty("node1", equalTo("s8"))
        )));
        // #13
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s5.s9")),
                hasProperty("node0", equalTo("s5")),
                hasProperty("node1", equalTo("s9"))
        )));
        // #14
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s6.x")),
                hasProperty("node0", equalTo("s6")),
                hasProperty("node1", equalTo("x"))
        )));
        // #15
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s7.x")),
                hasProperty("node0", equalTo("s7")),
                hasProperty("node1", equalTo("x"))
        )));
        // #16
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("x.s10")),
                hasProperty("node0", equalTo("x")),
                hasProperty("node1", equalTo("s10"))
        )));
        // #17
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("x.s11")),
                hasProperty("node0", equalTo("x")),
                hasProperty("node1", equalTo("s11"))
        )));
        // #18
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s8.s12")),
                hasProperty("node0", equalTo("s8")),
                hasProperty("node1", equalTo("s12"))
        )));
        // #19
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s9.s13")),
                hasProperty("node0", equalTo("s9")),
                hasProperty("node1", equalTo("s13"))
        )));
        // #20
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s9.s14")),
                hasProperty("node0", equalTo("s9")),
                hasProperty("node1", equalTo("s14"))
        )));
        // #21
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s10.s13")),
                hasProperty("node0", equalTo("s10")),
                hasProperty("node1", equalTo("s13"))
        )));
        // #22
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s10.s14")),
                hasProperty("node0", equalTo("s10")),
                hasProperty("node1", equalTo("s14"))
        )));
        // #23
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s11.s15")),
                hasProperty("node0", equalTo("s11")),
                hasProperty("node1", equalTo("s15"))
        )));
        // #24
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s11.s16")),
                hasProperty("node0", equalTo("s11")),
                hasProperty("node1", equalTo("s16"))
        )));
        // #25
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s12.s15")),
                hasProperty("node0", equalTo("s12")),
                hasProperty("node1", equalTo("s15"))
        )));
        // #26
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s12.s16")),
                hasProperty("node0", equalTo("s12")),
                hasProperty("node1", equalTo("s16"))
        )));
        // #27
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s13.se")),
                hasProperty("node0", equalTo("s13")),
                hasProperty("node1", equalTo("se"))
        )));
        // #28
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s14.e1")),
                hasProperty("node0", equalTo("s14")),
                hasProperty("node1", equalTo("e1"))
        )));
        // #29
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s15.e2")),
                hasProperty("node0", equalTo("s15")),
                hasProperty("node1", equalTo("e2"))
        )));
        // #30
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("s16.ne")),
                hasProperty("node0", equalTo("s16")),
                hasProperty("node1", equalTo("ne"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String nw = block.getEdgeId("nw");
        String w2 = block.getEdgeId("w2");
        String w1 = block.getEdgeId("w1");
        String sw = block.getEdgeId("sw");
        String ne = block.getEdgeId("ne");
        String e2 = block.getEdgeId("e2");
        String e1 = block.getEdgeId("e1");
        String se = block.getEdgeId("se");

        // Then ...
        assertEquals("sw.s1", sw);
        assertEquals("w1.s2", w1);
        assertEquals("w2.s3", w2);
        assertEquals("nw.s4", nw);
        assertEquals("s13.se", se);
        assertEquals("s14.e1", e1);
        assertEquals("s15.e2", e2);
        assertEquals("s16.ne", ne);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry nw = block.getEntryGeometry("nw");
        OrientedGeometry w2 = block.getEntryGeometry("w.2");
        OrientedGeometry w1 = block.getEntryGeometry("w.1");
        OrientedGeometry sw = block.getEntryGeometry("sw");
        OrientedGeometry ne = block.getEntryGeometry("ne");
        OrientedGeometry e2 = block.getEntryGeometry("e.2");
        OrientedGeometry e1 = block.getEntryGeometry("e.1");
        OrientedGeometry se = block.getEntryGeometry("se");

        // Then ...
        assertThat(w1, orientedGeometry(
                pointCloseTo(0, 0, EPSILON),
                equalTo(0)));
        assertThat(w2, orientedGeometry(
                pointCloseTo(0, TRACK_GAP, EPSILON),
                equalTo(SWITCH_ANGLE_DEG)));
        assertThat(nw, orientedGeometry(
                pointCloseTo(0, TRACK_GAP * 1.5, EPSILON),
                equalTo(SWITCH_ANGLE_DEG)));
        assertThat(sw, orientedGeometry(
                pointCloseTo(0, -TRACK_GAP / 2, EPSILON),
                equalTo(-180)));
        assertThat(ne, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH * 2, TRACK_GAP * 1.5, EPSILON),
                equalTo(SWITCH_ANGLE_DEG - 180)));
        assertThat(e2, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH * 2, TRACK_GAP, EPSILON),
                equalTo(-180)));
        assertThat(e1, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH * 2, 0, EPSILON),
                equalTo(-180)));
        assertThat(se, orientedGeometry(
                pointCloseTo(SWITCH_LENGTH * 2, -TRACK_GAP / 2, EPSILON),
                equalTo(180 - SWITCH_ANGLE_DEG)));
    }

    @Test
    void getInnerNodes() {
        // GIven
        // When ...
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity()).collect(Collectors.toList());

        // Then ...
        assertThat(nodes, hasSize(17));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s1")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP, -PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s2")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP, -0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s3")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP, TRACK_GAP, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s4")),
                hasProperty("location", pointCloseTo(
                        SWITCH_GAP, TRACK_GAP + PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s5")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s6")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP, PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s7")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP, TRACK_GAP - PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s8")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH - SWITCH_GAP, TRACK_GAP, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s9")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH + SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s10")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH + SWITCH_GAP, PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s11")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH + SWITCH_GAP, TRACK_GAP - PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s12")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH + SWITCH_GAP, TRACK_GAP, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s13")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH * 2 - SWITCH_GAP, -PLATFORM_SWITCH_Y, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s14")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH * 2 - SWITCH_GAP, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s15")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH * 2 - SWITCH_GAP, TRACK_GAP, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("id.s16")),
                hasProperty("location", pointCloseTo(
                        SWITCH_LENGTH * 2 - SWITCH_GAP, TRACK_GAP + PLATFORM_SWITCH_Y, EPSILON))
        )));
    }

    @Test
    void getInnerRouteParams() {
        // Given ...
        // When ...
        Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = block.getInnerRouteParams();

        // Then ...
        assertThat(routes, hasSize(5));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("s2", "s5", "s1", "s6"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("s3", "s8", "s4", "s7"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("s9","s14","s10","s13"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("s12","s15","s11","s16"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("x"))));
    }

    @BeforeEach
    void setUp() {
        this.block = CentralCross.create("id");
    }
}