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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways.Matchers.tupleOf;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class WayoutTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways2.model.blocks.Wayout");
    public static final double EPSILON = 1e-3;
    private Wayout block;

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(Wayout.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void getEdgeBuilders() {
        // Given ...
        // When ...
        List<EdgeBuilder> builders = block.getEdgeBuilders();

        // Then ...
        assertThat(builders, hasSize(2));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("entry.in")),
                hasProperty("node0", equalTo("entry")),
                hasProperty("node1", equalTo("in"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("exit.out")),
                hasProperty("node0", equalTo("exit")),
                hasProperty("node1", equalTo("out"))
        )));
    }

    @Test
    void getEdgeId() {
        // When ...
        String entry = block.getEdgeId("entry");
        String exit = block.getEdgeId("exit");

        // Then ...
        assertEquals("entry.in", entry);
        assertEquals("exit.out", exit);
    }

    @Test
    void getGeometry() {
        // When ..
        OrientedGeometry entry = block.getEntryGeometry("entry");
        OrientedGeometry exit = block.getEntryGeometry("exit");

        // Then ...
        assertThat(entry, orientedGeometry(
                pointCloseTo(0, 0, 10e-3),
                equalTo(0)
        ));
        assertThat(exit, orientedGeometry(
                pointCloseTo(0, TRACK_GAP, 10e-3),
                equalTo(0)
        ));
    }

    @Test
    void getInnerNodes() {
        // When ..
        List<NodeBuilderParams> nodes = block.getInnerParams(UnaryOperator.identity()).collect(Collectors.toList());

        // Then ...
        assertThat(nodes, hasSize(2));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("a.in")),
                hasProperty("location", pointCloseTo(1, 0, EPSILON))
        )));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("a.out")),
                hasProperty("location", pointCloseTo(1, TRACK_GAP, EPSILON))
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
                contains("out"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("in"))));
    }

    @BeforeEach
    void setUp() {
        this.block = Wayout.create("a");
    }

}