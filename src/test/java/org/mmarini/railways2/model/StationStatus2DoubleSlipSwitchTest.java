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

import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class StationStatus2DoubleSlipSwitchTest extends WithStationStatusTest {

    public static final double GAME_DURATION = 300d;
    public static final int GAP = 10;
    private static final double LENGTH = 100;
    private Subscriber<SoundEvent> events;

    /**
     * StationDef map
     * <pre>
     *    a           b           c           d           e           f
     *    |           |           |           |           |           |
     *  0 a1 --a1b1-- b1 /-b1c2-\
     * -1                --b1c1
     * -2 a2 --a2b2-- b2 --b2c2-- c2 -------------c2f2--------------- f2
     *                     b1c1--
     * -3                \-b2c1-\ c1 --c1d1-- d1 /-d1e3
     *                                           --d1e1
     *                                             d1e3-\
     * -4 a3 -------------a3d3--------------- d3 --d3e3-- e3 --e3f3-- f3
     *                                           \-d3e1
     *                                             d1e1--
     * -5                                          d3e1-\ e1 --e1f1-- f1
     * </pre>
     */
    void createStatus(boolean through12, boolean through23) {
        this.events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
                .addNode("a1", new Point2D.Double(), "a1b1")
                .addNode("b1", new Point2D.Double(LENGTH, 0), "a1b1", "b1c1", "b1c2")
                .addNode("c1", new Point2D.Double(LENGTH * 2, -GAP * 3), "c1d1", "b1c1", "b2c1")
                .addNode("d1", new Point2D.Double(LENGTH * 3, -GAP * 3), "c1d1", "d1e1", "d1e3")
                .addNode("e1", new Point2D.Double(LENGTH * 4, -GAP * 5), "e1f1", "d1e1", "d3e1")
                .addNode("f1", new Point2D.Double(LENGTH * 5, -GAP * 5), "e1f1")
                .addNode("a2", new Point2D.Double(0, -GAP * 2), "a2b2")
                .addNode("b2", new Point2D.Double(LENGTH, -GAP * 2), "a2b2", "b2c2", "b2c1")
                .addNode("c2", new Point2D.Double(LENGTH * 2, -GAP * 2), "c2f2", "b2c2", "b1c2")
                .addNode("f2", new Point2D.Double(LENGTH * 5, -GAP * 2), "c2f2")
                .addNode("a3", new Point2D.Double(0, -GAP * 4), "a3d3")
                .addNode("d3", new Point2D.Double(LENGTH * 3, -GAP * 4), "a3d3", "d3e3", "d3e1")
                .addNode("e3", new Point2D.Double(LENGTH * 4, -GAP * 4), "e3f3", "d3e3", "d1e3")
                .addNode("f3", new Point2D.Double(LENGTH * 5, -GAP * 42), "e3f3")
                .addTrack("a1b1", "a1", "b1")
                .addTrack("b1c1", "b1", "c1")
                .addTrack("b1c2", "b1", "c2")
                .addTrack("c1d1", "c1", "d1")
                .addTrack("d1e1", "d1", "e1")
                .addTrack("d1e3", "d1", "e3")
                .addTrack("e1f1", "e1", "f1")

                .addTrack("a2b2", "a2", "b2")
                .addTrack("b2c1", "b2", "c1")
                .addTrack("b2c2", "b2", "c2")
                .addTrack("c2f2", "c2", "f2")

                .addTrack("a3d3", "a3", "d3")
                .addTrack("d3e1", "d3", "e1")
                .addTrack("d3e3", "d3", "e3")
                .addTrack("e3f3", "e3", "f3")

                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a1")
                .addRoute(Entry::create, "a2")
                .addRoute(Entry::create, "a3")
                .addRoute(DoubleSlipSwitch.create(through12), "b1", "c1", "b2", "c2")
                .addRoute(DoubleSlipSwitch.create(through23), "d1", "e1", "d3", "e3")
                .addRoute(Exit::create, "f1")
                .addRoute(Exit::create, "f2")
                .addRoute(Exit::create, "f3")
                .build();
    }

    @Test
    void toggleToDiverging() {
        // Given ...
        createStatus(true, true);

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b1");

        // Then ...
        assertFalse(status1.<DoubleSlipSwitch>getRoute("b1").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleToThrough() {
        // Given ...
        createStatus(false, true);

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b1");

        // Then ...
        assertTrue(status1.<DoubleSlipSwitch>getRoute("b1").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleCrossing() {
        // Given ...
        createStatus(true, true);
        status = withTrain()
                .addTrain(3, "a1", "f1", "a1b1", "b1", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b1");

        // Then ...
        assertSame(status, status1);
    }

    @Test
    void toggleTrainInSection() {
        // Given ...
        createStatus(true, true);
        status = withTrain()
                .addTrain(3, "a1", "f1", "a2b2", "b2", LENGTH)
                .addTrain(3, "a1", "f1", "a3d3", "d3", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b1");

        // Then ...
        assertSame(status, status1);
    }
}