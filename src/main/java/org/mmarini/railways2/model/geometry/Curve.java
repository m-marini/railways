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

import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.function.BiFunction;

import static java.lang.Math.*;

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
        return (node0, node1) -> new Curve(id, node0, node1, angle);
    }

    private final double angle;
    private final double radius;
    private final double length;
    private final Point2D center;
    private final double angle0;

    /**
     * Create the edge
     *
     * @param id    the edge identifier
     * @param node0 the first node
     * @param node1 the second node
     * @param angle the angle (RAD)
     */
    public Curve(String id, Node node0, Node node1, double angle) {
        super(id, node0, node1);
        this.angle = angle;
        Point2D pa = node0.getLocation();
        Point2D pb = node1.getLocation();
        double xa = pa.getX();
        double ya = pa.getY();
        double xb = pb.getX();
        double yb = pb.getY();
        this.radius = pa.distance(pb) / sin(abs(angle) / 2) / 2;
        this.length = radius * abs(angle);
        double ratio = cos(angle / 2) / sin(angle / 2) / 2;
        double x0 = (xa + xb) / 2 + (ya - yb) * ratio;
        double y0 = (ya + yb) / 2 + (xb - xa) * ratio;
        this.center = new Point2D.Double(x0, y0);
        this.angle0 = atan2(ya - y0, xa - x0);
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
     * Returns the angle at node0
     */
    double getAngle0() {
        return angle0;
    }

    public Point2D getCenter() {
        return center;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public Point2D getLocation(boolean direct, double distance) {
        double da = (direct ? distance : (length - distance)) / radius;
        double a = angle0 + (angle > 0 ? da : -da);
        return new Point2D.Double(center.getX() + radius * cos(a), center.getY() + radius * sin(a));
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), angle);
    }
}
