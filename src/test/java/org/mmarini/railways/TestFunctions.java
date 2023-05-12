/*
 * MIT License
 *
 * Copyright (c) 2022 Marco Marini
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.mmarini.railways;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.EdgeLocation;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.routes.Section;

import java.awt.geom.Point2D;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;

public interface TestFunctions {
/*
    @Deprecated
    static Matcher<Object> locatedAt(Edge edge, boolean direct, double distance) {
        return allOf(
                isA(OrientedLocation.class),
                hasProperty("edge", equalTo(edge)),
                hasProperty("direct", equalTo(direct)),
                hasProperty("distance", closeTo(distance, 10e-3))
        );
    }


 */

    static <T> Matcher<Optional<T>> emptyOptional() {
        return equalTo(Optional.empty());
    }

    static Matcher<Object> locatedAt(Edge edge, Node destination, double distance) {
        return allOf(
                isA(EdgeLocation.class),
                hasProperty("direction", allOf(
                        hasProperty("edge", equalTo(edge)),
                        hasProperty("destination", equalTo(destination)))),
                hasProperty("distance", closeTo(distance, 10e-3))
        );
    }

    static <T> Matcher<Optional<T>> optionalContaining(Matcher<T> exp) {
        requireNonNull(exp);
        return new CustomMatcher<>(format("Optional containing  %s",
                exp)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Optional
                        && ((Optional<T>) o).isPresent()
                        && exp.matches(((Optional<T>) o).orElseThrow());
            }
        };
    }

    static <T> Matcher<Optional<T>> optionalContaining(T exp) {
        return optionalContaining(equalTo(exp));
    }

    static Matcher<Point2D> pointCloseTo(double x, double y, double epsilon) {
        return pointCloseTo(new Point2D.Double(x, y), epsilon);
    }

    static Matcher<Point2D> pointCloseTo(Point2D expected, double epsilon) {
        requireNonNull(expected);
        return new CustomMatcher<>(format("Point close to %s within +- %f",
                expected,
                epsilon)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Point2D
                        && ((Point2D) o).distance(expected) <= epsilon;
            }
        };
    }

    /*
    @Deprecated
    static Matcher<Object> section(RouteDirection terminal0, RouteDirection terminal1, Edge... edges) {
        return allOf(
                isA(org.mmarini.railways1.model.routes.Section.class),
                anyOf(
                        allOf(
                                hasProperty("terminal0", equalTo(Optional.ofNullable(terminal0))),
                                hasProperty("terminal1", equalTo(Optional.ofNullable(terminal1)))
                        ),
                        allOf(
                                hasProperty("terminal0", equalTo(Optional.ofNullable(terminal1))),
                                hasProperty("terminal1", equalTo(Optional.ofNullable(terminal0)))
                        )
                ),
                hasProperty("edges", containsInAnyOrder(edges))
        );
    }
*/

    static Matcher<Object> section(Direction terminal0, Direction terminal1, Edge... edges) {
        return allOf(
                isA(Section.class),
                anyOf(
                        allOf(
                                hasProperty("exit0", equalTo(terminal0)),
                                hasProperty("exit1", equalTo(terminal1))
                        ),
                        allOf(
                                hasProperty("exit0", equalTo(terminal1)),
                                hasProperty("exit1", equalTo(terminal0))
                        )
                ),
                hasProperty("edges", containsInAnyOrder(edges))
        );
    }

    static String text(String... lines) {
        return String.join("\n", lines) + "\n";
    }
}
