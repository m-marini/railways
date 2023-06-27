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
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.atan2;
import static java.lang.Math.ceil;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.RailwayConstants.*;
import static org.mmarini.railways2.model.Train.*;
import static org.mmarini.railways2.model.Utils.nextPoisson;

/**
 * Tracks the status of trains and station components.
 * Retrieves the sections, the edges occupied by trains.
 */
public class StationStatus {

    /**
     * Arrival train frequency (#/s)
     */
    private static final Logger logger = LoggerFactory.getLogger(StationStatus.class);
    private static final int MIN_TRAIN_ID = 100;
    private static final int MAX_TRAIN_ID = 999;

    /**
     * Returns the station status
     *
     * @param stationMap     the station map
     * @param routes         the routes
     * @param gameDuration   the game duration (s)
     * @param trains         the trains
     * @param time           the time instant
     * @param trainFrequency the train frequency
     * @param events         the event subscriber
     */
    public static StationStatus create(StationMap stationMap, List<Route> routes, double gameDuration, List<Train> trains, double time, double trainFrequency, Subscriber<SoundEvent> events) {
        logger.atDebug().setMessage("Creating station {}").addArgument(stationMap::getId).log();
        ExtendedPerformance performance = ExtendedPerformance.create(stationMap.getId(), gameDuration).setElapsedTime(time);
        return new StationStatus(stationMap, routes, trains, true, trainFrequency, performance,
                events, null, null, null, null, null, null, null, null, null
        );
    }

    private final StationMap stationMap;
    private final Collection<? extends Route> routes;
    private final Collection<Train> trains;
    private final double trainFrequency;
    private final ExtendedPerformance performance;
    private final Subscriber<SoundEvent> events;
    private final boolean autolock;
    private List<Entry> entries;
    private List<Exit> exits;
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
     * @param stationMap        the station map
     * @param routes            the routes
     * @param trains            the trains
     * @param autolock
     * @param trainFrequency    the train frequency
     * @param performance       the game performance
     * @param events            the event subscriber
     * @param entries           the list of entries
     * @param exits             the list of exits
     * @param routeByNode       the routes by nodes
     * @param firstTrainByEntry first train by entry
     * @param sections          the sections
     * @param trainByEdge       the train bay edge
     * @param trainBySection    the train by section
     * @param sectionByEdge     the section by edge
     * @param trainByExit       the train by exit
     */
    protected StationStatus(StationMap stationMap, Collection<? extends Route> routes,
                            Collection<Train> trains, boolean autolock, double trainFrequency,
                            ExtendedPerformance performance, Subscriber<SoundEvent> events, List<Entry> entries, List<Exit> exits,
                            Map<Node, ? extends Route> routeByNode,
                            Map<Entry, Train> firstTrainByEntry, Collection<Section> sections,
                            Map<Edge, Train> trainByEdge, Map<Section, Train> trainBySection,
                            Map<? extends Edge, Section> sectionByEdge, Map<Exit, Train> trainByExit) {
        this.stationMap = requireNonNull(stationMap);
        this.routes = requireNonNull(routes);
        this.trains = requireNonNull(trains);
        this.autolock = autolock;
        this.trainFrequency = trainFrequency;
        this.performance = requireNonNull(performance);
        this.entries = entries;
        this.exits = exits;
        this.routeByNode = routeByNode;
        this.firstTrainByEntry = firstTrainByEntry;
        this.sections = sections;
        this.trainByEdge = trainByEdge;
        this.trainBySection = trainBySection;
        this.sectionByEdge = sectionByEdge;
        this.trainByExit = trainByExit;
        this.events = events;
    }

    /**
     * Returns the station status with added incoming train number
     *
     * @param trainNumber the number of incoming train
     */
    private StationStatus addIncomingTrains(int trainNumber) {
        return setPerformance(performance.addTrainIncomingNumber(trainNumber));
    }

