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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.*;

public class WithTrain {
    private final List<TrainBuilder> builders;
    private final StationStatus status;

    public WithTrain(StationStatus status) {
        this.status = requireNonNull(status);
        builders = new ArrayList<>();
    }

    public WithTrain addTrain(TrainBuilder builder) {
        builders.add(builder);
        return this;
    }

    public WithTrain addTrain(int numCoaches, String arrival, String destination, String edge, String toward, double distance) {
        String id = "TT" + builders.size();
        return addTrain(new TrainBuilder(id, numCoaches, arrival, destination).at(edge, toward, distance).running());
    }

    public StationStatus build() {
        Stream<Train> trains = builders.stream()
                .map(builder -> builder.build(status));

        return status.setTrains(
                Stream.concat(status.getTrains().stream(),
                                trains)
                        .collect(Collectors.toList()));
    }

    public static class TrainBuilder {
        private Function<StationStatus, Train> builder;

        public TrainBuilder(String id, int numCoaches, String arrival, String destination) {
            requireNonNull(id);
            requireNonNull(arrival);
            requireNonNull(destination);
            if (numCoaches < MIN_COACH_COUNT || numCoaches > MAX_COACH_COUNT) {
                throw new IllegalArgumentException(format("# coaches must be between %d and %d (%d)",
                        MIN_COACH_COUNT, MAX_COACH_COUNT, numCoaches));
            }
            this.builder = (status) -> {
                Entry arrivalRoute;
                try {
                    arrivalRoute = status.getRoute(arrival);
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException(format("Illegal Entry %s", arrival));
                }
                Exit destinationRoute;
                try {
                    destinationRoute = status.getRoute(destination);
                } catch (ClassCastException ex) {
                    throw new IllegalArgumentException(format("Illegal Exit %s", destination));
                }
                return Train.create(id, numCoaches, arrivalRoute, destinationRoute);
            };
        }

        public TrainBuilder at(String edge, String toward, double distance) {
            requireNonNull(edge);
            requireNonNull(toward);
            if (distance < 0) {
                throw new IllegalArgumentException(format("Distance must be > 0 (%f)", distance));
            }
            BiFunction<Train, StationStatus, Train> modifier = (train, status) -> {
                Edge edge1 = status.getStationMap().getEdge(edge);
                Node to = status.getStationMap().getNode(toward);
                return train.setLocation(EdgeLocation.create(edge1, to, distance));
            };
            return modify(modifier);
        }

        public TrainBuilder braking() {
            return modify(train -> train.setState(Train.BRAKING_STATE));
        }

        public Train build(StationStatus status) {
            return builder.apply(status);
        }

        public TrainBuilder modify(BiFunction<Train, StationStatus, Train> modifier) {
            Function<StationStatus, Train> builder1 = builder;
            builder = status -> modifier.apply(builder1.apply(status), status);
            return this;
        }

        public TrainBuilder modify(UnaryOperator<Train> modifier) {
            Function<StationStatus, Train> builder1 = builder;
            builder = status -> modifier.apply(builder1.apply(status));
            return this;
        }

        public TrainBuilder running(double speed) {
            return modify(train -> train.setState(Train.RUNNING_STATE).setSpeed(speed));
        }

        public TrainBuilder running() {
            return running(MAX_SPEED);
        }
    }
}
