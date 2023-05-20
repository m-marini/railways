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
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.geometry.Track;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;

class TrainTest {
    StationStatus status;
    StationMap stationMap;

    /**
     * Entry(a) --ab-- Exit(b)
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(200, 0), "ab")
                .addEdge(Track.builder("ab"), "a", "b")
                .build();
        status = new StationStatus.Builder(stationMap, 1)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .build();
    }

    @Test
    void getLength() {
        Entry a = status.getRoute("a");
        Exit b = status.getRoute("b");
        Train train = Train.create("t1", 2, a, b);
        assertEquals(2 * COACH_LENGTH, train.getLength());
    }
}