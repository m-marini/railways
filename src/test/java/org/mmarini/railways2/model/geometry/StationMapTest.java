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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mmarini.railways2.model.WithStationMap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.PI;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

public class StationMapTest implements WithStationMap {

    StationMap stationMap;

    /**
     * Create the station map
     * aNode -- ab (Track)--> bNode -- bc (Platform) --> cNode -- cd (Curve) --> dNode
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("aNode", new Point2D.Double(0, 0), "ab")
                .addNode("bNode", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("cNode", new Point2D.Double(200, 0), "bc", "cd")
                .addNode("dNode", new Point2D.Double(200 + RADIUS, RADIUS), "cd")
                .addTrack("ab", "aNode", "bNode")
                .addPlatform("bc", "bNode", "cNode")
                .addCurve("cd", PI / 2, "cNode", "dNode")
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
                hasProperty("location", pointCloseTo(600, 400, 1e-3)),
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

    @Test
    void getBounds() {
        // Given ...

        // When ...
        Rectangle2D bounds = stationMap.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 1e-3));
        assertThat(bounds.getY(), closeTo(0, 1e-3));
        assertThat(bounds.getWidth(), closeTo(200 + RADIUS, 1e-3));
        assertThat(bounds.getHeight(), closeTo(RADIUS, 1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "-10,0,     ab,aNode,0",
            "10,10,     ab,aNode,10",
            "10,-10,    ab,aNode,10",
            "600,0,    cd,cNode,314.159",
            "1010,400,   cd,cNode,628.319"
    })
    void getNearestLocation(double x, double y, String expEdge, String expNode, double expDistance) {
        // Given ...
        // When ...
        EdgeLocation location = stationMap.getNearestLocation(new Point2D.Double(x, y));

        // Then ...
        assertThat(location, locatedAt(expEdge, expNode, expDistance));
    }

    @Override
    public StationMap stationMap() {
        return stationMap;
    }
}