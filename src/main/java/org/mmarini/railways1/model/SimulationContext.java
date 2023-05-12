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

package org.mmarini.railways1.model;

import org.mmarini.railways2.model.geometry.OrientedLocation;
import org.mmarini.railways1.model.routes.RouteDirection;
import org.mmarini.railways1.model.routes.SingleNodeRoute;
import org.mmarini.railways1.model.routes.Signal;
import org.mmarini.railways1.model.trains.Train;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Generates the new station status after a time interval.
 * Applies the changes of the status to have an updated status.
 */
public class SimulationContext {
    private final double dt;
    private final Set<Train> trains;
    private StationStatus status;

    /**
     * Creates the initial simulation context
     *
     * @param status the status
     * @param dt     the time interval (s)
     */
    public SimulationContext(StationStatus status, double dt) {
        this.status = status;
        this.dt = dt;
        this.trains = new HashSet<>(status.getTrains());
    }

    /**
     * Returns the time interval
     */
    public double getDt() {
        return dt;
    }

    /**
     * Returns the time ofter interval
     */
    public double getNextTime() {
        return status.getTime() + dt;
    }

    /**
     * Returns the status
     */
    public StationStatus getStatus() {
        return status;
    }

    /**
     * Returns the simulation time (s)
     */
    public double getTime() {
        return status.getTime();
    }

    /**
     * Returns true if the entry is clear
     * The entry is clear if the waiting for entry train is the first in the entry queue
     * and the entry section is clear
     *
     * @param train the train
     */
    public boolean isEntryClear(Train train) {
        return status.isEntryClear(train);
    }

    /**
     * Returns true if next signal is clear
     * The next signal is clear if exits and the signal is not locked and the next section is clear
     *
     * @param location the location
     */
    public boolean isNextSignalClear(OrientedLocation location) {
        return status.isNextSignalClear(location);
    }

    /**
     * Returns true if next track within the limit distance is clear
     * The next track is clear any signal within the limit distance is clear
     *
     * @param location       the location
     * @param limitDistance  the limit distance (m)
     * @param stopForLoading true if train should stop for loading
     */
    public boolean isNextTracksClear(OrientedLocation location, double limitDistance, boolean stopForLoading) {
        return status.isNextTracksClear(location, limitDistance, stopForLoading);
    }

    /**
     * Locks the route direction if it is a Signal
     *
     * @param direction the direction
     */
    public void lockSignal(RouteDirection direction) {
        SingleNodeRoute route = direction.getRoute();
        if (route instanceof Signal) {
            Signal newRoute = ((Signal) route).setLocked(direction.getIndex(), true);
            status = status.putRoute(newRoute);
        }
    }

    /**
     * Put the train in the station status
     *
     * @param train the train
     */
    private void putTrain(Train train) {
        trains.add(train);
        status = status.setTrains(trains);
    }

    /**
     * Removes the train from status
     *
     * @param train the train
     */
    void removeTrain(Train train) {
        trains.remove(train);
        status = status.setTrains(trains);
    }

    public StationStatus simulate() {
        for (Train train : status.getTrains()) {
            train.tick(this)
                    .ifPresentOrElse(
                            this::putTrain,
                            () -> this.removeTrain(train)
                    );
        }
        return status.setTime(status.getTime() + dt);
    }

    /**
     * Returns the terminal route direction of an edge point
     *
     * @param orientedLocation the edge point
     */
    public Optional<RouteDirection> terminalDirection(OrientedLocation orientedLocation) {
        return status.terminalDirection(orientedLocation);
    }
}
