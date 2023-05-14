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

import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.Train;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Signal;
import org.mmarini.railways2.model.routes.Switch;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

import static java.lang.Math.toRadians;

class MapPanelTest {

    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
    private static final double LENGTH = 500;

    public static void main(String[] args) {
        new MapPanelTest().run();
    }

    private final MapPanel panel;
    private final JFrame frame;
    private final StationStatus status;

    public MapPanelTest() {
        this.frame = new JFrame(getClass().getSimpleName());
        this.panel = new MapPanel();
        this.status = createStationStatus();
    }

    /*
     * Entry(a) --curve-- Signal(b) --platform-- Signal(c) --track-- Switch(d) -- track -- Exit(e)
     *                                                                         -- curve -- Exit(f)
     */
    protected StationStatus createStationStatus() {
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(0, LENGTH), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc", "cd")
                .addNode("d", new Point2D.Double(2 * LENGTH + 1, 0), "cd", "de", "df")
                .addNode("e", new Point2D.Double(3 * LENGTH + 1, 0), "de")
                .addNode("f", new Point2D.Double(3 * LENGTH + 1, -LENGTH), "df")
                .addEdge(Curve.builder("ab", toRadians(90)), "a", "b")
                .addEdge(Platform.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .addEdge(Track.builder("de"), "d", "e")
                .addEdge(Curve.builder("df", toRadians(-90)), "d", "f")
                .build();
        StationStatus status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Signal::create, "b")
                .addRoute(Signal::create, "c")
                .addRoute(Switch::through, "d")
                .addRoute(Exit::create, "e")
                .addRoute(Exit::create, "f")
                .build();
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Entry aEntry = status.getRoute("a");
        Exit eExit = status.getRoute("e");
        Train t = Train.create("id", 3, aEntry, eExit)
                .setLocation(EdgeLocation.create(bc, c,  10));
        return status.setTrains(t);
    }

    private void run() {
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setSize(DEFAULT_SIZE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel.paintStation(status);
        frame.setVisible(true);
    }

}