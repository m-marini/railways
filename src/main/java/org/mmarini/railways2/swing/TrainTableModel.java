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
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.EdgeLocation;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mmarini.railways2.model.RailwayConstants.KMH;

/**
 * Models the train info in a table
 */
public class TrainTableModel extends AbstractTableModel {
    public static final String UNKONWN_STRING = "???";
    private static final List<Train.State> STATE_ORDERS = List.of(
            Train.STATE_WAITING_FOR_RUN,
            Train.STATE_WAITING_FOR_SIGNAL,
            Train.STATE_LOADING,
            Train.STATE_ENTERING,
            Train.STATE_EXITING,
            Train.STATE_BRAKING,
            Train.STATE_RUNNING
    );

    /**
     * Returns less than zero if train a is before train b
     *
     * @param a the train
     * @param b the train
     */
    private static int compare(Train a, Train b) {
        int stateCmp = compare(a.getState(), b.getState());
        if (stateCmp != 0) {
            return stateCmp;
        }
        return Double.compare(a.getArrivalTime(), b.getArrivalTime());
    }

    /**
     * Returns less than zero if status a is before status b
     *
     * @param a the status
     * @param b the status
     */
    private static int compare(Train.State a, Train.State b) {
        return Integer.compare(STATE_ORDERS.indexOf(a), STATE_ORDERS.indexOf(b));
    }

    private List<Train> trains;
    private String stationId;

    /**
     * Creates the table model
     */
    public TrainTableModel() {
        stationId = "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 1:
                return Long.class;
            case 2:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Messages.getString("TrainTableModel.header.name");
            case 1:
                return Messages.getString("TrainTableModel.header.speed");
            case 2:
                return Messages.getString("TrainTableModel.header.loaded");
            case 3:
                return Messages.getString("TrainTableModel.header.location");
            case 4:
                return Messages.getString("TrainTableModel.header.destination");
            case 5:
                return Messages.getString("TrainTableModel.header.status");
        }
        return UNKONWN_STRING;
    }

    /**
     * Returns the string location of the train
     *
     * @param train the train
     */
    private String getLocation(Train train) {
        return train.getLocation()
                .map(EdgeLocation::getDirection)
                .map(Direction::getEdge)
                .map(Edge::getId)
                .or(() ->
                        Optional.of(train.getState().equals(Train.STATE_ENTERING) ?
                                train.getArrival().getId() :
                                train.getDestination().getId()))
                .map(id -> StationLabels.getLabel(stationId, id))
                .orElse(UNKONWN_STRING);
    }

    @Override
    public int getRowCount() {
        List<Train> list = trains;
        if (list == null)
            return 0;
        return list.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<Train> list = trains;
        if (list == null)
            return UNKONWN_STRING;
        Train train = list.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return train.getId();
            case 1:
                return Math.round(train.getSpeed() / KMH);
            case 2:
                return !train.isUnloaded();
            case 3:
                return getLocation(train);
            case 4:
                return Messages.getString("station." + stationId + "." + train.getDestination().getId());
            case 5:
                return Messages
                        .getString("TrainTableModel.value.status." + train.getState().getId());
        }
        return UNKONWN_STRING;
    }

    /**
     * Sets the station status
     *
     * @param status the station status
     */
    public void setStatus(StationStatus status) {
        this.stationId = status.getStationMap().getId();
        this.trains = new ArrayList<>(status.getTrains());
        this.trains.sort(TrainTableModel::compare);
        fireTableDataChanged();

    }
}