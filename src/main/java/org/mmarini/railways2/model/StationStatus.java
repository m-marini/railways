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

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.route.*;
import org.mmarini.railways2.model.trains.Train;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;

/**
 * Tracks the status of trains and station components by simulating the elapsed time.
 * Retrieves the sections, the edges occupied by trains.
 */
public class StationStatus {
    public static StationStatus create(StationMap stationMap, RoutesConfig routes, Collection<Train> trains) {
        return new StationStatus(stationMap, routes, 0, trains, null, null, null, null);
    }

    private final StationMap stationMap;
    private final RoutesConfig routes;
    private final double time;
    private final Collection<Train> trains;
    private Map<Edge, Train> trainByEdge;
    private Map<Section, Train> trainBySection;
    private Map<String, List<Train>> trainsByEntry;
    private Map<Exit, Train> trainByExit;

    /**
     * Creates the station status
     *
     * @param stationMap     the station map
     * @param routes         the routes dictionary
     * @param time           the time (s)
     * @param trains         the train dictionary
     * @param trainByEdge    the train map by edge
     * @param trainBySection the train map by sections
     * @param trainsByEntry  the trains by entry
     * @param trainByExit    the train by exit
     */
    public StationStatus(StationMap stationMap, RoutesConfig routes, double time, Collection<Train> trains, Map<Edge, Train> trainByEdge, Map<Section, Train> trainBySection, Map<String, List<Train>> trainsByEntry, Map<Exit, Train> trainByExit) {
        this.stationMap = requireNonNull(stationMap);
        this.routes = requireNonNull(routes);
        this.trains = requireNonNull(trains);
        this.time = time;
        this.trainByEdge = trainByEdge;
        this.trainBySection = trainBySection;
        this.trainsByEntry = trainsByEntry;
        this.trainByExit = trainByExit;
    }

    /**
     * Returns the map of train by edge
     */
    private Map<Edge, Train> createTrainByEdge() {
        return trains.stream()
                .flatMap(t ->
                        this.findBackwardEdges(t.getLocation(), t.getLength() * COACH_LENGTH)
                                .map(e -> Tuple2.of(e, t))
                ).collect(Tuple2.toMap());
    }

    /**
     * Returns the train by exit map
     */
    private Map<Exit, Train> createTrainByExit() {
        return trains.stream().filter(
                Train::isExiting
        ).collect(Collectors.toMap(
                Train::getExitingNode,
                Function.identity()
        ));
    }

    /**
     * Returns the map of train by section
     */
    private Map<Section, Train> createTrainBySection() {
        return routes.getSections().stream()
                .flatMap(section ->
                        section.getEdges().stream()
                                .flatMap(edge -> train(edge).stream())
                                .findAny()
                                .map(train -> Tuple2.of(section, train))
                                .stream()
                ).collect(Tuple2.toMap());
    }

