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
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Signal;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StationStatusLoaderTest {

    public static final String RESOURCE = "/issue-89.yml";
    public static final String TRAIN_ID = "T149";
    public static final String DOUBLE_SLIP_SWITCH_ID = "westCentralCross.s3";
    public static final String SIGNAL_ID = "platforms.5.signalw";
    public static final String SIGNAL_LOCK_EDGE = "platforms.5.trackw";

    @Test
    void fromJson() throws IOException {
        // Given ...
        JsonNode stationJson = Utils.fromResource("/stations/downville.station.yml");
        StationDef stationDef = StationDef.create(stationJson, Locator.root());
        JsonNode json = Utils.fromResource(RESOURCE);

        // When ...
        StationStatus status = StationStatusLoader.fromJson(json, Locator.root(), stationDef);

        // Then ...
        assertNotNull(status);
        DoubleSlipSwitch s3 = status.getRoute(DOUBLE_SLIP_SWITCH_ID);
        assertTrue(s3.isThrough());

        Signal signal = status.getRoute(SIGNAL_ID);
        Edge edge = status.getStationMap().getEdge(SIGNAL_LOCK_EDGE);
        assertTrue(signal.isLocked(new Direction(edge, signal.getNodes().get(0))));

        Optional<Train> trainOpt = status.getTrain(TRAIN_ID);
        assertTrue(trainOpt.isPresent());
    }

    @Test
    void toggle() throws IOException {
        // Given ...
        JsonNode stationJson = Utils.fromResource("/stations/downville.station.yml");
        StationDef stationDef = StationDef.create(stationJson, Locator.root());
        JsonNode json = Utils.fromResource(RESOURCE);
        StationStatus status = StationStatusLoader.fromJson(json, Locator.root(), stationDef);

        // When ...
        // DoubleSlipSwitch westCentralCross.s3
        StationStatus status1 = status.toggleDoubleSlipSwitch(DOUBLE_SLIP_SWITCH_ID);

        // Then ...
        DoubleSlipSwitch s3 = status1.getRoute(DOUBLE_SLIP_SWITCH_ID);
        assertFalse(s3.isThrough());
    }
}