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
import org.mmarini.railways2.model.geometry.EdgeBuilder;
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

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class CurvesLeftTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.Curves",
            "numTracks: 2",
            "angle: 30");
    static final int ANGLE = 30;
    static final double EPSILON = 1e-3;
    private Curves block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(Curves.class));
        assertThat(block, hasProperty("id", equalTo("name")));
        assertThat(block, hasProperty("angle", equalTo(ANGLE)));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilder> builders = block.getEdgeBuilders();

        // Then ...
        assertThat(builders, hasSize(2));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("w1.e1")),
                hasProperty("node0", equalTo("w1")),
                hasProperty("node1", equalTo("e1"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("w2.e2")),
                hasProperty("node0", equalTo("w2")),
                hasProperty("node1", equalTo("e2"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String w1 = block.getEdgeId("w1");
        String e1 = block.getEdgeId("e1");
        String w2 = block.getEdgeId("w2");
        String e2 = block.getEdgeId("e2");

        // Then ...
        assertEquals("w1.e1", w1);
        assertEquals("w2.e2", w2);
        assertEquals("w1.e1", e1);
        assertEquals("w2.e2", e2);
    }

    @Test
    void getGeometry() {
        // When ...
        OrientedGeometry w1 = block.getEntryGeometry("w1");
        OrientedGeometry e1 = block.getEntryGeometry("e1");
        OrientedGeometry w2 = block.getEntryGeometry("w2");
        OrientedGeometry e2 = block.getEntryGeometry("e2");

        // Then ...
        assertThat(w1, orientedGeometry(
                pointCloseTo(0, 0, EPSILON),
                equalTo(0)));
        assertThat(w2, orientedGeometry(
                pointCloseTo(0, TRACK_GAP, EPSILON),
                equalTo(0)));
        assertThat(e1, orientedGeometry(
                pointCloseTo(
                        (RADIUS + TRACK_GAP) * sin(toRadians(ANGLE)),
                        (RADIUS + TRACK_GAP) * (1 - cos(toRadians(ANGLE))),
                        EPSILON),
                equalTo(ANGLE - 180)));
        assertThat(e2, orientedGeometry(
                pointCloseTo(
                        (RADIUS) * sin(toRadians(ANGLE)),
                        (RADIUS) * (1 - cos(toRadians(ANGLE))),
                        EPSILON),
                equalTo(ANGLE - 180)));
    }

    @Test
    void getInnerNodes() {
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
        this.block = Curves.create("id", 2, ANGLE);
    }
}