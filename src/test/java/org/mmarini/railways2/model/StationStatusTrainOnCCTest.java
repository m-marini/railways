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

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.railways2.swing.WithTrain;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways2.model.RailwayConstants.MAX_SPEED;
import static org.mmarini.yaml.Utils.fromResource;

class StationStatusTrainOnCCTest extends WithStationStatusTest {

    public static final int SEED = 1234;

    @BeforeEach
    void beforeEach() throws IOException {
        JsonNode root = fromResource("/stations/downville.station.yml");
        StationDef station = StationDef.create(root, Locator.root());
        status = new BlockStationBuilder(station, null).build();
        status = new WithTrain(status)
                .addTrain(10, "norton.in", "norton.out", "westCentralCross.s5.s9", "westCentralCross.s9", 22.3)
                .addTrain(10, "sowerth.in", "norton.out", "westCentralTrack6.w1.e1", "westCentralTrack6.e1", 92.1)
                .build();
    }

    @Test
    void isNextTracksClear() {
        // Given ...
        // When ... Then ...
        assertTrue(status.isNextSignalClear(train("TT0")));
    }

    @Test
    void tick() {
        // Given ...
        // When ...
        StationStatus status1 = status.tick(0.1, new Random(SEED));

        // Than ...
        Train t0 = train("TT0");
        assertEquals(t0.getSpeed(), MAX_SPEED);
    }
}