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

import javax.swing.*;
import java.awt.*;

class StationPanelTest {

    private static final Dimension DEFAULT_SIZE = new Dimension(800, 600);

    public static void main(String[] args) {
        /*
        StationStatus status1 = new WithTrain(StationExamples.createSwitchStation())
                .addTrain(10, "a", "e", "ab", "b", 0)
                .addTrain(3, "a", "f", "bc", "c", 0)
                .build();
        StationStatus status2 = new WithTrain(StationExamples.create2CrossExitStation(true))
                .addTrain(10, "a", "e", "ab", "b", 0)
                .addTrain(3, "a", "j", "gh", "h", 0)
                .build();

        StationStatus status3 = new WithTrain(StationExamples.create3Entry2ExitStation())
                .build();
         */
        StationStatus status4 = StationExamples.createDeadEndStation();
        new StationPanelTest(status4).run();
    }

    private final StationPanel panel;
    private final JFrame frame;
    private final StationStatus status;

    public StationPanelTest(StationStatus status) {
        this.frame = new JFrame(getClass().getSimpleName());
        this.panel = new StationPanel();
        this.status = status;

    }

    void run() {
        frame.getContentPane().setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setSize(DEFAULT_SIZE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel.paintStation(status);
        frame.setVisible(true);
    }

}