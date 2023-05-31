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

import java.awt.geom.Point2D;

import static java.lang.Math.PI;

public interface MathUtils {

    double RAD90 = PI / 2;
    double RAD_90 = -RAD90;
    double RAD180 = PI;
    double RAD_180 = -PI;
    double RAD360 = 2 * PI;
    double RAD_360 = -RAD360;

    /**
     * Returns the normalized angle (DEG)
     *
     * @param x angle (DEG)
     */
    static double normalizeDeg(double x) {
        while (x < 180) {
            x += 360;
        }
        while (x >= 180) {
            x -= 360;
        }
        return x;
    }

    /**
     * Returns the normalized angle (DEG)
     *
     * @param x angle (DEG)
     */
    static int normalizeDeg(int x) {
        while (x < 180) {
            x += 360;
        }
        while (x >= 180) {
            x -= 360;
        }
        return x;
    }

    /**
     * Returns the normalized angle (RAD)
     *
     * @param x angle (RAD)
     */
    static double normalizeRad(double x) {
        while (x < RAD_180) {
            x += RAD360;
        }
        while (x >= RAD180) {
            x += RAD_360;
        }
        return x;
    }

    /**
     * Returns the rounded value
     *
     * @param value     the value
     * @param precision the precision
     */
    static double round(double value, double precision) {
        return Math.round(value / precision) * precision;
    }

    /**
     * Returns the point snaped to the nearest value
     *
     * @param point     point
     * @param precision the precision
     */
    static Point2D snap(Point2D point, double precision) {
        return new Point2D.Double(
                round(point.getX(), precision),
                round(point.getY(), precision)
        );
    }
}
