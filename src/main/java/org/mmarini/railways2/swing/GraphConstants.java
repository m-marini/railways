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

package org.mmarini.railways2.swing;

import java.awt.*;

/**
 * Graphic constants
 */
public interface GraphConstants {

    /**
     * Coach width (m)
     */
    double COACH_WIDTH = 3;

    /**
     * Track gauge (m)
     */
    double TRACK_GAUGE = 1.435;

    Color PLATFORM_GREEN_COLOR = Color.getHSBColor(140f / 360f, 0.9f, 0.9f);
    Color PLATFORM_RED_COLOR = Color.getHSBColor(343f / 360, 0.93f, 0.75f);
    Color TRACK_GREEN_COLOR = Color.getHSBColor(162f / 360f, 0.9f, 1f);
    Color TRACK_RED_COLOR = Color.getHSBColor(343f / 360, 0.93f, 1f);
    Color TRAIN_COLOR = Color.getHSBColor(53f / 360f, .80f, 1f);
}
