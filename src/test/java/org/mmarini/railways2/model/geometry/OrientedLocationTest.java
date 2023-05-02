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
import static org.mmarini.railways.TestFunctions.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

class OrientedLocationTest {

    @Test
    void curveLocation() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "ab")
                .addEdge(Curve.builder("ab", toRadians(90)), "a", "b")
                .build();
        Curve edge = station.getEdge("ab");

        OrientedLocation ep1 = new OrientedLocation(edge, true, RADIUS * toRadians(30));
        assertThat(ep1.getLocation(), pointCloseTo(33.8, 9.1, 1e-1));

        OrientedLocation ep2 = new OrientedLocation(edge, false, RADIUS * toRadians(30));
        assertThat(ep2.getLocation(), pointCloseTo(58.5, 33.8, 1e-1));
    }

    @Test
    void platformLocation() {
        StationMap station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 100), "ab")
                .addEdge(Platform.builder("ab"), "a", "b")
                .build();
        Platform edge = station.getEdge("ab");

        OrientedLocation ep1 = new OrientedLocation(edge, true, 100);
        assertThat(ep1.getLocation(), pointCloseTo(70.7, 70.7, 1e-1));

        OrientedLocation ep2 = new OrientedLocation(edge, false, 100);
        assertThat(ep2.getLocation(), pointCloseTo(100 - 70.7, 100 - 70.7, 1e-1));
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

        OrientedLocation ep1 = new OrientedLocation(edge, true, 30);

        OrientedLocation rev = ep1.reverse();
        assertThat(rev, hasProperty("edge", equalTo(edge)));
        assertThat(rev, hasProperty("origin", equalTo(b)));
        assertThat(rev, hasProperty("terminal", equalTo(a)));
        assertThat(rev, hasProperty("direct", equalTo(false)));
        assertThat(rev, hasProperty("distance", closeTo(70, 1e-3)));
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

        OrientedLocation ep1 = new OrientedLocation(edge, true, 100);
        assertThat(ep1, hasProperty("direct", equalTo(true)));
        assertThat(ep1, hasProperty("origin", equalTo(a)));
        assertThat(ep1, hasProperty("terminal", equalTo(b)));
        assertThat(ep1.getLocation(), pointCloseTo(70.7, 70.7, 1e-1));

        OrientedLocation ep2 = new OrientedLocation(edge, false, 100);
        assertThat(ep2, hasProperty("direct", equalTo(false)));
        assertThat(ep2, hasProperty("origin", equalTo(b)));
        assertThat(ep2, hasProperty("terminal", equalTo(a)));
        assertThat(ep2.getLocation(), pointCloseTo(100 - 70.7, 100 - 70.7, 1e-1));
    }
}