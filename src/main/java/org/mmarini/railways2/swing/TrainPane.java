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

/**
 * Shows the train information in a table
 */
public class TrainPane extends JPanel {
    public static final Font DEFAULT_FONT = Font.decode("Dialog Plain 10");
    private final TrainTableModel model;

    /**
     * Creates the train panel
     */
    public TrainPane() {
        model = new TrainTableModel();
        JTable table = new JTable(model);

        setLayout(new BorderLayout());
        int h = table.getCellRenderer(0, 0)
                .getTableCellRendererComponent(table, "", false, false, 0, 0)
                .getSize().height;
        table.setPreferredScrollableViewportSize(new Dimension(100, h * 11));
        table.setFont(DEFAULT_FONT);
        JScrollPane pane = new JScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }

    /**
     * Sets the station status
     *
     * @param status the station status
     */
    public void setStatus(StationStatus status) {
        model.setStatus(status);
    }
}