    /**
     * Returns the map of train by entry
     */
    private Map<String, List<Train>> createTrainsByEntry() {
        // The list must be sorted by arrival time
        Map<String, List<Train>> trainsByEntry1 = trains.stream()
                .filter(Train::isEntering)
                .collect(Collectors.groupingBy(
                        t -> t.getArrival().getId())
                );
        return Tuple2.stream(trainsByEntry1)
                .map(t -> t.setV2(t._2.stream()
                        .sorted(Comparator.comparingDouble(Train::getArrivalTime))
                        .collect(Collectors.toList())
                ))
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the list of edges traversing backward the route
     *
     * @param start  the start point
     * @param length the length of route
     */
    public Stream<Edge> findBackwardEdges(OrientedLocation start, double length) {
        Stream.Builder<Edge> builder = Stream.builder();
        OrientedLocation orientedLocation = start;
        while (length >= 0 && orientedLocation != null) {
            Edge edge = orientedLocation.getEdge();
            builder.add(edge);
            length -= orientedLocation.getDistance();
            Node term = orientedLocation.getOrigin();
            Route route = routes.getRoute(term.getId());
            Optional<OrientedLocation> next = route.getConnectedDirection(route.indexOf(edge))
                    .flatMap(RouteDirection::getLocation)
                    .map(OrientedLocation::reverse);
            orientedLocation = next.orElse(null);
        }
        return builder.build();
    }

    /**
     * Returns the first train of entry
     *
     * @param entry the entry
     */
    public Optional<Train> firstTrainFrom(Entry entry) {
        return Optional.ofNullable(getTrainsByEntry().get(entry.getId()))
                .flatMap(l -> l.isEmpty() ?
                        Optional.empty() :
                        Optional.of(l.get(0)));
    }

    /**
     * Returns the route dictionary
     */
    public RoutesConfig getRoutes() {
        return routes;
    }

    /**
     * Returns the station status with a new route map
     *
     * @param routes the route map
     */
    public StationStatus setRoutes(RoutesConfig routes) {
        return new StationStatus(stationMap, routes, time, trains, null, null, null, null);
    }

    /**
     * Returns the time (s)
     */
    public double getTime() {
        return time;
    }

    /**
     * Returns the station status with a new time
     *
     * @param time the new time
     */
    public StationStatus setTime(double time) {
        return new StationStatus(stationMap, routes, time, trains, trainByEdge, trainBySection, trainsByEntry, trainByExit);
    }

    /**
     * Returns the map of train by edge
     */
    public Map<Edge, Train> getTrainByEdge() {
        if (trainByEdge == null) {
            trainByEdge = createTrainByEdge();
        }
        return trainByEdge;
    }

    /**
     * Returns the train by exit map
     */
    public Map<Exit, Train> getTrainByExit() {
        if (trainByExit == null) {
            trainByExit = createTrainByExit();
        }
        return trainByExit;
    }

    /**
     * Returns the map of train by section
     */
    public Map<Section, Train> getTrainBySection() {
        if (trainBySection == null) {
            trainBySection = createTrainBySection();
        }
        return trainBySection;
    }

    /**
     * Returns the collection of trains
     */
    public Collection<Train> getTrains() {
        return trains;
    }

    /**
     * Returns the station status with new train collection
     *
     * @param trains the train collection
     */
    StationStatus setTrains(Collection<Train> trains) {
        return new StationStatus(stationMap, routes, time, trains, null, null, null, null);
    }

    /**
     * Returns the map of train by entry
     */
    Map<String, List<Train>> getTrainsByEntry() {
        if (trainsByEntry == null) {
            trainsByEntry = createTrainsByEntry();
        }
        return trainsByEntry;
    }

    /**
     * Returns true if the entry is clear
     * The entry is clear if the waiting for entry train is the first in the entry queue
     * and the entry section is clear
     *
     * @param train the train
     */
    public boolean isEntryClear(Train train) {
        Entry arrival = train.getArrival();
        return firstTrainFrom(arrival)
                .filter(train::equals)
                .filter(x ->
                        arrival.getDirection(0)
                                .filter(this::isSectionClear)
                                .isPresent()
                ).isPresent();
    }

    /**
     * Returns true if the exit is clear
     *
     * @param exit the exit
     */
    boolean isExitClear(Exit exit) {
        return !getTrainByExit().containsKey(exit);
    }

    /**
     * Returns true if next signal is clear
     * The next signal is clear if exists and the signal is not locked and the next section is clear
     *
     * @param location the location
     */
    boolean isNextSignalClear(OrientedLocation location) {
        // Find the section terminal
        return routes.getTerminalDirection(location)
                .filter(termDir -> {
                    // Found terminal route
                    Route route = termDir.getRoute();
                    if (route instanceof Entry) {
                        // terminal route is entry
                        return false;
                    } else if (route instanceof Exit) {
                        // terminal route  is exit
                        return isExitClear((Exit) route);
                    } else if (route instanceof Signal) {
                        // terminal route is signal
                        if (route.isLocked(termDir.getIndex())) {
                            return false;
                        }
                        return termDir.connectedDirection()
                                .filter(this::isSectionClear)
                                .isPresent();
                    } else {
                        return false;
                    }
                }).isPresent();
    }

    /**
     * Returns true if next track within the limit distance is clear
     * The next track is clear any signal within the limit distance is clear
     * and the train should not stop for load
     *
     * @param location       the location
     * @param limitDistance  the limit distance (m)
     * @param stopForLoading true if train should stop for load
     */
    public boolean isNextTracksClear(OrientedLocation location, double limitDistance, boolean stopForLoading) {
        while (location != null) {
            // Checks for distance
            Edge edge = location.getEdge();
            limitDistance -= edge.getLength() - location.getDistance();
            if (limitDistance <= 0) {
                return true;
            }
            Route route = routes.getRoute(location.getTerminal().getId());
            if (route instanceof Exit) {
                return isExitClear((Exit) route);
            } else if (route instanceof Entry
                    || (route instanceof SectionTerminal && !isNextSignalClear(location))
                    || (edge instanceof Platform && stopForLoading)) {
                return false;
            }

            location = routes.getTerminalDirection(location)
                    .flatMap(RouteDirection::connectedDirection)
                    .flatMap(RouteDirection::getLocation)
                    .orElse(null);
        }
        return false;
    }

    /**
     * Returns true if route direction is clear(no transit train)
     *
     * @param direction the direction
     */
    public boolean isSectionClear(RouteDirection direction) {
        return direction.getLocation()
                .map(OrientedLocation::getEdge)
                .flatMap(routes::getSection)
                .filter(this::isSectionClear)
                .isPresent();
    }

    /**
     * Returns true if the section is clear (no transiting train).
     *
     * @param section the section
     */
    boolean isSectionClear(Section section) {
        return train(section).isEmpty();
    }

    /**
     * Returns the status with a changed route
     *
     * @param route the route
     */
    public StationStatus putRoute(Route route) {
        return setRoutes(routes.putRoute(route));
    }

    /**
     * Returns the terminal route direction of an edge point
     *
     * @param orientedLocation the edge point
     */
    public Optional<RouteDirection> terminalDirection(OrientedLocation orientedLocation) {
        return routes.getTerminalDirection(orientedLocation);
    }

    /**
     * Returns the train in a section
     *
     * @param section the section
     */
    Optional<Train> train(Section section) {
        return Optional.ofNullable(getTrainBySection().get(section));
    }

    /**
     * Returns train for a given edge
     *
     * @param edge the edge
     */
    public Optional<Train> train(Edge edge) {
        return Optional.ofNullable(getTrainByEdge().get(edge));
    }
}
