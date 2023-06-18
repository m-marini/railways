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

import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.routes.Route;

import java.util.Optional;

/**
 * Generates the new station status after a time interval.
 * Applies the changes of the status to have an updated status.
 */
public class SimulationContext {
    private StationStatus status;

    /**
     * Creates the initial simulation context
     *
     * @param status the status
     */
    public SimulationContext(StationStatus status) {
        this.status = status;
    }

    /**
     * Returns the next exit for the given direction
     *
     * @param direction the entry direction
     */
    public Optional<Direction> getNextExit(Direction direction) {
        return status.getExit(direction);
    }

    /**
     * Returns the route of a node
     *
     * @param node the node
     */
    public Route getRoute(Node node) {
        return status.getRoute(node);
    }

    /**
     * Returns the status of station
     */
    public StationStatus getStatus() {
        return status;
    }

    /**
     * Returns the time after a time interval
     *
     * @param interval the interval (s)
     */
    public double getTimeAfter(double interval) {
        return status.getTime() + interval;
    }

    /**
     * Returns the remaining time interval to the required time (s)
     *
     * @param time the required time instant (s)
     */
    public double getTimeTo(double time) {
        return time - status.getTime();
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
     * Returns if the next track is clear
     *
     * @param direction the direction
     */
    public boolean isNextRouteClear(Direction direction) {
        return status.isNextRouteClear(direction);
    }

    /**
     * Returns true if next track is clear
     * The next track is clear if any signals within the limit distance is clear
     * and the train has to load at end of platform
     *
     * @param train the train
     */
    public boolean isNextTracksClear(Train train) {
        return status.isNextSignalClear(train);
    }

    /**
     * Locks the signal of the given direction
     *
     * @param direction the direction
     */
    public void lockSignals(Direction direction) {
        /*
        Optional<StationStatus> newStatus = status.getSection(direction.getEdge())
                .map(section -> {
                    Stream.of(section.getExit0(), section.getExit1())
                            .flatMap(dir -> {
                                // Finds the route terminal of section
                                Route route = status.getRoute(dir.getOrigin());
                                // Find the entry direction into the section
                                Optional<Direction> entryOpt = route.getExit(dir.opposite()).map(Direction::opposite);
                                return entryOpt.map(entry -> Tuple2.of(route, entry)).stream();
                            })
                            .filter(t -> t._1 instanceof Signal)
                            .map(t -> ((Signal) t._1).lock(t._2))
                            .forEach(signal -> {
                                routes.remove(signal);
                                routes.add(signal);
                            });
                    return status.setRoutes(routes);
                });

         */
        status = status.getSection(direction.getEdge())
                .map(status::lock)
                .orElse(status);
    }
}