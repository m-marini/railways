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
import org.mmarini.railways2.model.routes.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Tracks the status of trains and station components.
 * Retrieves the sections, the edges occupied by trains.
 */
public class StationStatus {
    private final StationMap stationMap;
    private final Collection<? extends Route> routes;
    private final Collection<Train> trains;
    private final double time;
    private Map<Node, ? extends Route> routeByNode;
    private Map<Entry, Train> firstTrainByEntry;
    private Collection<Section> sections;
    private Map<Edge, Train> trainByEdge;
    private Map<Section, Train> trainBySection;
    private Map<? extends Edge, Section> sectionByEdge;
    private Map<Exit, Train> trainByExit;

    /**
     * Creates the station status
     *
     * @param stationMap  the station map
     * @param routes      the routes
     * @param time        the simulation time instant
     * @param trains      the trains
     * @param routeByNode the routes by nodes
     */
    protected StationStatus(StationMap stationMap, Collection<? extends Route> routes, Collection<Train> trains, double time, Map<Node, ? extends Route> routeByNode, Map<Entry, Train> firstTrainByEntry, Collection<Section> sections, Map<Edge, Train> trainByEdge, Map<Section, Train> trainBySection, Map<? extends Edge, Section> sectionByEdge, Map<Exit, Train> trainByExit) {
        this.stationMap = requireNonNull(stationMap);
        this.routes = requireNonNull(routes);
        this.time = time;
        this.trains = requireNonNull(trains);
        this.routeByNode = routeByNode;
        this.firstTrainByEntry = firstTrainByEntry;
        this.sections = sections;
        this.trainByEdge = trainByEdge;
        this.trainBySection = trainBySection;
        this.sectionByEdge = sectionByEdge;
        this.trainByExit = trainByExit;
    }

