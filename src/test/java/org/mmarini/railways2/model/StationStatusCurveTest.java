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

package org.mmarini.railways2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;

import java.awt.geom.Point2D;
import java.util.Optional;

import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mmarini.railways.Matchers.optionalOf;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.MathUtils.RAD_180;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;

class StationStatusCurveTest {

    public static final double LENGTH = 100;
    private StationMap stationMap;
    private StationStatus status;
    private Node a;
    private Node b;
    private Curve ab;

    /**
     * Station map
     * <pre>
     *            Curve(ab)
     *          /          \
     * Entry(a)             Exit(b)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        this.stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(2 * RADIUS, 0), "ab")
                .addCurve("ab", RAD_180, "a", "b")
                .build();
        this.a = stationMap.getNode("a");
        this.b = stationMap.getNode("b");
        this.ab = stationMap.getEdge("ab");
        this.status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .build();
    }

    @Test
    void computeCoachLocation() {
        // Given ...

        // When ...
        Optional<Tuple2<Point2D, Double>> loc1 = status.computeCoachLocation(
                EdgeLocation.create(ab, a, RADIUS * toRadians(90) + COACH_LENGTH / 2));
        Optional<Tuple2<Point2D, Double>> loc2 = status.computeCoachLocation(
                EdgeLocation.create(ab, a, RADIUS * toRadians(45) + COACH_LENGTH / 2));
        Optional<Tuple2<Point2D, Double>> loc3 = status.computeCoachLocation(
                EdgeLocation.create(ab, a, RADIUS * toRadians(135) + COACH_LENGTH / 2));

        // Then ...
        assertThat(loc1, optionalOf(
                hasProperty("v2", closeTo(0, toRadians(1)))
        ));
        assertThat(loc1, optionalOf(
                hasProperty("v1", pointCloseTo(RADIUS, RADIUS, 0.9))
        ));
        assertThat(loc2, optionalOf(
                hasProperty("v2", closeTo(toRadians(45), toRadians(1)))
        ));
        assertThat(loc2, optionalOf(
                hasProperty("v1", pointCloseTo(RADIUS * (1 - sqrt(2) / 2), RADIUS * sqrt(2) / 2, 0.9))
        ));
        assertThat(loc3, optionalOf(
                hasProperty("v2", closeTo(toRadians(-45), toRadians(1)))
        ));
        assertThat(loc3, optionalOf(
                hasProperty("v1", pointCloseTo(RADIUS * (1 + sqrt(2) / 2), RADIUS * sqrt(2) / 2, 0.9))
        ));
    }
}