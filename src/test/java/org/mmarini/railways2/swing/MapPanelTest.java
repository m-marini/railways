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
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

class MapPanelTest {

    public static final double GAME_DURATION = 300d;
    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    public static void main(String[] args) throws IOException {
        new MapPanelTest(
                /*
                new WithTrain(StationExamples.createSwitchStation())
                        .addTrain(3, "a", "e", "ab", "b", 100)
                        .addTrain(3, "a", "e", "bc", "c", 10)
                        .build()
*/
                                /*
                new WithTrain(StationExamples.create2CrossExitStation(true))
                        .addTrain(10, "a", "e", "ab", "b", 0)
                        .addTrain(3, "a", "j", "gh", "h", 0)
                        .build()
                 */

                new BlockStationBuilder(StationDef.create(
                        Utils.fromResource("/stations/downville.station.yml"), Locator.root()), GAME_DURATION, null).build()
        ).run();
    }

    private final MapPanel panel;
    private final JFrame frame;
    private final StationStatus status;

    public MapPanelTest(StationStatus status) {
        this.frame = new JFrame(getClass().getSimpleName());
        this.panel = new MapPanel();
        this.status = status;
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