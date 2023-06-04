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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mmarini.Tuple2;

import java.awt.geom.Point2D;
import java.util.Optional;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.mmarini.railways2.model.MathUtils.GRID_SIZE;
import static org.mmarini.railways2.model.MathUtils.round;

public interface Matchers {

    double SNAP_EPSILON = GRID_SIZE / 2;

    static <T> Matcher<Optional<T>> emptyOptional() {
        return equalTo(Optional.empty());
    }

    static <T> Matcher<Optional<? extends T>> optionalOf(Matcher<? extends T> exp) {
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

    static <T> Matcher<Optional<? extends T>> optionalOf(T exp) {
        return optionalOf(equalTo(exp));
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
            public void describeMismatch(Object item, Description description) {
                if (item instanceof Point2D) {
                    double distance = expected.distance((Point2D) item);
                    description.appendText("distance between ")
                            .appendValue(item)
                            .appendText(" and ")
                            .appendValue(expected)
                            .appendText(" = ")
                            .appendValue(distance)
                            .appendText(" > ")
                            .appendValue(SNAP_EPSILON);
                } else {
                    super.describeMismatch(item, description);
                }
            }

            @Override
            public boolean matches(Object o) {
                if (!(o instanceof Point2D)) return false;
                double distance = ((Point2D) o).distance(expected);
                boolean matched = distance <= epsilon;
                return matched;
            }
        };
    }

    static Matcher<Point2D> pointSnapTo(double x, double y) {
        double snapX = round(x, GRID_SIZE);
        double snapY = round(y, GRID_SIZE);
        return new CustomMatcher<>(format("Point snap to %.3f, %.3f",
                snapX, snapY)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Point2D
                        && abs(((Point2D) o).getX() - snapX) <= SNAP_EPSILON
                        && abs(((Point2D) o).getY() - snapY) <= SNAP_EPSILON;
            }
        };
    }

    static <T1, T2> Matcher<Tuple2<? extends T1, ? extends T2>> tupleOf(T1 value1, T2 value2) {
        return tupleOf(equalTo(value1), equalTo(value2));
    }

    static <T1, T2> Matcher<Tuple2<? extends T1, ? extends T2>> tupleOf(Matcher<? extends T1> value1, Matcher<? extends T2> value2) {
        requireNonNull(value1);
        requireNonNull(value2);
        return new CustomMatcher<>(format("Tuple containing  %s, %s",
                value1, value2)) {
            @Override
            public boolean matches(Object o) {
                return o instanceof Tuple2
                        && value1.matches(((Tuple2<T1, T2>) o)._1)
                        && value2.matches(((Tuple2<T1, T2>) o)._2);
            }
        };
    }
}
