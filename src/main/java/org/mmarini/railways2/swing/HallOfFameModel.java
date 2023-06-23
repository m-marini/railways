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

import org.mmarini.Utils;
import org.mmarini.railways2.model.ExtendedPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.util.List;

import static org.mmarini.railways2.model.RailwayConstants.SPH;
import static org.mmarini.railways2.model.RailwayConstants.SPM;

/**
 * Models the table of the hall of fame
 */
public class HallOfFameModel extends AbstractTableModel {

    public static final String UNKNOWN = "???"; //$NON-NLS-1$
    private static final Logger logger = LoggerFactory.getLogger(HallOfFameModel.class);

    private Object[][] entries;

    /**
     * Creates the hall of fame table model
     */
    public HallOfFameModel() {
        logger.atDebug().log("Created");
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
            case 5:
                return Integer.class;
            case 2:
                return Date.class;
            case 3:
            case 4:
            case 6:
                return Double.class;
        }
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return 8;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("HallOfFameModel.column.name"); //$NON-NLS-1$
            case 1:
                return Messages.getString("HallOfFameModel.player.name"); //$NON-NLS-1$
            case 2:
                return Messages.getString("HallOfFameModel.date.name"); //$NON-NLS-1$
            case 3:
                return Messages.getString("HallOfFameModel.performance.name"); //$NON-NLS-1$
            case 4:
                return Messages.getString("HallOfFameModel.gameLength.name"); //$NON-NLS-1$
            case 5:
                return Messages.getString("HallOfFameModel.trains.name"); //$NON-NLS-1$
            case 6:
                return Messages.getString("HallOfFameModel.frequency.name"); //$NON-NLS-1$
            case 7:
                return Messages.getString("HallOfFameModel.station.name"); //$NON-NLS-1$
        }
        return UNKNOWN;
    }

    @Override
    public int getRowCount() {
        return entries != null ? entries.length : 0;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return entries[row][col];
    }

    /**
     * Sets the hall of fame data
     *
     * @param hof the hall of fame
     */
    public void setHallOfFame(List<ExtendedPerformance> hof) {
        entries = Utils.zipWithIndex(hof)
                .map(t -> new Object[]{
                        t._1 + 1, // Index
                        t._2.getPlayer(), // The name
                        new Date(t._2.getTimestamp()), // Timestamp
                        t._2.getPerformance() * SPH, // Performance
                        t._2.getElapsedTime() / SPM, // The duration
                        t._2.getRightOutgoingTrainNumber(), //the right outgoing train
                        t._2.getFrequency() * SPH, // the frequency
                        t._2.getStationId() // the station id
                })
                .toArray(Object[][]::new);
        fireTableDataChanged();
    }
}
