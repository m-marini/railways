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

package org.mmarini.railways2.model.blocks;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.Curve;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;

import static java.lang.Math.toRadians;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.blocks.Signals.SIGNAL_GAP;

public class DownvilleTest {

    public static final double EPSILON = 1e-3;
    private JsonNode root;

    @Test
    void build() {
        StationDef station = StationDef.create(root, Locator.root());
        StationStatus status = new BlockStationBuilder(station).build();
        assertEquals("Downville", status.getStationMap().getId());
    }

    @Test
    void parseYaml() {
        StationDef station = StationDef.create(root, Locator.root());
        assertEquals("Downville", station.getId());
    }

    @BeforeEach
    void setUp() throws IOException {
        this.root = Utils.fromResource("/stations/downville.station.yml");
    }

    @Test
    void sowerthCurve() {
        // Given ...
        StationDef station = StationDef.create(root, Locator.root());
        BlockStationBuilder builder = new BlockStationBuilder(station);

        // When ...
        OrientedGeometry geo1 = builder.getWorldGeometry("westSignals.w2");
        OrientedGeometry geo2 = builder.getWorldGeometry("sowerthCurves.e2");

        // Then ...
        assertThat(geo1.getPoint(),
                pointCloseTo(-6 * SEGMENT_LENGTH - 2 * SIGNAL_GAP, 3 * TRACK_GAP, EPSILON));
        assertThat(geo2.getPoint(),
                pointCloseTo(-6 * SEGMENT_LENGTH - 2 * SIGNAL_GAP, 3 * TRACK_GAP, EPSILON));
    }

    @Test
    void sowerthCurve1() {
        // Given ...
        StationDef station = StationDef.create(root, Locator.root());
        BlockStationBuilder builder = new BlockStationBuilder(station);
        StationStatus status = builder.build();

        // When ...
        Curve w1e2 = status.getStationMap().getEdge("sowerthCurves.w1.e1");


        // Then ...
        assertThat(w1e2.getAngle(), closeTo(toRadians(-45), toRadians(1)));
    }
}
