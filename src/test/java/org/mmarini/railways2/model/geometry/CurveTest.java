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

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mmarini.railways.TestFunctions.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

class CurveTest {
    @Test
    void angles270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(270)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getAngle0(), closeTo(toRadians(-180), 1e-3));
    }

    @Test
    void angles90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(90)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getAngle0(), closeTo(toRadians(-90), 1e-3));
    }

    @Test
    void angles_270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(-270)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getAngle0(), closeTo(toRadians(-90), 1e-3));
    }

    @Test
    void angles_90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(-90)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getAngle0(), closeTo(toRadians(180), 1e-3));
    }

    @Test
    void center270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(270)), "a", "b")
                .build()
                .getEdge("curve");

        Point2D center = curve.getCenter();
        assertThat(center, pointCloseTo(RADIUS, 0, 1e-3));
    }

    @Test
    void center90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(90)), "a", "b")
                .build()
                .getEdge("curve");

        Point2D center = curve.getCenter();
        assertThat(center, pointCloseTo(0, RADIUS, 1e-3));
    }

    @Test
    void center_270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(-270)), "a", "b")
                .build()
                .getEdge("curve");

        Point2D center = curve.getCenter();
        assertThat(center, pointCloseTo(0, RADIUS, 1e-3));
    }

    @Test
    void center_90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, -RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(-90)), "a", "b")
                .build()
                .getEdge("curve");

        Point2D center = curve.getCenter();
        assertThat(center, pointCloseTo(0, -RADIUS, 1e-3));
    }

    @Test
    void length180() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(0, 2 * RADIUS), "curve")
                .addEdge(Curve.builder("curve", PI), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();

        assertThat(len, closeTo(RADIUS * PI, 1e-3));
    }

    @Test
    void length270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(-RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", PI * 3 / 2), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();

        assertThat(len, closeTo(RADIUS * PI * 3 / 2, 1e-3));
    }

    @Test
    void length90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", PI / 2), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();
        assertThat(len, closeTo(RADIUS * PI / 2, 1e-3));
    }

    @Test
    void length_180() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(0, -2 * RADIUS), "curve")
                .addEdge(Curve.builder("curve", -PI), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();

        assertThat(len, closeTo(RADIUS * PI, 1e-3));
    }

    @Test
    void length_270() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(-RADIUS, -RADIUS), "curve")
                .addEdge(Curve.builder("curve", -PI * 3 / 2), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();

        assertThat(len, closeTo(RADIUS * PI * 3 / 2, 1e-3));
    }

    @Test
    void length_90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, -RADIUS), "curve")
                .addEdge(Curve.builder("curve", -PI / 2), "a", "b")
                .build()
                .getEdge("curve");

        double len = curve.getLength();

        assertThat(len, closeTo(RADIUS * PI / 2, 1e-3));
    }

    @Test
    void location90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(90)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getLocation(true, toRadians(30) * RADIUS),
                pointCloseTo(RADIUS * sin(toRadians(30)), RADIUS - RADIUS * cos(toRadians(30)), 1e-3));

        assertThat(curve.getLocation(false, toRadians(30) * RADIUS),
                pointCloseTo(RADIUS * sin(toRadians(60)), RADIUS - RADIUS * cos(toRadians(60)), 1e-3));
    }

    @Test
    void location_90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(-90)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getLocation(true, toRadians(30) * RADIUS),
                pointCloseTo(RADIUS - RADIUS * cos(toRadians(30)), RADIUS * sin(toRadians(30)), 1e-3));

        assertThat(curve.getLocation(false, toRadians(30) * RADIUS),
                pointCloseTo(RADIUS - RADIUS * cos(toRadians(60)), RADIUS * sin(toRadians(60)), 1e-3));
    }

    @Test
    void radius90() {
        Curve curve = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "curve")
                .addNode("b", new Point2D.Double(RADIUS, RADIUS), "curve")
                .addEdge(Curve.builder("curve", toRadians(90)), "a", "b")
                .build()
                .getEdge("curve");

        assertThat(curve.getRadius(), closeTo(RADIUS, 1e-3));
    }
}