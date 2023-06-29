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

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways2.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.Matchers.locatedAt;

class TrackTest implements WithStationMap {
    public static final int LENGTH = 100;
    private StationMap stationMap;
    private Track track;

    @BeforeEach
    void beforeEach() {
        this.stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, 0), "ab")
                .addNode("b", new Point2D.Double(LENGTH, LENGTH), "ab")
                .addTrack("ab", "a", "b")
                .build();
        this.track = stationMap.getEdge("ab");
    }

    @Test
    void getBounds() {
        // Given ...

        // When ...
        Rectangle2D bounds = track.getBounds();

        // Then ...
        assertEquals(new Rectangle2D.Double(0, 0, LENGTH, LENGTH), bounds);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0,0",
            "0, 100,100",
            "0, 10,10",
            "70.711, 0,100",
            "14.142, -10,-10",
            "14.142, 110,110",
            "141.421, 200,0",
            "100, 200,100"
    })
    void getDistance(double expDistance, double x, double y) {
        // Given ...
        Point2D.Double point = new Point2D.Double(x, y);

        // When ...
        double distance = track.getDistance(point);

        // Then ...
        assertThat(distance, closeTo(expDistance, 1e-3));
    }

    @ParameterizedTest
    @CsvSource({
            "ab,a,0, 0,0",
            "ab,a,141.421,  100,100",
            "ab,a,14.142,   10,10",
            "ab,a,70.711,   0,100",
            "ab,a,0,        -10,-10",
            "ab,a,141.421,  200,200"
    })
    void getNearestLocation(String edge, String to, double distance, double x, double y) {
        // Given ...
        Point2D.Double point = new Point2D.Double(x, y);

        // When ...
        EdgeLocation location = track.getNearestLocation(point);

        // Then ...
        assertThat(location, locatedAt(edge, to, distance));
    }

    @Test
    void length() {
        assertThat(track.getLength(), closeTo(LENGTH * sqrt(2), 1e-3));
    }

    @Test
    void location() {
        // Given ...
        Node a = node("a");
        Node b = node("b");

        // When ...
        Point2D locationA60 = track.getLocation(EdgeLocation.create(track, a, 60 * sqrt(2)));
        Point2D locationB60 = track.getLocation(EdgeLocation.create(track, b, 60 * sqrt(2)));

        // Then ...
        assertThat(locationA60, pointCloseTo(60, 60, 1e-3));
        assertThat(locationB60, pointCloseTo(40, 40, 1e-3));
    }

    @Override
    public StationMap stationMap() {
        return stationMap;
    }
}