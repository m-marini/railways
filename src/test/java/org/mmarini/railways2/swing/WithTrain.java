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
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.EdgeLocation;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class WithTrain {
    private final List<Train> trains;
    private final StationStatus status;

    public WithTrain(StationStatus status) {
        this.status = status;
        trains = new ArrayList<>();
    }

    public WithTrain addTrain(int numCoaches, String arrivalId, String destinationId, String edgeId, String toId, double distance) {
        String id = "T" + trains.size();
        Entry arrivalRoute;
        try {
            arrivalRoute = status.getRoute(arrivalId);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(format("Illegal Entry %s", arrivalId));
        }
        Exit destinationRoute;
        try {
            destinationRoute = status.getRoute(destinationId);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(format("Illegal Exit %s", destinationId));
        }
        Edge edge = status.getStationMap().getEdge(edgeId);
        Node to = status.getStationMap().getNode(toId);
        trains.add(Train.create(id, numCoaches, arrivalRoute, destinationRoute)
                .setLocation(EdgeLocation.create(edge, to, distance)));
        return this;
    }

    public StationStatus build() {
        return status.setTrains(trains);
    }
}
