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
import org.mmarini.railways2.model.WithStationStatusTest;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JackvilleTest extends WithStationStatusTest {

    public static final double EPSILON = 1e-3;
    public static final double GAME_DURATION = 300d;
    public static final double FREQUENCY = 0.1;
    private JsonNode root;

    @Test
    void build() {
        StationDef station = StationDef.create(root, Locator.root());
        this.status = new BlockBuilder(station).buildStatus(GAME_DURATION, FREQUENCY, null, null);
        assertEquals("jackville", status.getStationMap().getId());
    }

    @Test
    void parseYaml() {
        StationDef station = StationDef.create(root, Locator.root());
        assertEquals("jackville", station.getId());
    }

    @BeforeEach
    void setUp() throws IOException {
        this.root = Utils.fromResource("/stations/jackville.station.yml");
    }
}
