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

import org.mmarini.NotImplementedException;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import java.util.function.BiFunction;

import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;
import static org.mmarini.railways2.model.MathUtils.*;

/**
 * Connects two points for train transit with a curve
 */
public class Curve extends AbstractEdge {

    /**
     * Returns the curve builder
     *
     * @param id    the curve identifier
     * @param angle the angle (RAD)
     */
    public static BiFunction<Node, Node, Edge> builder(String id, double angle) {
        return (node0, node1) -> Curve.create(id, node0, node1, angle);
    }

    /**
     * Create the edge
     *
     * @param id    the edge identifier
     * @param node0 the first node
     * @param node1 the second node
     * @param angle the angle (RAD)
     */
    public static Curve create(String id, Node node0, Node node1, double angle) {
        requireNonNull(node0);
        requireNonNull(node1);
        Point2D pa = node0.getLocation();
        Point2D pb = node1.getLocation();
        double radius = pa.distance(pb) / sin(abs(angle) / 2) / 2;
        double length = radius * abs(angle);
        double xa = pa.getX();
        double ya = pa.getY();
        double xb = pb.getX();
        double yb = pb.getY();
        double ratio = cos(angle / 2) / sin(angle / 2) / 2;
        double x0 = (xa + xb) / 2 + (ya - yb) * ratio;
        double y0 = (ya + yb) / 2 + (xb - xa) * ratio;
        Point2D center = new Point2D.Double(x0, y0);
        double angle0 = atan2(ya - y0, xa - x0);
        double[] xbounds = limitCos(angle0, angle);
        double[] ybounds = limitSin(angle0, angle);
        double xmin = x0 + xbounds[0] * radius;
        double xmax = x0 + xbounds[1] * radius;
        double ymin = y0 + ybounds[0] * radius;
        double ymax = y0 + ybounds[1] * radius;

        Rectangle2D bounds = new Rectangle2D.Double(xmin, ymin, xmax - xmin, ymax - ymin);
        // Ensures terminal points are in the bounds due to computational approximation
        bounds.add(pa);
        bounds.add(pb);
        return new Curve(id, node0, node1, angle, length, bounds, radius, center, angle0);
    }

    /**
     * Returns the limits of cos [min, max]
     *
     * @param a0 start angle (RAD) +/- PI
     * @param da delta angle (RAD) +/- 2 PI
     */
    static double[] limitCos(double a0, double da) {
        double a1 = a0 + da;
        double y0 = cos(a0);
        double y1 = cos(a1);
        double max = (a0 >= 0) &&
                (da <= 0 && a1 <= 0 ||
                        (da > 0 && a1 >= RAD360)) ||
                (a0 < 0 &&
                        (da >= 0 && a0 + da >= 0 ||
                                da < 0 && a0 + da <= RAD_360)) ?
                1 :
                max(y0, y1);
        double min = (a0 >= 0 &&
                (da >= 0 && a1 >= RAD180 ||
                        da < 0 && a1 <= RAD_180)) ||
                (a0 < 0 &&
                        (da <= 0 && a1 <= RAD_180 ||
                                da > 0 && a1 >= RAD180)) ?
                -1 :
                min(y0, y1);
        return new double[]{min, max};
    }

    /**
     * Returns the limits of sin [min, max]
     *
     * @param a0 start angle (RAD) +/- PI
     * @param da delta angle (RAD) +/- 2 PI
     */
    static double[] limitSin(double a0, double da) {
        return limitCos(normalizeRad(a0 - RAD90), da);
    }

    private final double radius;
    private final Point2D center;
    private final double angle0;
    private final double angle;

    /**
     * Create the edge
     *
     * @param id     the edge identifier
     * @param node0  the first node
     * @param node1  the second node
     * @param angle  the angle (RAD)
     * @param length the length of curve
     * @param bounds the bounds of curve
     * @param radius the curve radius
     * @param center the curve center
     * @param angle0 the angle of point0
     */
    public Curve(String id, Node node0, Node node1, double angle, double length, Rectangle2D bounds,
                 double radius, Point2D center, double angle0) {
        super(id, node0, node1, length, bounds);
        this.angle = angle;
        this.radius = radius;
        this.center = requireNonNull(center);
        this.angle0 = angle0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Curve curve = (Curve) o;
        return Double.compare(curve.angle, angle) == 0;
    }

    /**
     * Returns the angle of curve (RAD)
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Returns the angle of location in the curve (RAD)
     *
     * @param location the location
     */
    public double getAngle(EdgeLocation location) {
        Node destination = location.getDirection().getDestination();
        double distance = location.getDistance();
        double d0 = destination.equals(getNode0()) ?
                distance :
                (length - distance);
        double da = d0 / radius;
        return angle0 + (angle > 0 ? da : -da);
    }

    /**
     * Returns the angle at node0 (RAD)
     */
    public double getAngle0() {
        return angle0;
    }

    /**
     * Returns the center of curve
     */
    public Point2D getCenter() {
        return center;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public Point2D getLocation(EdgeLocation location) {
        double a = getAngle(location);
        return new Point2D.Double(center.getX() + radius * cos(a), center.getY() + radius * sin(a));
    }

    @Override
    public EdgeLocation getNearestLocation(Point2D point) { // TODO tests
        throw new NotImplementedException(); // TODO
    }

    @Override
    public double getOrientation(EdgeLocation location) {
        double da = location.getDirection().getOrigin().equals(node0) ?
                angle >= 0 ?
                        (length - location.getDistance()) / radius + RAD90 :
                        (-length + location.getDistance()) / radius + RAD_90 :
                angle >= 0 ?
                        location.getDistance() / radius + RAD_90 :
                        -location.getDistance() / radius + RAD90;
        return normalizeRad(angle0 + da);
    }

    /**
     * Returns the radius of curve
     */
    public double getRadius() {
        return radius;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), angle);
    }
}