    /**
     * Returns first train by entry
     */
    Map<Entry, Train> createFirstTrainByEntry() {
        // The list must be sorted by arrival time
        Map<Entry, List<Train>> trainsByEntry1 = trains.stream()
                .filter(Train::isEntering)
                .collect(Collectors.groupingBy(
                        Train::getArrival)
                );
        return Tuple2.stream(trainsByEntry1)
                .flatMap(t -> t._2.stream()
                        .min(Comparator.comparingDouble(Train::getArrivalTime))
                        .map(t::setV2)
                        .stream())
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the route by node
     */
    private Map<Node, ? extends Route> createRouteByNode() {
        return routes.stream()
                .flatMap(route -> route.getNodes().stream()
                        .map(node -> Tuple2.of(node, route)))
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the section by edge
     */
    Map<? extends Edge, Section> createSectionByEdge() {
        return getSections().stream()
                .flatMap(section -> section.getEdges().stream()
                        .map(edge -> Tuple2.of(edge, section)))
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the sections
     */
    Collection<Section> createSections() {
        Set<Section> sections = new HashSet<>();
        Map<Section, Set<Edge>> crossingEdgesBySection = new HashMap<>();
        // Extracts all the section terminal directions
        Set<Direction> fringe = routes.stream()
                .filter(r -> r instanceof SectionTerminal)
                .flatMap(route -> route.getExits().stream())
                .collect(Collectors.toSet());

        while (!fringe.isEmpty()) {
            // Get a direction from fringe and find the section from it
            Direction dir = fringe.iterator().next();
            fringe.remove(dir);
            findSection(dir).ifPresent(t -> {
                sections.add(t._1);
                fringe.remove(t._1.getExit0());
                fringe.remove(t._1.getExit1());
                crossingEdgesBySection.put(t._1, t._2);
            });
        }
        // Computes the map of section by edge
        Map<Edge, Section> sectionByEdge = sections.stream()
                .flatMap(section -> section.getEdges().stream()
                        .map(edge -> Tuple2.of(edge, section)))
                .collect(Tuple2.toMap());

        // Computes and sets crossing sections
        Tuple2.stream(crossingEdgesBySection)
                .forEach(t -> {
                    Set<Section> crossingSections = t._2.stream()
                            .flatMap(edge -> {
                                Section section = sectionByEdge.get(edge);
                                return section != null ? Stream.of(section) : Stream.of();
                            })
                            .collect(Collectors.toSet());
                    t._1.setCrossingSections(crossingSections);
                });
        return sections;
    }

    /**
     * Returns rain by edge
     */
    Map<Edge, Train> createTrainByEdge() {
        return trains.stream()
                .flatMap(train -> getTrainEdges(train)
                        .map(edge -> Tuple2.of(edge, train)))
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the train by exit map
     */
    Map<Exit, Train> createTrainByExit() {
        return trains.stream().filter(
                Train::isExiting
        ).collect(Collectors.toMap(
                Train::getExitingNode,
                Function.identity()
        ));
    }

    /**
     * Returns the train by section
     */
    Map<Section, Train> createTrainBySection() {
        return getSections().stream()
                .flatMap(section ->
                        section.getEdges().stream()
                                .flatMap(edge -> getTrain(edge).stream())
                                .findAny()
                                .map(train -> Tuple2.of(section, train))
                                .stream()
                ).collect(Tuple2.toMap());
    }

    /**
     * Returns the forward edges for the distance
     *
     * @param location the location
     * @param distance the distance
     */
    Stream<Edge> findForwardEdges(EdgeLocation location, double distance) {
        Stream.Builder<Edge> builder = Stream.builder();
        Direction direction = location.getDirection();
        distance += location.opposite().getDistance();
        while (distance > 0 && direction != null) {
            // Get edge
            Edge edge = direction.getEdge();
            builder.add(edge);
            // computes the new limit distance
            distance -= edge.getLength();
            if (distance > 0) {
                // Next direction
                direction = getRoute(direction.getDestination()) // gets destination route
                        .getExit(location.getDirection()) // gets next exit for route
                        .orElse(null);
            }
        }
        return builder.build();
    }

    /**
     * Returns the section from a given direction with its crossing edges
     *
     * @param direction the direction
     */
    public Optional<Tuple2<Section, Set<Edge>>> findSection(Direction direction) {
        Route term = getRoute(direction.getOrigin());
        if (!(term instanceof SectionTerminal)) {
            throw new IllegalArgumentException(format("Route %s is not a section terminal", term.getId()));
        }
        List<Edge> edges = new ArrayList<>();
        Set<Edge> crossingEdges = new HashSet<>();
        Direction terminal0 = direction;
        for (; ; ) {
            edges.add(direction.getEdge());
            term = getRoute(direction.getDestination());
            if (term instanceof SectionTerminal) {
                return Optional.of(
                        Tuple2.of(
                                Section.create(terminal0, direction.opposite(), edges),
                                crossingEdges));
            }
            crossingEdges.addAll(term.getCrossingEdges(direction));
            Optional<Direction> next = term.getExit(direction);
            if (next.isEmpty()) {
                return Optional.empty();
            }
            direction = next.orElseThrow();
        }
    }

    /**
     * Returns the next exit for the given direction
     *
     * @param direction the entry direction
     */
    public Optional<Direction> getExit(Direction direction) {
        return getRoute(direction.getDestination()).getExit(direction);
    }

    /**
     * Returns the first train by entry (lazy value)
     */
    Map<Entry, Train> getFirstTrainByEntry() {
        if (firstTrainByEntry == null) {
            firstTrainByEntry = createFirstTrainByEntry();
        }
        return firstTrainByEntry;
    }

    /**
     * Returns the first train from a given entry
     *
     * @param entry the entry
     */
    Optional<Train> getFirstTrainFrom(Entry entry) {
        return Optional.ofNullable(getFirstTrainByEntry().get(entry));
    }

    /**
     * Returns the route for a given node
     *
     * @param node the node
     */
    public <T extends Route> T getRoute(Node node) {
        Route route = getRouteByNode().get(node);
        if (route == null) {
            throw new IllegalArgumentException(format("Route for node %s does not exist", node.getId()));
        }
        return (T) route;
    }

    public <T extends Route> T getRoute(String a) {
        return getRoute(stationMap.getNode(a));
    }

    /**
     * Returns the route by node (lazy value)
     */
    Map<Node, ? extends Route> getRouteByNode() {
        if (routeByNode == null) {
            routeByNode = createRouteByNode();
        }
        return routeByNode;
    }

    /**
     * Returns the routes
     */
    public Collection<? extends Route> getRoutes() {
        return routes;
    }

    /**
     * Returns the station status with a set of routes
     *
     * @param routes the routes
     */
    public StationStatus setRoutes(Collection<? extends Route> routes) {
        return new StationStatus(stationMap, routes, trains, time, null, null, null, null, null, null, null);
    }

    /**
     * Returns the section containing the edge
     *
     * @param edge the edge
     */
    Optional<Section> getSection(Edge edge) {
        return Optional.ofNullable(getSectionByEdge().get(edge));
    }

    /**
     * Returns the section by edge (lazy value)
     */
    Map<? extends Edge, Section> getSectionByEdge() {
        if (sectionByEdge == null) {
            sectionByEdge = createSectionByEdge();
        }
        return sectionByEdge;
    }

    /**
     * Returns the sections (lazy value)
     */
    Collection<Section> getSections() {
        if (sections == null) {
            sections = createSections();
        }
        return sections;
    }

    /**
     * Returns the simulation time instant
     */
    public double getTime() {
        return time;
    }

    /**
     * Returns the station with time set
     *
     * @param time the simulation instant
     */
    public StationStatus setTime(double time) {
        return time == this.time ? this :
                new StationStatus(stationMap, routes, trains, time, routeByNode, firstTrainByEntry, sections, trainByEdge, trainBySection, sectionByEdge, trainByExit);
    }

    /**
     * Returns the train transiting in an edge
     *
     * @param edge the edge
     */
    Optional<Train> getTrain(Edge edge) {
        return Optional.ofNullable(getTrainByEdge().get(edge));
    }

    /**
     * Returns the train in the section
     *
     * @param section the section
     */
    Optional<Train> getTrain(Section section) {
        return Optional.ofNullable(getTrainBySection().get(section));
    }

    /**
     * Returns train by edge (lazy value)
     */
    private Map<Edge, Train> getTrainByEdge() {
        if (trainByEdge == null) {
            trainByEdge = createTrainByEdge();
        }
        return trainByEdge;
    }

    /**
     * Returns the train by exit map (lazy value)
     */
    private Map<Exit, Train> getTrainByExit() {
        if (trainByExit == null) {
            trainByExit = createTrainByExit();
        }
        return trainByExit;
    }

    /**
     * Returns the train by section (lazy value)
     */
    private Map<Section, Train> getTrainBySection() {
        if (trainBySection == null) {
            trainBySection = createTrainBySection();
        }
        return trainBySection;
    }

    /**
     * Returns the edges of the train
     *
     * @param train the train
     */
    Stream<Edge> getTrainEdges(Train train) {
        EdgeLocation location = train.getLocation();
        return location != null ? findForwardEdges(location.opposite(), train.getLength()) : Stream.of();
    }

    /**
     * Returns the train collection
     */
    public Collection<Train> getTrains() {
        return trains;
    }

    /**
     * Returns the station status with a train collection set
     *
     * @param trains the trains
     */
    public StationStatus setTrains(Train... trains) {
        return setTrains(Arrays.asList(trains));
    }

    /**
     * Returns the station status with a train collection set
     *
     * @param trains the trains
     */
    public StationStatus setTrains(Collection<Train> trains) {
        return new StationStatus(stationMap, routes, trains, time, routeByNode, null, sections, null, null, sectionByEdge, null);
    }

    /**
     * Returns true if the entry is clear for given train
     * The entry is clear if the train is the first in the entry queue
     * and the entry section is clear
     *
     * @param train the train
     */
    public boolean isEntryClear(Train train) {
        Entry arrival = train.getArrival();
        return getFirstTrainFrom(arrival)
                .filter(train::equals) // Checks for first train in queue
                .filter(unused ->
                                // Checks for entry section clear
                        {
                            Edge edge = arrival.getExits().iterator().next().getEdge();
                            return isSectionClear(edge);
                        }
                )
                .isPresent();
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
     * Returns true if the next route of given direction is clear.
     * <p>
     * The next route is clear if
     * <ul><li>it is not an entry</li>
     * <li>and it is not a not clear exit</li>
     * <li>and it is not a not clear signal</li>
     * </ul>
     * </p>
     *
     * @param direction the direction
     */
    boolean isNextRouteClear(Direction direction) {
        Route route = getRoute(direction.getDestination());
        if (route instanceof Entry) {
            return false;
        } else if (route instanceof Exit) {
            return isExitClear((Exit) route);
        } else if (route instanceof Signal) {
            return isSignalClear(direction);
        } else {
            return true;
        }
    }

    /**
     * Returns true if next track is clear
     * The next track is clear if any signals within the limit distance is clear
     * and the train has to load at end of platform
     *
     * @param train the train
     */
    public boolean isNextTracksClear(Train train) {
        EdgeLocation location = train.getLocation();
        Direction direction = location.getDirection();
        double stopDistance = train.getStopDistance();
        double edgeLimitDistance = stopDistance - location.getDistance();
        while (direction != null && edgeLimitDistance > 0) {
            Edge edge = direction.getEdge();
            if (!isNextRouteClear(direction)
                    || (edge instanceof Platform && !train.isLoaded())) {
                return false;
            }
            edgeLimitDistance -= edge.getLength();
            // Find next direction
            direction = getExit(direction).orElse(null);
        }
        // Limit distance reached
        return edgeLimitDistance <= 0;
    }

    /**
     * Returns true if the section containing the edge is clear (no transiting train)
     *
     * @param edge the edge
     */
    boolean isSectionClear(Edge edge) {
        // Searches section
        return getSection(edge)
                // Filter for train in the section
                .filter(section -> getTrain(section).isEmpty())
                .isPresent();
    }

    /**
     * Returns true if the entry direction of signal is clear
     *
     * @param direction the entry direction
     */
    private boolean isSignalClear(Direction direction) {
        Signal signal = getRoute(direction.getDestination());
        return !signal.isLocked(direction)
                && signal.getExit(direction)
                .filter(exitDir -> isSectionClear(exitDir.getEdge()))
                .isPresent();
    }

    /**
     * Creates the station status by the station map, route builders and no trains
     * The builders are a tuple of node identifier array and function that creates the route by node array
     */
    public static class Builder {
        private final StationMap stationMap;
        private final List<Tuple2<String[], Function<Node[], ? extends Route>>> builders;

        /**
         * Creates the status builder
         *
         * @param stationMap the station map
         */
        public Builder(StationMap stationMap) {
            this.stationMap = stationMap;
            builders = new ArrayList<>();
        }

        /**
         * Returns the builder with a new route
         *
         * @param builder the route builder
         * @param nodes   the nodes
         */
        public Builder addRoute(Function<Node[], ? extends Route> builder, String... nodes) {
            builders.add(Tuple2.of(nodes, builder));
            return this;
        }

        /**
         * Returns the station status by the station map, route builders and no trains
         */
        public StationStatus build() {
            List<Route> routes1 = builders.stream()
                    .map(t -> {
                        Node[] nodes = Arrays.stream(t._1)
                                .map(stationMap::getNode)
                                .toArray(Node[]::new);
                        return t._2.apply(nodes);
                    }).collect(Collectors.toList());
            return new StationStatus(stationMap, routes1, List.of(), 0, null, null, null, null, null, null, null);
        }
    }
}
