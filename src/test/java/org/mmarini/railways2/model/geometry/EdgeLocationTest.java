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

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;

import static java.lang.Math.toRadians;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

class EdgeLocationTest {

    @Test
    void curveLocation() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "ab")
                .addEdge(Curve.builder("ab", toRadians(90)), "a", "b")
                .build();
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Curve ab = station.getEdge("ab");

        EdgeLocation ep1 = EdgeLocation.create(ab, b, RADIUS * toRadians(60));
        assertThat(ep1.getLocation(), pointCloseTo(33.8, 9.1, 1e-1));

        EdgeLocation ep2 = EdgeLocation.create(ab, a, RADIUS * toRadians(60));
        assertThat(ep2.getLocation(), pointCloseTo(58.5, 33.8, 1e-1));
    }

    @Test
    void platformLocation() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 100), "ab")
                .addEdge(Platform.builder("ab"), "a", "b")
                .build();
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Platform edge = station.getEdge("ab");

        EdgeLocation ep1 = EdgeLocation.create(edge, b, 100);
        assertThat(ep1.getLocation(), pointCloseTo(100 - 70.7, 100 - 70.7, 1e-1));

        EdgeLocation ep2 = EdgeLocation.create(edge, a, 100);
        assertThat(ep2.getLocation(), pointCloseTo(70.7, 70.7, 1e-1));
    }

    @Test
    void reverse() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab")
                .addEdge(Track.builder("ab"), "a", "b")
                .build();
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Track edge = station.getEdge("ab");

        EdgeLocation ep1 = EdgeLocation.create(edge, b, 70);

        EdgeLocation rev = ep1.opposite();
        assertThat(rev, hasProperty("direction",
                hasProperty("edge", equalTo(edge))));
        assertThat(rev, hasProperty("direction",
                hasProperty("destination", equalTo(a))));
        assertThat(rev, hasProperty("distance", closeTo(30, 1e-3)));
        assertThat(rev.getLocation(), pointCloseTo(30, 0, 1e-1));
    }

    @Test
    void trackLocation() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 100), "ab")
                .addEdge(Track.builder("ab"), "a", "b")
                .build();
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Track edge = station.getEdge("ab");

        EdgeLocation ep1 = EdgeLocation.create(edge, b, 100);
        assertThat(ep1.getLocation(), pointCloseTo(100 - 70.7, 100 - 70.7, 1e-1));

        EdgeLocation ep2 = EdgeLocation.create(edge, a, 100);
        assertThat(ep2.getLocation(), pointCloseTo(70.7, 70.7, 1e-1));

    }
}