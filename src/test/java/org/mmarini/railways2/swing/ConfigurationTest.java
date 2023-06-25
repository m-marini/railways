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

package org.mmarini.railways2.swing;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.ExtendedPerformance;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.yaml.Utils.fromText;

class ConfigurationTest {

    private static final String JSON = text(
            "---",
            "version: \"1.0\"",
            "userPreferences:",
            "  simulationSpeed: 1.0",
            "  mute: false",
            "  gain: -3.0",
            "  lookAndFeelClass: \"javax.swing.plaf.metal.MetalLookAndFeel\"",
            "hallOfFame:",
            "- player: \"player\"",
            "  stationId: \"Downville\"",
            "  timestamp: 1687525433248",
            "  gameDuration: 10.0",
            "  incomingTrainNumber: 4",
            "  rightOutgoingTrainNumber: 0",
            "  wrongOutgoingTrainNumber: 0",
            "  trainStopNumber: 0",
            "  totalTrainTime: 0.0",
            "  traveledDistance: 0.0",
            "  elapsedTime: 10.000000000000007",
            "  trainWaitingTime: 0.0");

    @Test
    void fromJson() throws IOException {
        // Given ...
        JsonNode json = fromText(JSON);

        // When ...
        Configuration conf = Configuration.fromJson(json, Locator.root());

        // Then ...
        UserPreferences userPreferences = conf.getUserPreferences();
        assertEquals(1d, userPreferences.getSimulationSpeed());
        assertEquals(-3d, userPreferences.getGain());
        assertFalse(userPreferences.isMute());
        assertEquals("javax.swing.plaf.metal.MetalLookAndFeel", userPreferences.getLookAndFeelClass());

        assertThat(conf.getHallOfFame(), hasSize(1));
        ExtendedPerformance perf = conf.getHallOfFame().get(0);
        assertEquals("player", perf.getPlayer());
        assertEquals("Downville", perf.getStationId());
        assertEquals(1687525433248L, perf.getTimestamp());
        assertEquals(10d, perf.getGameDuration());
        assertEquals(0d, perf.getTotalTrainTime());
        assertEquals(0d, perf.getTraveledDistance());
        assertEquals(0d, perf.getTrainWaitingTime());
        assertEquals(10.000000000000007, perf.getElapsedTime());
        assertEquals(4, perf.getIncomingTrainNumber());
        assertEquals(0, perf.getRightOutgoingTrainNumber());
        assertEquals(0, perf.getWrongOutgoingTrainNumber());
        assertEquals(0, perf.getTrainStopNumber());
    }
}