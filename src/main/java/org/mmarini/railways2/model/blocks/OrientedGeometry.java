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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import static java.lang.Math.toRadians;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.MathUtils.normalizeDeg;

/**
 * Defines a point and a direction in the point
 */
public class OrientedGeometry {
    /**
     * Returns the oriented geometry
     *
     * @param x           x coordinates (m)
     * @param y           y coordinates (m)
     * @param orientation orientation (DEG)
     */
    public static OrientedGeometry create(double x, double y, int orientation) {
        return new OrientedGeometry(new Point2D.Double(x, y), orientation);
    }

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
     * Returns the transformation from the block geometry to the world geometry
     * for the block geometry expressed in world geometry
     */
    public UnaryOperator<OrientedGeometry> getBlock2World() {
        AffineTransform tr = AffineTransform.getTranslateInstance(point.getX(), point.getY());
        tr.rotate(toRadians(orientation));
        return pointBlockGeo -> {
            int beta0 = normalizeDeg(orientation + pointBlockGeo.getOrientation());
            Point2D p0 = tr.transform(pointBlockGeo.getPoint(), null);
            return new OrientedGeometry(p0, beta0);
        };
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

    /**
     * Returns the world block geometry for this geometry block point given the world point geometry
     *
     * @param worldPointGeo the point world geometry
     */

    public OrientedGeometry getWorldBlockGeo(OrientedGeometry worldPointGeo) {
        /*
        Point2D p0 = worldPointGeo.getPoint();
        Point2D p1 = point;
        int alpha0 = worldPointGeo.getOrientation();
        int alpha1 = orientation;
        int alpha2 = normalizeDeg(alpha0 - alpha1);
        AffineTransform tr = AffineTransform.getTranslateInstance(p0.getX(), p0.getY());
        tr.rotate(toRadians(alpha2));
        tr.translate(-p1.getX(), -p1.getY());
        Point2D p2 = new Point2D.Double();
        tr.transform(p2, p2);
        return new OrientedGeometry(p2, alpha2);

         */
        Point2D p0 = worldPointGeo.getPoint();
        int alpha0 = worldPointGeo.getOrientation();
        int alpha1 = orientation;
        int blockOrientation = normalizeDeg(alpha0 - alpha1);
        AffineTransform tr = AffineTransform.getTranslateInstance(p0.getX(), p0.getY());
        tr.rotate(toRadians(blockOrientation));
        tr.translate(-point.getX(), -point.getY());
        Point2D blockLocation = new Point2D.Double();
        tr.transform(blockLocation, blockLocation);
        return new OrientedGeometry(blockLocation, blockOrientation);
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
