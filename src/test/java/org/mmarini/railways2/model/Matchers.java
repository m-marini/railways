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

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.EdgeLocation;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.railways2.model.routes.Section;

import java.util.Arrays;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;

public interface Matchers {

    static Matcher<Section> hasSectionId(Matcher<String> id) {
        requireNonNull(id);
        return new CustomMatcher<>(format("Section %s", id)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Section
                        && id.matches(((Section) o).getId());
            }
        };
    }

    static Matcher<Direction> isDirection(String edgeId, String destinationId) {
        return isDirection(isEdge(edgeId), isNode(destinationId));
    }

    static Matcher<Direction> isDirection(Matcher<? extends Edge> edgeId, Matcher<Node> destinationId) {
        requireNonNull(edgeId);
        requireNonNull(destinationId);
        return new CustomMatcher<>(format("Direction %s to %s", edgeId, destinationId)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Direction
                        && edgeId.matches(((Direction) o).getEdge())
                        && destinationId.matches(((Direction) o).getDestination());
            }
        };
    }

    static <T extends Edge> Matcher<T> isEdge(Matcher<String> expId) {
        requireNonNull(expId);
        return new CustomMatcher<>(format("Edge %s", expId)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Edge
                        && expId.matches(((Edge) o).getId());
            }
        };
    }

    static <T extends Edge> Matcher<T> isEdge(String expId) {
        return isEdge(equalTo(expId));
    }

    static <T extends Node> Matcher<T> isNode(String expId) {
        return isNode(equalTo(expId));
    }

    static <T extends Node> Matcher<T> isNode(Matcher<String> expId) {
        requireNonNull(expId);
        return new CustomMatcher<>(format("Node %s", expId)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Node
                        && expId.matches(((Node) o).getId());
            }
        };
    }

    static <T extends Route> Matcher<T> isRoute(Matcher<String> expId) {
        requireNonNull(expId);
        return new CustomMatcher<>(format("Route %s", expId)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Route
                        && expId.matches(((Route) o).getId());
            }
        };
    }

    static <T extends Route> Matcher<T> isRoute(String expId) {
        return isRoute(equalTo(expId));
    }

    static Matcher<Section> isSectionWith(String edge0, String to0, String edge1, String to1, String... edges) {
        return isSectionWith(isDirection(edge0, to0), isDirection(edge1, to1),
                containsInAnyOrder(Arrays.stream(edges).map(Matchers::isEdge).toArray(Matcher[]::new)));
    }

    static Matcher<Section> isSectionWith(Matcher<Direction> terminal0, Matcher<Direction> terminal1, Matcher<Iterable<? extends Edge>> edges) {
        return allOf(
                sectionTerminateAt(terminal0),
                sectionTerminateAt(terminal1),
                sectionContaining(edges)
        );
    }

    static Matcher<EdgeLocation> locatedAt(Matcher<Direction> direction, Matcher<Double> distance) {
        requireNonNull(direction);
        requireNonNull(distance);
        return new CustomMatcher<>(format("Located at %s %s", direction, direction)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof EdgeLocation
                        && direction.matches(((EdgeLocation) o).getDirection())
                        && distance.matches(((EdgeLocation) o).getDistance());
            }
        };
    }

    static Matcher<EdgeLocation> locatedAt(String edge, String destination, double distance) {
        return locatedAt(isDirection(edge, destination), closeTo(distance, 10e-3));
    }

    static Matcher<Section> sectionContaining(Edge... edges) {
        return sectionContaining(containsInAnyOrder(edges));
    }

    static Matcher<Section> sectionContaining(Matcher<Iterable<? extends Edge>> edges) {
        requireNonNull(edges);
        return new CustomMatcher<>(format("Section containing %s", edges)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Section
                        && edges.matches(((Section) o).getEdges());
            }
        };
    }

    static Matcher<Section> sectionTerminateAt(String edge, String to) {
        return sectionTerminateAt(isDirection(edge, to));
    }

    static Matcher<Section> sectionTerminateAt(Matcher<Direction> terminal) {
        requireNonNull(terminal);
        return new CustomMatcher<>(format("Section at %s", terminal)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Section
                        && (terminal.matches(((Section) o).getExit0())
                        || terminal.matches(((Section) o).getExit1()));
            }
        };
    }
}
