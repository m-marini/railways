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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationContextTest {
    static StationMap stationMap;
    static StationStatus status;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab-- Signal(b) --bc-- Signal(c) --cd-- Exit(dc)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(200, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(300, 0), "cd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap,1 )
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Signal::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void lockSignals() {
        // Given ...
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");
        SimulationContext context = new SimulationContext(status, 0.1);

        // When ...
        context.lockSignals(new Direction(bc, c));

        // Then
        StationStatus status1 = context.getStatus();
        Signal bSignal = status1.getRoute("b");
        assertTrue(bSignal.isLocked(new Direction(ab, b)));
        assertFalse(bSignal.isLocked(new Direction(bc, b)));
        Signal cSignal = status1.getRoute("c");
        assertTrue(cSignal.isLocked(new Direction(cd, c)));
        assertFalse(cSignal.isLocked(new Direction(bc, c)));
    }
}