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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.lang.Math.sqrt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways2.Matchers.pointCloseTo;

class PlatformTest {
    public static final int LENGTH = 100;
    private StationMap stationMap;
    private Platform platform;

    @BeforeEach
    void beforeEach() {
        this.stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, 0), "ab")
                .addNode("b", new Point2D.Double(LENGTH, LENGTH), "ab")
                .addPlatform("ab", "a", "b")
                .build();
        this.platform = stationMap.getEdge("ab");
    }

    @Test
    void getBounds() {
        // Given ...

        // When ...
        Rectangle2D bounds = platform.getBounds();

        // Then ...
        assertEquals(new Rectangle2D.Double(0, 0, LENGTH, LENGTH), bounds);
    }

    @Test
    void length() {
        assertThat(platform.getLength(), closeTo(LENGTH * sqrt(2), 1e-3));
    }

    @Test
    void location() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");

        // When ...
        Point2D locationA60 = platform.getLocation(EdgeLocation.create(platform, a, 60 * sqrt(2)));
        Point2D locationB60 = platform.getLocation(EdgeLocation.create(platform, b, 60 * sqrt(2)));

        // Then ...
        assertThat(locationA60, pointCloseTo(60, 60, 1e-3));
        assertThat(locationB60, pointCloseTo(40, 40, 1e-3));
    }
}