    /**
     * Returns the coach location
     *
     * @param start the front of coach
     */
    Optional<Tuple2<Point2D, Double>> computeCoachLocation(EdgeLocation start) {
        return getLocationAt(start, COACH_RAIL_DISTANCE)
                .map(EdgeLocation::getLocation)
                .flatMap(front ->
                        getLocationAt(start, COACH_LENGTH - COACH_RAIL_DISTANCE)
                                .map(EdgeLocation::getLocation)
                                .map(rear -> {
                                    double x0 = rear.getX();
                                    double x1 = front.getX();
                                    double y0 = rear.getY();
                                    double y1 = front.getY();
                                    Point2D center = new Point2D.Double(
                                            (x0 + x1) / 2,
                                            (y0 + y1) / 2);
                                    double dx = x1 - x0;
                                    double dy = y1 - y0;
                                    double orientation = atan2(dy, dx);
                                    return Tuple2.of(center, orientation);
                                })
                );
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
     * Returns a new train
     *
     * @param trains  the train list
     * @param random  the random number generator
     * @param arrival the arrival entry
     */
    Train createNewTrain(List<Train> trains, Random random, Entry arrival) {
        List<Exit> exits = getExits();
        // Generates a unique id
        String trainId;
        do {
            trainId = "T" + (random.nextInt(MAX_TRAIN_ID - MIN_TRAIN_ID + 1) + MIN_TRAIN_ID);
        } while (trains.stream().map(Train::getId).anyMatch(trainId::equals));
        // Generates the train length
        int numCoaches = random.nextInt(MAX_COACH_COUNT - MIN_COACH_COUNT + 1) + MIN_COACH_COUNT;
        // Generates the arrival and destination of train
        Exit destination = exits.get(random.nextInt(exits.size()));
        play(SoundEvent.ARRIVING);
        return Train.create(trainId, numCoaches, arrival, destination)
                .setArrivalTime(getTime() + ENTRY_TIMEOUT);
    }

    /**
     * Returns the list of train with new random created trains
     *
     * @param trains the train list
     * @param lambda the lambda generator factor
     * @param random the random number generator
     */
    List<Train> createNewTrains(List<Train> trains, double lambda, Random random) {
        // Generates new trains
        int n = nextPoisson(random, lambda);
        List<Entry> entries = getEntries();
        for (int i = 0; i < n; i++) {
            Entry arrival = entries.get(random.nextInt(entries.size()));
            trains.add(createNewTrain(trains, random, arrival));
        }
        return trains;
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
                .flatMap(route -> route.getValidExits().stream())
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
                        .getExit(direction) // gets next exit for route
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
     * Returns the bounds of station
     */
    public Rectangle2D getBounds() {
        return stationMap.getBounds();
    }

    /**
     * Returns the list of entries
     */
    List<Entry> getEntries() {
        if (entries == null) {
            entries = getRoutes().stream()
                    .filter(route -> route instanceof Entry)
                    .map(entry -> (Entry) entry)
                    .collect(Collectors.toList());
        }
        return entries;
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
     * Returns the list of exits
     */
    List<Exit> getExits() {
        if (exits == null) {
            exits = getRoutes().stream()
                    .filter(route -> route instanceof Exit)
                    .map(entry -> (Exit) entry)
                    .collect(Collectors.toList());
        }
        return exits;
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
     * Returns the location from a point at a given distance
     *
     * @param start    the start location
     * @param distance the distance
     */
    Optional<EdgeLocation> getLocationAt(EdgeLocation start, double distance) {
        while (start != null) {
            double terminalDistance = start.getDistance();
            if (distance <= terminalDistance) {
                return Optional.of(start.setDistance(terminalDistance - distance));
            }
            distance -= terminalDistance;
            start = getNextDirection(start.getDirection())
                    .map(dir -> new EdgeLocation(dir, dir.getEdge().getLength()))
                    .orElse(null);
        }
        // Terminal not found
        return Optional.empty();
    }

    /**
     * Returns the next direction
     *
     * @param direction the direction
     */
    private Optional<Direction> getNextDirection(Direction direction) {
        return getRoute(direction.getDestination()).getExit(direction);
    }

    /**
     * Returns the game performance
     */
    public ExtendedPerformance getPerformance() {
        return performance;
    }

    /**
     * Returns the station status with set performance
     *
     * @param performance the new performance
     */
    private StationStatus setPerformance(ExtendedPerformance performance) {
        return new StationStatus(stationMap, routes, trains, autolock, trainFrequency, performance, events, entries, exits, routeByNode, firstTrainByEntry, sections, trainByEdge, trainBySection, sectionByEdge, trainByExit);
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
        return new StationStatus(stationMap, routes, trains, autolock, trainFrequency, performance, events, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Returns the section containing the edge
     *
     * @param edge the edge
     */
    public Optional<Section> getSection(Edge edge) {
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
     * Returns the segments from a location for a given distance
     *
     * @param start    the start location
     * @param distance the length (m)
     */
    Stream<EdgeSegment> getSegments(EdgeLocation start, double distance) {
        Stream.Builder<EdgeSegment> builder = Stream.builder();
        while (distance > 0 && start != null) {
            double len = start.getDistance();
            if (distance <= len) {
                EdgeSegment seg = EdgeSegment.create(start, distance);
                builder.add(seg);
                distance = 0;
            } else {
                EdgeSegment seg = EdgeSegment.create(start, len);
                builder.add(seg);
                distance -= len;
                start = getExit(start.getDirection())
                        .map(dir -> new EdgeLocation(dir, dir.getEdge().getLength()))
                        .orElse(null);
            }
        }
        return builder.build();
    }

    /**
     * Returns the station map
     */
    public StationMap getStationMap() {
        return stationMap;
    }

    /**
     * Returns the simulation time instant
     */
    public double getTime() {
        return performance.getElapsedTime();
    }

    /**
     * Returns the station with time set
     *
     * @param time the simulation instant
     */
    public StationStatus setTime(double time) {
        return time == this.getTime() ? this :
                new StationStatus(stationMap, routes, trains, autolock, trainFrequency, performance.setElapsedTime(time), events, entries, exits, routeByNode, firstTrainByEntry, sections, trainByEdge, trainBySection, sectionByEdge, trainByExit);
    }

    /**
     * Returns the train
     *
     * @param id the train identifier
     */
    public Optional<Train> getTrain(String id) {
        return trains.stream().filter(t -> id.equals(t.getId())).findAny();
    }

    /**
     * Returns the train transiting in an edge
     *
     * @param edge the edge
     */
    public Optional<Train> getTrain(Edge edge) {
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
     * Returns the train composition
     *
     * @param train the train
     */
    TrainComposition getTrainCoaches(Train train) {
        Train.State state = train.getState();
        List<Tuple2<Point2D, Double>> coaches = new ArrayList<>();
        if (Train.STATE_EXITING.equals(state)) {
            int numExitedCoaches = (int) ceil(train.getExitDistance() / COACH_LENGTH);
            if (numExitedCoaches >= train.getNumCoaches()) {
                return TrainComposition.EMPTY;
            }
            double startDistance = numExitedCoaches * COACH_LENGTH - train.getExitDistance();
            Direction exitDir = train.getExitingNode().getValidExits().iterator().next();
            EdgeLocation start = new EdgeLocation(exitDir, exitDir.getEdge().getLength() - startDistance);
            int n = train.getNumCoaches() - numExitedCoaches - 1;
            while (start != null && n > 0) {
                computeCoachLocation(start).ifPresent(coaches::add);
                start = getLocationAt(start, COACH_LENGTH).orElse(null);
                n--;
            }
            Tuple2<Point2D, Double> tail = computeCoachLocation(start).orElse(null);
            return new TrainComposition(null, tail, coaches);
        } else {
            return train.getLocation().map(location -> {
                EdgeLocation start = location.opposite();
                Tuple2<Point2D, Double> head = computeCoachLocation(start).orElse(null);
                start = getLocationAt(start, COACH_LENGTH).orElse(null);
                int n = train.getNumCoaches() - 2;
                while (start != null && n > 0) {
                    computeCoachLocation(start).ifPresent(coaches::add);
                    start = getLocationAt(start, COACH_LENGTH).orElse(null);
                    n--;
                }
                Tuple2<Point2D, Double> tail = computeCoachLocation(start).orElse(null);
                return new TrainComposition(head, tail, coaches);
            }).orElse(new TrainComposition(null, null, List.of()));
        }
    }

    /**
     * Returns the edges of the train
     *
     * @param train the train
     */
    Stream<Edge> getTrainEdges(Train train) {
        return train.getLocation().stream()
                .flatMap(location -> findForwardEdges(location.opposite(), train.getLength()));
    }

    /**
     * Returns the train segments
     *
     * @param train the train
     */
    public Stream<EdgeSegment> getTrainSegments(Train train) {
        if (!train.isExiting()) {
            return train.getLocation().stream()
                    .flatMap(location -> getSegments(location.opposite(), train.getLength()));
        } else if (train.getExitDistance() >= train.getLength()) {
            // Train completely exited
            return Stream.empty();
        } else {
            // Train partially exited
            double exitingTrainLength = train.getLength() - train.getExitDistance();
            Node exitingNode = train.getExitingNode().getNodes().get(0);
            Direction exitingDirection = exitingNode.getEntries().get(0);
            EdgeLocation exitLocation = new EdgeLocation(exitingDirection, 0).opposite();
            return getSegments(exitLocation, exitingTrainLength);
        }
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
        return new StationStatus(stationMap, routes, trains, autolock, trainFrequency, performance, events, entries, exits, routeByNode, null, sections, null, null, sectionByEdge, null);
    }

    /**
     * Returns the composition of all trains
     */
    public Stream<TrainComposition> getTrainsCoaches() {
        return getTrains().stream()
                .map(this::getTrainCoaches);
    }

    /**
     * Returns true if autolock set
     */
    public boolean isAutolock() {
        return autolock;
    }

    /**
     * Returns true if the status is conflict
     */
    boolean isConsistent() {
        return getSections().stream()
                .filter(section -> section.getCrossingSections().stream()
                        .anyMatch(this::isSectionWithTrain))
                .findAny()
                .isEmpty();
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
                            Edge edge = arrival.getValidExits().iterator().next().getEdge();
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
    public boolean isExitClear(Exit exit) {
        return !getTrainByExit().containsKey(exit);
    }

    /**
     * Returns true if the game has finished
     */
    public boolean isGameFinished() {
        return performance.isGameFinished();
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
    public boolean isNextSignalClear(Train train) {
        EdgeLocation location = train.getLocation().orElseThrow();
        double edgeLimitDistance = train.getStopDistance();
        do {
            Direction direction = location.getDirection();
            Edge edge = direction.getEdge();
            edgeLimitDistance -= location.getDistance();
            if (edgeLimitDistance <= 0) {
                // current edge shorter than limit distance (clear track)
                return true;
            }
            // current edge longer then limit distance
            if (!isNextRouteClear(direction)) {
                // next route not clear (not clear)
                return false;
            }
            if (edge instanceof Platform && train.isUnloaded()) {
                // current edge is platform and train is not loaded (not clear)
                return false;
            }
            // Find next direction
            location = getExit(direction)
                    .map(d -> new EdgeLocation(d, d.getEdge().getLength()))
                    .orElse(null);
        } while (location != null);
        // exit found
        return true;
    }

    /**
     * Returns true if the section containing the edge is clear (no transiting train) and no trains is transiting in the crossing sections
     *
     * @param edge the edge
     */
    public boolean isSectionClear(Edge edge) {
        // Searches section
        return getSection(edge)
                // Filter for train in the section
                .filter(section -> getTrain(section).isEmpty())
                .filter(section ->
                        // Filter for train in crossing sections
                        section.getCrossingSections().stream()
                                .noneMatch(crossSection -> getTrain(crossSection).isPresent())
                )
                .isPresent();
    }

    /**
     * Returns true if the section is locked.
     * <p>
     * The section is locked if it is not clear or any terminals is locked or if any crossing section is not clear
     * </p>
     *
     * @param edge the session edge
     */
    public boolean isSectionLocked(Edge edge) {
        if (!isSectionClear(edge)) {
            // Section not clear
            return true;
        }
        Optional<Section> sectionOpt = getSection(edge);
        if (sectionOpt.isEmpty()) {
            // Session does not exit
            return true;
        }
        Section section = sectionOpt.orElseThrow();
        Direction exit0 = section.getExit0();
        Route route0 = getRoute(exit0.getOrigin());
        if (route0 instanceof Signal && ((Signal) route0).isExitLocked(exit0)) {
            // Signals locked
            return true;
        }
        Direction exit1 = section.getExit1();
        Route route1 = getRoute(exit1.getOrigin());
        if (route1 instanceof Signal && ((Signal) route1).isExitLocked(exit1)) {
            // Signals locked
            return true;
        }
        // Searches for any train in crossing sections
        return section.getCrossingSections().stream()
                .anyMatch(s ->
                        getTrain(s).isPresent());
    }

    /**
     * Returns true if the section containing the edge has transiting train
     *
     * @param section the section
     */
    private boolean isSectionWithTrain(Section section) {
        return getTrain(section).isPresent();
    }

    /**
     * Returns true if the section containing the edge has transiting train
     *
     * @param edge the edge
     */
    public boolean isSectionWithTrain(Edge edge) {
        // Searches section
        return getSection(edge)
                // Filter for train in the section
                .filter(this::isSectionWithTrain)
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
     * Returns the station status with section locked
     *
     * @param section the section
     */
    StationStatus lock(Section section) {
        HashSet<Route> newRoutes = new HashSet<>(getRoutes());
        Stream.of(section.getExit0(), section.getExit1())
                .flatMap(dir -> {
                    // Finds the route terminal of section
                    Route route = getRoute(dir.getOrigin());
                    // Find the entry direction into the section
                    Optional<Direction> entryOpt = route.getExit(dir.opposite()).map(Direction::opposite);
                    return entryOpt.map(entry -> Tuple2.of(route, entry)).stream();
                })
                .filter(t -> t._1 instanceof Signal)
                .map(t -> ((Signal) t._1).lock(t._2))
                .forEach(signal -> {
                    newRoutes.remove(signal);
                    newRoutes.add(signal);
                });
        return setRoutes(newRoutes);
    }

    /**
     * Returns the station status with section locked
     *
     * @param id the section identifier
     */
    public StationStatus lockSection(String id) {
        return getSections().stream()
                .filter(section -> section.getId().equals(id))
                .findAny()
                .map(this::lock)
                .orElse(this);
    }

    /**
     * Returns the station status with signal locked
     *
     * @param id     the signal identifier
     * @param edgeId the entry edge
     */
    public StationStatus lockSignal(String id, String edgeId) {
        Signal signal = getRoute(id);
        Edge edge = stationMap.getEdge(edgeId);
        return signal.getValidEntries().stream()
                .filter(dir -> dir.getEdge().equals(edge))
                .findAny()
                .flatMap(entryDir ->
                        !signal.isLocked(entryDir)
                                ? Optional.of(signal.lock(entryDir))
                                : Optional.empty())
                .map(newSignal ->
                        setRoutes(getRoutes().stream().
                                map(route -> route.equals(signal)
                                        ? newSignal : route)
                                .collect(Collectors.toList())))
                .orElse(this);
    }

    /**
     * Returns the status with all signals locked
     */
    public StationStatus lockSignals() {
        List<Route> newRoutes = routes.stream()
                .map(route -> {
                    if (route instanceof Signal) {
                        Signal signal = (Signal) route;
                        Signal newSignal = signal;
                        for (Direction entry : signal.getValidEntries()) {
                            newSignal = newSignal.lock(entry);
                        }
                        return newSignal;
                    } else {
                        return route;
                    }
                }).collect(Collectors.toList());
        return setRoutes(newRoutes);
    }

    /**
     * Generates a sound event
     *
     * @param event the event
     */
    public void play(SoundEvent event) {
        if (events != null) {
            events.onNext(event);
        }
    }

    /**
     * Returns the station status with train reverted
     *
     * @param trainId the train identifier
     */
    public StationStatus revertTrain(String trainId) {
        return getTrain(trainId).map(train -> {
                    Train.State state = train.getState();
                    if (state.equals(Train.STATE_WAITING_FOR_RUN) || state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
                        Train newTrain = train.getLocation()
                                .map(EdgeLocation::opposite)
                                .flatMap(location -> getLocationAt(location, train.getLength()))
                                .map(train::setLocation)
                                .map(Train::run)
                                .orElse(train);
                        List<Train> newTrains = trains.stream().map(train1 ->
                                        train.equals(train1) ? newTrain : train1)
                                .collect(Collectors.toList());
                        play(SoundEvent.LEAVING);
                        return setTrains(newTrains);
                    } else {
                        return this;
                    }
                })
                .orElse(this);
    }

    /**
     * Returns the status with autolock set
     *
     * @param autolock true if autolock
     */
    public StationStatus setAutoLock(boolean autolock) {
        return autolock != this.autolock
                ? new StationStatus(stationMap, routes, trains, autolock, trainFrequency, performance, events, null, null, null, null, null, null, null, null, null)
                : this;
    }

    /**
     * Returns the status with the train started
     *
     * @param trainId the train identifier
     */
    public StationStatus startTrain(String trainId) {
        return getTrain(trainId).map(train -> {
                    Train.State state = train.getState();
                    if (state.equals(Train.STATE_WAITING_FOR_RUN) || state.equals(Train.STATE_BRAKING)) {
                        List<Train> newTrains = trains.stream().map(train1 ->
                                        train.equals(train1)
                                                ? train.run()
                                                : train1)
                                .collect(Collectors.toList());
                        play(SoundEvent.LEAVING);
                        return setTrains(newTrains);
                    } else {
                        return this;
                    }
                })
                .orElse(this);
    }

    /**
     * Returns the status with train stopped
     *
     * @param trainId the train identifier
     */
    public StationStatus stopTrain(String trainId) {
        return getTrain(trainId).map(train -> {
                    Train.State state = train.getState();
                    if (state.equals(STATE_RUNNING) ||
                            state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
                        play(SoundEvent.BRAKING);
                        List<Train> newTrains = trains.stream().map(train1 ->
                                        train.equals(train1)
                                                ? train.brake()
                                                : train1)
                                .collect(Collectors.toList());
                        return setTrains(newTrains);
                    } else {
                        return this;
                    }
                })
                .orElse(this);
    }

    /**
     * Returns the status with all trains braking
     */
    public StationStatus stopTrains() {
        List<Train> newTrains = trains.stream()
                .map(train -> {
                    State state = train.getState();
                    if (STATE_RUNNING.equals(state)
                            || STATE_WAITING_FOR_SIGNAL.equals(state)) {
                        return train.brake();
                    } else {
                        return train;
                    }
                }).collect(Collectors.toList());
        return setTrains(newTrains);
    }

    /**
     * Returns the next status simulating the time elapsed interval
     *
     * @param dt     the time interval (s)
     * @param random the random generator
     */
    public StationStatus tick(double dt, Random random) {
        SimulationContext ctx = new SimulationContext(this);
        List<Tuple2<Optional<Train>, Performance>> transitions = getTrains().stream()
                .map(t -> t.tick(ctx, dt))
                .collect(Collectors.toList());
        Performance performance = Performance.sumIterable(
                transitions.stream()
                        .map(Tuple2::getV2)
                        .collect(Collectors.toList()));
        List<Train> newTrains = transitions.stream()
                .flatMap(t -> t._1.stream())
                .collect(Collectors.toCollection(ArrayList::new));
        int trainNumber = newTrains.size();
        newTrains = createNewTrains(newTrains, trainFrequency * dt, random);
        int incomingTrainNumber = newTrains.size() - trainNumber;
        ExtendedPerformance newPerformance = this.performance.addTrainIncomingNumber(incomingTrainNumber)
                .add(performance)
                .setElapsedTime(this.performance.getElapsedTime() + dt);

        return ctx.getStatus()
                .setTrains(newTrains)
                .setPerformance(newPerformance);
    }

    /**
     * Returns the station status with switch toggled
     *
     * @param id the switch identifier
     */
    public StationStatus toggleDoubleSlipSwitch(String id) {
        DoubleSlipSwitch route = getRoute(id);
        Edge entry0 = route.getNodes().get(0).getEdges().get(0);
        Edge entry2 = route.getNodes().get(2).getEdges().get(0);
        if (isSectionWithTrain(entry0) || isSectionWithTrain(entry2)) {
            return this;
        }
        DoubleSlipSwitch newRoute = route.isThrough() ? route.diverging() : route.through();
        List<Route> newRoutes = getRoutes().stream()
                .map(route1 -> route1.equals(route) ? newRoute : route1)
                .collect(Collectors.toList());
        StationStatus stationStatus = setRoutes(newRoutes);
        play(SoundEvent.SWITCH);
        return stationStatus.isConsistent() ? stationStatus : this;
    }

    /**
     * Returns the station status with switch toggled
     *
     * @param id the switch identifier
     */
    public StationStatus toggleSwitch(String id) {
        Switch route = getRoute(id);
        Edge entryEdge = route.getNodes().get(0).getEdges().get(0);
        if (!isSectionClear(entryEdge)) {
            return this;
        }
        Switch newRoute = route.isThrough() ? route.diverging() : route.through();
        List<Route> newRoutes = getRoutes().stream()
                .map(route1 -> route1.equals(route) ? newRoute : route1)
                .collect(Collectors.toList());
        play(SoundEvent.SWITCH);
        return setRoutes(newRoutes);
    }

    /**
     * Returns the station status with section unlocked
     *
     * @param section the section
     */
    StationStatus unlock(Section section) {
        HashSet<Route> newRoutes = new HashSet<>(getRoutes());
        Stream.of(section.getExit0(), section.getExit1())
                .flatMap(dir -> {
                    // Finds the route terminal of section
                    Route route = getRoute(dir.getOrigin());
                    // Find the entry direction into the section
                    Optional<Direction> entryOpt = route.getExit(dir.opposite()).map(Direction::opposite);
                    return entryOpt.map(entry -> Tuple2.of(route, entry)).stream();
                })
                .filter(t -> t._1 instanceof Signal)
                .map(t -> ((Signal) t._1).unlock(t._2))
                .forEach(signal -> {
                    newRoutes.remove(signal);
                    newRoutes.add(signal);
                });
        return setRoutes(newRoutes);
    }

    /**
     * Returns the station status with section unlocked
     *
     * @param id the section identifier
     */
    public StationStatus unlockSection(String id) {
        return getSections().stream()
                .filter(section -> section.getId().equals(id))
                .findAny()
                .map(this::unlock)
                .orElse(this);
    }

    /**
     * Returns the station status with signal unlocked
     *
     * @param id     the signal identifier
     * @param edgeId the entry edge
     */
    public StationStatus unlockSignal(String id, String edgeId) {
        Signal signal = getRoute(id);
        Edge edge = stationMap.getEdge(edgeId);
        return signal.getValidEntries().stream()
                .filter(dir -> dir.getEdge().equals(edge))
                .findAny()
                .flatMap(entryDir ->
                        signal.isLocked(entryDir)
                                ? Optional.of(signal.unlock(entryDir))
                                : Optional.empty())
                .map(newSignal ->
                        setRoutes(getRoutes().stream().
                                map(route -> route.equals(signal)
                                        ? newSignal : route)
                                .collect(Collectors.toList())))
                .orElse(this);
    }

    /**
     * Creates the station status by the station map, route builders and no trains
     * The builders are a tuple of node identifier array and function that creates the route by node array
     */
    public static class Builder {
        private final StationMap stationMap;
        private final List<Tuple2<String[], Function<Node[], ? extends Route>>> builders;
        private final double trainFrequency;
        private final double gameDuration;
        private final Random random;
        private final Subscriber<SoundEvent> events;

        /**
         * Creates the status builder
         *
         * @param stationMap     the station map
         * @param trainFrequency the train frequency
         * @param gameDuration   the game duration
         * @param random         the random number generator
         * @param events         the event subscriber
         */
        public Builder(StationMap stationMap, double trainFrequency, double gameDuration, Random random, Subscriber<SoundEvent> events) {
            this.stationMap = requireNonNull(stationMap);
            this.trainFrequency = trainFrequency;
            this.gameDuration = gameDuration;
            this.random = random;
            this.events = events;
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
         * Returns the station status by the station map, route builders and trains (if random generator set)
         */
        public StationStatus build() {
            List<Route> routes1 = builders.stream()
                    .map(t -> {
                        Node[] nodes = Arrays.stream(t._1)
                                .map(stationMap::getNode)
                                .toArray(Node[]::new);
                        return t._2.apply(nodes);
                    }).collect(Collectors.toList());
            StationStatus stationStatus = StationStatus.create(stationMap, routes1, gameDuration, List.of(), 0, trainFrequency, events);
            if (random != null) {
                List<Train> trains = new ArrayList<>();
                for (Entry entry : stationStatus.getEntries()) {
                    trains.add(stationStatus.createNewTrain(trains, random, entry));
                }
                stationStatus = stationStatus.setTrains(trains).addIncomingTrains(trains.size());
            }
            return stationStatus;
        }
    }

}
