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

package org.mmarini.railways2.model.blocks;

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.MathUtils.normalizeDeg;

/**
 * Defines a point and a direction in the point
 */
public class OrientedGeometry {
    private final Point2D point;
    private final int orientation;

    /**
     * Creates the oriented geometry
     *
     * @param point       the point
     * @param orientation the orientating (DEG)
     */
    public OrientedGeometry(Point2D point, int orientation) {
        this.point = requireNonNull(point);
        this.orientation = orientation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrientedGeometry that = (OrientedGeometry) o;
        return orientation == that.orientation && point.equals(that.point);
    }

    /**
     * Returns the orientation (DEG)
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Returns the point
     */
    public Point2D getPoint() {
        return point;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, orientation);
    }

    /**
     * Returns the opposite geometry
     */
    public OrientedGeometry opposite() {
        return new OrientedGeometry(point, normalizeDeg(orientation - 180));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrientedGeometry.class.getSimpleName() + "[", "]")
                .add("point=" + point)
                .add("orientation=" + orientation)
                .toString();
    }
}
