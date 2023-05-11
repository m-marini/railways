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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mmarini.railways.TestFunctions.pointCloseTo;
import static org.mmarini.railways2.model.geometry.StationMapTest.createStation;
import static org.mmarini.railways2.model.geometry.StationMapTest.stationMap;

class PlatformTest {
    @BeforeAll
    static void beforAll() {
        createStation();
    }

    @Test
    void length() {
        Platform track = stationMap.getEdge("bc");
        assertThat(track.getLength(), closeTo(100, 1e-3));
    }

    @Test
    void location() {
        Platform track = stationMap.getEdge("bc");
        Node b = stationMap.getNode("bNode");
        Node c = stationMap.getNode("cNode");
        assertThat(track.getLocation(c, 40), pointCloseTo(160, 0, 1e-3));
        assertThat(track.getLocation(b, 40), pointCloseTo(140, 0, 1e-3));
    }
}