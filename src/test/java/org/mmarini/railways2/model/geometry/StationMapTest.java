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

package org.mmarini.railways2.model.geometry;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static java.lang.Math.PI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.railways.TestFunctions.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

public class StationMapTest {

    static StationMap stationMap;

    /**
     * Create the station map
     * aNode -- ab (Track)--> bNode -- bc (Platform) --> cNode -- cd (Curve) --> dNode
     */
    @BeforeAll
    static void createStation() {
        stationMap = new StationBuilder("station")
                .addNode("aNode", new Point2D.Double(0, 0), "ab")
                .addNode("bNode", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("cNode", new Point2D.Double(200, 0), "bc", "cd")
                .addNode("dNode", new Point2D.Double(200 + RADIUS, RADIUS), "cd")
                .addEdge(Track.builder("ab"), "aNode", "bNode")
                .addEdge(Platform.builder("bc"), "bNode", "cNode")
                .addEdge(Curve.builder("cd", PI / 2), "cNode", "dNode")
                .build();
        assertNotNull(stationMap);
    }

    @Test
    void create() {
        assertEquals("station", stationMap.getId());
        assertThat(stationMap.getNodeMap().keySet(), hasSize(4));
        assertThat(stationMap.getEdges().keySet(), hasSize(3));

        Node aNode = stationMap.getNode("aNode");
        Node bNode = stationMap.getNode("bNode");
        Node cNode = stationMap.getNode("cNode");
        Node dNode = stationMap.getNode("dNode");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");

        assertThat(aNode, allOf(
                hasProperty("id", equalTo("aNode")),
                hasProperty("location", equalTo(new Point2D.Double())),
                hasProperty("edges", containsInAnyOrder(ab))
        ));
        assertThat(bNode, allOf(
                hasProperty("id", equalTo("bNode")),
                hasProperty("location", pointCloseTo(100, 0, 1e-3)),
                hasProperty("edges", containsInAnyOrder(ab, bc))
        ));
        assertThat(cNode, allOf(
                hasProperty("id", equalTo("cNode")),
                hasProperty("location", equalTo(new Point2D.Double(200, 0))),
                hasProperty("edges", containsInAnyOrder(bc, cd))
        ));
        assertThat(dNode, allOf(
                hasProperty("id", equalTo("dNode")),
                hasProperty("location", pointCloseTo(267.615, 67.615, 1e-3)),
                hasProperty("edges", containsInAnyOrder(cd))
        ));

        assertThat(ab, allOf(
                hasProperty("id", equalTo("ab")),
                isA(Track.class),
                hasProperty("node0", equalTo(aNode)),
                hasProperty("node1", equalTo(bNode))
        ));
        assertThat(bc, allOf(
                hasProperty("id", equalTo("bc")),
                isA(Platform.class),
                hasProperty("node0", equalTo(bNode)),
                hasProperty("node1", equalTo(cNode))
        ));
        assertThat(cd, allOf(
                hasProperty("id", equalTo("cd")),
                isA(Curve.class),
                hasProperty("node0", equalTo(cNode)),
                hasProperty("node1", equalTo(dNode)),
                hasProperty("angle", equalTo(PI / 2))
        ));
    }

    @Test
    void directions() {
        Node a = stationMap.getNode("aNode");
        Node b = stationMap.getNode("bNode");
        Node c = stationMap.getNode("cNode");
        Node d = stationMap.getNode("dNode");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");

        assertThat(a.getExits(), containsInAnyOrder(
                new Direction(ab, b)));
        assertThat(b.getExits(), containsInAnyOrder(
                new Direction(ab, a),
                new Direction(bc, c)));
        assertThat(c.getExits(), containsInAnyOrder(
                new Direction(bc, b),
                new Direction(cd, d)));
        assertThat(d.getExits(), containsInAnyOrder(
                new Direction(cd, c)));
    }

/*
    @Test
    void curveLength() {
        assertThat(stationMap.edgeLength(cd), closeTo(RADIUS * PI / 2, 1e-3));
        assertThat(stationMap.edgeLength("cd"), closeTo(RADIUS * PI / 2, 1e-3));
    }

    @Test
    void findTerminalNodes() {
        List<Node> nodes = stationMap.findTerminalNodes(ab);
        assertThat(nodes, hasItems(aNode, bNode));
    }

    @Test
    void nodeLocation() {
        assertEquals(new Point2D.Double(), stationMap.nodeLocation("aNode"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stationMap.nodeLocation("x"));
        assertThat(ex.getMessage(), matchesRegex("Node x not found"));
    }

    @Test
    void platformLength() {
        assertEquals(100D, stationMap.edgeLength(bc));
        assertEquals(100D, stationMap.edgeLength("bc"));
    }

    @Test
    void trackLength() {
        assertEquals(100D, stationMap.edgeLength(ab));
        assertEquals(100D, stationMap.edgeLength("ab"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stationMap.edgeLength("x"));
        assertThat(ex.getMessage(), matchesRegex("Edge x not found"));
    }

 */
}