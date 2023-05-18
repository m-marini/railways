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

package org.mmarini.railways2.model.geometry;

import org.mmarini.Tuple2;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Defines the location an orientation of the train coaches
 * <p>
 * Each coach is defined by the center point (m) and the orientation (RAD)
 * </p>
 */
public class TrainComposition {
    /**
     * The empty train composition
     */
    public static final TrainComposition EMPTY = new TrainComposition(null, null, List.of());

    /**
     * Returns the empty train composition
     */
    public static TrainComposition empty() {
        return EMPTY;
    }

    private final Tuple2<Point2D, Double> head;
    private final List<Tuple2<Point2D, Double>> coaches;
    private final Tuple2<Point2D, Double> tail;

    /**
     * Creates the train composition.
     *
     * @param head    the head (null if not available)
     * @param tail    the tail (null if not available)
     * @param coaches the coaches list
     */
    public TrainComposition(Tuple2<Point2D, Double> head, Tuple2<Point2D, Double> tail, List<Tuple2<Point2D, Double>> coaches) {
        this.head = head;
        this.coaches = requireNonNull(coaches);
        this.tail = tail;
    }

    /**
     * Returns the list of coaches (the center point (m) and the orientation (RAD))
     */
    public List<Tuple2<Point2D, Double>> getCoaches() {
        return coaches;
    }

    /**
     * Returns the head of train (the center point (m) and the orientation (RAD))
     */
    public Optional<Tuple2<Point2D, Double>> getHead() {
        return Optional.ofNullable(head);
    }

    /**
     * Returns the tail of head (the center point (m) and the orientation (RAD))
     */
    public Optional<Tuple2<Point2D, Double>> getTail() {
        return Optional.ofNullable(tail);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TrainComposition.class.getSimpleName() + "[", "]")
                .add("head=" + head)
                .add("coaches=" + coaches)
                .add("tail=" + tail)
                .toString();
    }
}
