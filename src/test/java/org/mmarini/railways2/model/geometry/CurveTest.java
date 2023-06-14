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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mmarini.railways2.model.WithStationMap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.Matchers.locatedAt;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

class CurveTest implements WithStationMap {

    private StationMap stationMap;
    private Curve curve;

    @Test
    void angles270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 270);

        // When .. Then ...
        assertThat(curve.getAngle0(), closeTo(toRadians(-180), 1e-3));
    }

    @Test
    void angles90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When .. Then ...
        assertThat(curve.getAngle0(), closeTo(toRadians(-90), 1e-3));
    }

    @Test
    void angles_270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -270);

        // When .. Then ...
        assertThat(curve.getAngle0(), closeTo(toRadians(-90), 1e-3));
    }

    @Test
    void angles_90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -90);

        // When .. Then ...
        assertThat(curve.getAngle0(), closeTo(toRadians(180), 1e-3));
    }

    @Test
    void bounds180() {
        // Given ...
        given(0, 0, 2 * RADIUS, 0, 180);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 0.1));
        assertThat(bounds.getY(), closeTo(-RADIUS, 0.1));
        assertThat(bounds.getWidth(), closeTo(2 * RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(RADIUS, 0.1));
    }

    @Test
    void bounds270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 270);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 0.1));
        assertThat(bounds.getY(), closeTo(-RADIUS, 0.1));
        assertThat(bounds.getWidth(), closeTo(2 * RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(2 * RADIUS, 0.1));
    }

    @Test
    void bounds90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 0.1));
        assertThat(bounds.getY(), closeTo(0, 0.1));
        assertThat(bounds.getWidth(), closeTo(RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(RADIUS, 0.1));
    }

    @Test
    void bounds_180() {
        // Given ...
        given(0, 0, 2 * RADIUS, 0, -180);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 0.1));
        assertThat(bounds.getY(), closeTo(0, 0.1));
        assertThat(bounds.getWidth(), closeTo(2 * RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(RADIUS, 0.1));
    }

    @Test
    void bounds_270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -270);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(-RADIUS, 0.1));
        assertThat(bounds.getY(), closeTo(0, 0.1));
        assertThat(bounds.getWidth(), closeTo(2 * RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(2 * RADIUS, 0.1));
    }

    @Test
    void bounds_90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -90);

        // When ...
        Rectangle2D bounds = curve.getBounds();

        // Then ...
        assertThat(bounds.getX(), closeTo(0, 0.1));
        assertThat(bounds.getY(), closeTo(0, 0.1));
        assertThat(bounds.getWidth(), closeTo(RADIUS, 0.1));
        assertThat(bounds.getHeight(), closeTo(RADIUS, 0.1));
    }

    @Test
    void center270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 270);

        // When ...
        Point2D center = curve.getCenter();

        // Then ...
        assertThat(center, pointCloseTo(RADIUS, 0, 1e-3));
    }

    @Test
    void center90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ...
        Point2D center = curve.getCenter();

        // Then ...
        assertThat(center, pointCloseTo(0, RADIUS, 1e-3));
    }

    @Test
    void center_270() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -270);

        // When ...
        Point2D center = curve.getCenter();

        // Then ...
        assertThat(center, pointCloseTo(0, RADIUS, 1e-3));
    }

    @Test
    void center_90() {
        // Given ...
        given(0, 0, RADIUS, -RADIUS, -90);

        // When ...
        Point2D center = curve.getCenter();

        // Then ...
        assertThat(center, pointCloseTo(0, -RADIUS, 1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "0,           400,0,-180",
            "0,           0,0,-180",
            "1256.637,    800,0,-180",
            "628.319,     400,400,-180",
            "628.319,     400,390,-180",
            "628.319,     400,410,-180",
            "0,           10,0,-180",
            "1256.637,    790,0,-180",
            "0,           -10,0,-180",
            "1256.637,    810,0,-180",
            "0,           0,-10,-180",
            "1256.637,    800,-10,-180",

            "0,           400,0,180",
            "0,           0,0,180",
            "1256.637,    800,0,180",
            "628.319,     400,-400,180",
            "628.319,     400,-390,180",
            "628.319,     400,-410,180",
            "0,           10,0,180",
            "1256.637,    790,0,180",
            "0,           -10,0,180",
            "1256.637,    810,0,180",
            "0,           0,10,180",
            "1256.637,    800,10,180"
    })
    void getNearestLocation(double distance, double x, double y, int angle) {
        // Given ...
        given(0, 0, 2 * RADIUS, 0, angle);

        Point2D.Double point = new Point2D.Double(x, y);

        // When ...
        EdgeLocation location = curve.getNearestLocation(point);

        // Then ...
        assertThat(location, locatedAt("curve", "a", distance));
    }

    void given(double x0, double y0, double x1, double y1, double angle) {
        this.stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(x0, y0), "curve")
                .addNode("b", new Point2D.Double(x1, y1), "curve")
                .addCurve("curve", toRadians(angle), "a", "b")
                .build();
        this.curve = edge("curve");
    }

    @Test
    void length180() {
        // Given ...
        given(0, 0, 0, 2 * RADIUS, 180);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI, 1e-3));
    }

    @Test
    void length270() {
        // Given ...
        given(0, 0, -RADIUS, RADIUS, 270);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI * 3 / 2, 1e-3));
    }

    @Test
    void length90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI / 2, 1e-3));
    }

    @Test
    void length_180() {
        // Given ...
        given(0, 0, 0, -2 * RADIUS, -180);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI, 1e-3));
    }

    @Test
    void length_270() {
        // Given ...
        given(0, 0, -RADIUS, -RADIUS, -270);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI * 3 / 2, 1e-3));
    }

    @Test
    void length_90() {
        // Given ...
        given(0, 0, RADIUS, -RADIUS, -90);

        // When ...
        double len = curve.getLength();

        // Then ...
        assertThat(len, closeTo(RADIUS * PI / 2, 1e-3));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-1,1,-120,-270",
            "-1,1,-120,-240",
            "-1,0.866,-120,-210",
            "-1,0.5,-120,-180",
            "-1,-0.5,-120,-120",
            "-1,-0.5,-120,-60",
            "-0.866,-0.5,-120,-30",

            "-1,1,-150,340",
            "-1,1,-150,330",
            "-0.866,1,-150,300",
            "-0.866,1,-150,150",
            "-0.866,0.866,-150,120",
            "-0.866,-0.5,-150,30",

            "-1,1,-30,-360",
            "-1,1,-30,-330",
            "-1,0.8666,-30,-300",
            "-1,0.8666,-30,-150",
            "-1,0.8666,-30,-150",
            "-0.866,0.8666,-30,-120",
            "-0.5,0.8666,-30,-90",
            "0.5,0.8666,-30,-30",

            "-1,1,-60,270",
            "-1,1,-60,240",
            "-0.866,1,-60,210",
            "-0.5,1,-60,180",
            "0.5,1,-60,120",
            "0.5,1,-60,60",
            "0.5,0.8666,-60,30",

            "-1,1,120,-330",
            "-1,1,120,-300",
            "-0.866,1,120,-270",
            "-0.5,1,120,-240",
            "-0.5,1,120,-120",
            "-0.5,0.866,120,-90",
            "-0.5,0.5,120,-60",
            "-0.5,0,120,-30",

            "-1,1,30,-240",
            "-1,1,30,-210",
            "-0.866,1,30,-180",
            "0,1,30,-120",
            "-0.5,1,30,-150",
            "0.5,1,30,-90",
            "0.866,1,30,-60",
            "0.866,1,30,-30",

            "-1,1,120,270",
            "-1,1,120,240",
            "-1,0.866,120,210",
            "-1,0.5,120,180",
            "-1,0,120,150",
            "-1,-0.5,120,120",
            "-1,-0.5,120,60",
            "-0.866,-0.5,120,30",

            "-1,1,30,330",
            "-1,0.866,30,270",
            "-1,0.866,30,150",
            "-0.5,0.866,30,90",
            "0,0.866,30,60",
            "0.5,0.866,30,30",

            "-1,1,0,210",
            "-0.5,1,0,120",
            "0,1,0,90",
            "0.5,1,0,60",
    })
    void limitCos(double expMin, double expMax, double a0, double da) {
        double[] limits = Curve.limitCos(toRadians(a0), toRadians(da));
        assertThat(limits[0], closeTo(expMin, 1e-3));
        assertThat(limits[1], closeTo(expMax, 1e-3));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-1,1,-120,-340",
            "-1,1,-120,-330",
            "-0.866,1,-120,-300",
            "-0.866,1,-120,-150",
            "-0.866,0.866,-120,-120",
            "-0.866,0.5,-120,-90",
            "-0.866,-0.5,-120,-30",

            "-1,1,-150,270",
            "-1,1,-150,240",
            "-1,0.866,-150,210",
            "-1,0.5,-150,180",
            "-1,-0.5,-150,120",
            "-1,-0.5,-150,60",
            "-0.866,-0.5,-150,30",

            "-1,1,-60,340",
            "-1,1,-60,330",
            "-0.866,1,-60,300",
            "-0.866,1,-60,150",
            "-0.866,0.866,-60,120",
            "-0.866,0.5,-60,90",
            "-0.866,-0.5,-60,30",

            "-1,1,60,-340",
            "-1,1,60,-330",
            "-1,0.866,60,-300",
            "-1,0.866,60,-150",
            "-0.866,0.866,60,-120",
            "-0.5,0.866,60,-90",
            "0.5,0.866,60,-30",

            "-1,1,30,270",
            "-1,1,30,240",
            "-0.866,1,30,210",
            "-0.5,1,30,180",
            "0.5,1,30,120",
            "0.5,1,30,60",
            "0.5,0.866,30,30",

            "-1,1,150,-250",
            "-1,1,150,-240",
            "-0.866,1,150,-210",
            "-0.5,1,150,-180",
            "0.5,1,150,-120",
            "0.5,1,150,-60",
            "0.5,0.866,150,-30",

            "-1,1,120,340",
            "-1,1,120,330",
            "-1,0.866,120,300",
            "-1,0.866,120,150",
            "-0.866,0.866,120,120",
            "-0.5,0.866,120,90",
            "0.5,0.866,120,30",
    })
    void limitSin(double expMin, double expMax, double a0, double da) {
        double[] limits = Curve.limitSin(toRadians(a0), toRadians(da));
        assertThat(limits[0], closeTo(expMin, 1e-3));
        assertThat(limits[1], closeTo(expMax, 1e-3));
    }

    @Test
    void location90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ... Then ...
        assertThat(curve.getLocation(location("curve", "b", toRadians(60) * RADIUS)),
                pointCloseTo(RADIUS * sin(toRadians(30)), RADIUS - RADIUS * cos(toRadians(30)), 1e-3));

        assertThat(curve.getLocation(location("curve", "a", toRadians(60) * RADIUS)),
                pointCloseTo(RADIUS * sin(toRadians(60)), RADIUS - RADIUS * cos(toRadians(60)), 1e-3));
    }

    /*
     *     b
     *    /
     * a--
     */
    @Test
    void locationAngle90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ...
        double angleB60 = toDegrees(curve.getAngle(location("curve", "b", toRadians(60) * RADIUS)));
        double angleA60 = toDegrees(curve.getAngle(location("curve", "a", toRadians(60) * RADIUS)));

        // Then ...
        assertThat(angleB60, closeTo(-60, 0.1));
        assertThat(angleA60, closeTo(-90 + 60, 0.1));
    }


    /*
     *     --b
     *   /
     * a
     */
    @Test
    void locationAngle_90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -90);

        // When ...
        double angleB60 = toDegrees(curve.getAngle(location("curve", "b", toRadians(60) * RADIUS)));
        double angleA60 = toDegrees(curve.getAngle(location("curve", "a", toRadians(60) * RADIUS)));

        // Then ...
        assertThat(angleB60, closeTo(90 + 60, 0.1));
        assertThat(angleA60, closeTo(180 - 60, 0.1));
    }

    /*
     *     --b
     *   /
     * a
     */
    @Test
    void location_90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -90);

        // When ...
        Point2D locationB60 = curve.getLocation(location("curve", "b", toRadians(60) * RADIUS));
        Point2D locationA60 = curve.getLocation(location("curve", "a", toRadians(60) * RADIUS));

        // Then ...
        assertThat(locationB60, pointCloseTo(RADIUS - RADIUS * cos(toRadians(30)), RADIUS * sin(toRadians(30)), 1e-3));
        assertThat(locationA60, pointCloseTo(RADIUS - RADIUS * cos(toRadians(60)), RADIUS * sin(toRadians(60)), 1e-3));
    }

    @Test
    void orientation90() {
        // Given ...
        given(0, 0, RADIUS, -RADIUS, 90);

        // When ...
        double orientation1 = curve.getOrientation(location("curve", "a", toRadians(89) * RADIUS));
        double orientation2 = curve.getOrientation(location("curve", "a", toRadians(45) * RADIUS));
        double orientation3 = curve.getOrientation(location("curve", "a", toRadians(1) * RADIUS));
        double orientation4 = curve.getOrientation(location("curve", "b", toRadians(89) * RADIUS));
        double orientation5 = curve.getOrientation(location("curve", "b", toRadians(45) * RADIUS));
        double orientation6 = curve.getOrientation(location("curve", "b", toRadians(1) * RADIUS));

        // Then ...
        assertThat(toDegrees(orientation1), closeTo(179, 1));
        assertThat(toDegrees(orientation2), closeTo(135, 1));
        assertThat(toDegrees(orientation3), closeTo(91, 1));
        assertThat(toDegrees(orientation4), closeTo(-89, 1));
        assertThat(toDegrees(orientation5), closeTo(-45, 1));
        assertThat(toDegrees(orientation6), closeTo(-1, 1));
    }

    @Test
    void orientation_90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, -90);

        // When ...
        double orientation1 = curve.getOrientation(location("curve", "a", toRadians(89) * RADIUS));
        double orientation2 = curve.getOrientation(location("curve", "a", toRadians(45) * RADIUS));
        double orientation3 = curve.getOrientation(location("curve", "a", toRadians(1) * RADIUS));
        double orientation4 = curve.getOrientation(location("curve", "b", toRadians(89) * RADIUS));
        double orientation5 = curve.getOrientation(location("curve", "b", toRadians(45) * RADIUS));
        double orientation6 = curve.getOrientation(location("curve", "b", toRadians(1) * RADIUS));

        // Then ...
        assertThat(toDegrees(orientation1), closeTo(-179, 1));
        assertThat(toDegrees(orientation2), closeTo(-135, 1));
        assertThat(toDegrees(orientation3), closeTo(-91, 1));
        assertThat(toDegrees(orientation4), closeTo(89, 1));
        assertThat(toDegrees(orientation5), closeTo(45, 1));
        assertThat(toDegrees(orientation6), closeTo(1, 1));
    }

    @Test
    void radius90() {
        // Given ...
        given(0, 0, RADIUS, RADIUS, 90);

        // When ... Then ...
        assertThat(curve.getRadius(), closeTo(RADIUS, 1e-3));
    }

    @Override
    public StationMap stationMap() {
        return this.stationMap;
    }
}