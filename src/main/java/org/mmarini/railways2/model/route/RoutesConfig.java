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

package org.mmarini.railways2.model.route;

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.OrientedLocation;
import org.mmarini.railways2.model.geometry.StationMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Traverses the routes of station
 */
public class RoutesConfig {
    /**
     * Returns the Route config from station map and route builders
     *
     * @param stationMap the station map
     * @param builders   the route builder
     */
    public static RoutesConfig create(StationMap stationMap, Map<String, Function<Node, Route>> builders) {
        Map<String, Route> routes1 = Tuple2.stream(builders)
                .map(t -> t.setV2(
                        t._2.apply(
                                stationMap.getNode(t._1)))).collect(Tuple2.toMap());
        return new RoutesConfig(routes1);
    }

    private final Map<String, Route> routes;
    private Collection<Section> sections;
    private Map<Edge, Section> sectionByEdge;

    /**
     * Creates the route configuration
     *
     * @param routes the routes map
     */
    public RoutesConfig(Map<String, Route> routes) {
        this.routes = routes;
    }

    /**
     * Creates the sections
     */
    private Set<Section> createSections() {
        Set<Section> sections = new HashSet<>();
        Set<RouteDirection> fringe = routes.values().stream()
                .filter(r -> r instanceof SectionTerminal)
                .flatMap(Route::getValidDirections).collect(Collectors.toSet());
        while (!fringe.isEmpty()) {
            RouteDirection dir = fringe.iterator().next();
            fringe.remove(dir);
            Section section = findSection(dir);
            sections.add(section);
            fringe.removeAll(section.getTerminals());
        }
        return sections;
    }

    /**
     * Returns the section from a terminal
     *
     * @param terminal the terminal
     */
    Section findSection(RouteDirection terminal) {
        Set<Edge> edges = new HashSet<>();
        Set<RouteDirection> terminals = new HashSet<>();
        Set<RouteDirection> fringe = new HashSet<>();
        fringe.add(terminal);

        if (terminal.getRoute() instanceof SectionTerminal) {
            terminals.add(terminal);
        }
        while (!fringe.isEmpty()) {
            // Get eligible route and remove from fringe
            RouteDirection dir = fringe.iterator().next();
            fringe.remove(dir);
            dir.getLocation().ifPresent(point -> {
                Edge edge = point.getEdge();
                if (!edges.contains(edge)) {
                    // If edge point exits and the edge not yet traversed
                    // Add the edge
                    edges.add(edge);
                    // Find the terminal route of edge
                    getOppositeDirection(dir).ifPresent(opposite -> {
                        Route route = opposite.getRoute();
                        if (route instanceof SectionTerminal) {
                            terminals.add(opposite);
                        } else {
                            // Add all valid direction with edge not yet traversed
                            route.getValidDirections().filter(d -> d.getLocation()
                                            .map(OrientedLocation::getEdge)
                                            .stream()
                                            .noneMatch(edges::contains)
                                    )
                                    .forEach(fringe::add);
                        }
                    });
                }
            });
        }
        return Section.create(terminals, edges);
    }

    /**
     * Returns the terminal route direction of section from a location in the section
     *
     * @param location the section location
     */
    Optional<RouteDirection> findSectionTerminal(OrientedLocation location) {
        while (location != null) {
            Route term = getRoute(location.getTerminal().getId());
            int index = term.indexOf(location.getEdge());
            if (term instanceof SectionTerminal) {
                return term.getDirection(index);
            }
            location = term.getConnectedDirection(index)
                    .flatMap(RouteDirection::getLocation)
                    .orElse(null);
        }
        return Optional.empty();
    }

    /**
     * Returns the opposite direction of route direction.
     * The opposite direction is the direction from the terminal route
     *
     * @param direction the direction
     */
    Optional<RouteDirection> getOppositeDirection(RouteDirection direction) {
        return direction.getTerminal()
                .map(Node::getId)
                .<Route>map(this::getRoute)
                .flatMap(terminal -> direction.getLocation()
                        .map(OrientedLocation::getEdge)
                        .map(terminal::indexOf)
                        .map(index -> new RouteDirection(terminal, index)));
    }

    /**
     * Returns the route by identifier
     *
     * @param id the identifier
     * @throws IllegalArgumentException if identifier does not exit
     */
    public <T extends Route> T getRoute(String id) {
        T route = (T) routes.get(id);
        if (route == null) {
            throw new IllegalArgumentException(format("Route %s not found", id));
        }
        return route;
    }

    /**
     * Returns the routes map
     */
    public Map<String, Route> getRoutes() {
        return routes;
    }

    /**
     * Returns the section for an edge
     *
     * @param edge the edge
     */
    public Optional<Section> getSection(Edge edge) {
        return Optional.ofNullable(getSectionByEdge().get(edge));
    }

    public Map<Edge, Section> getSectionByEdge() {
        if (sectionByEdge == null) {
            sectionByEdge = getSections().stream()
                    .flatMap(section -> section.getEdges().stream()
                            .map(edge -> Tuple2.of(edge, section)))
                    .collect(Tuple2.toMap());
        }
        return sectionByEdge;
    }

    /**
     * Returns the set of sections
     */
    public Collection<Section> getSections() {
        if (sections == null) {
            sections = createSections();
        }
        return sections;
    }

    /**
     * Returns the terminal direction of the oriented location
     *
     * @param location the location
     */
    public Optional<RouteDirection> getTerminalDirection(OrientedLocation location) {
        Route route = getRoute(location.getTerminal().getId());
        return route.getDirection(route.indexOf(location.getEdge()));
    }

    /**
     * Returns the route configuration with a changed route
     *
     * @param route the route
     */
    public RoutesConfig putRoute(Route route) {
        Map<String, Route> newRoutes = new HashMap<>(routes);
        newRoutes.put(route.getId(), route);
        return new RoutesConfig(newRoutes);
    }
}
