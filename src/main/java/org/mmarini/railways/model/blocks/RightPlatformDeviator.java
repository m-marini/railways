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

package org.mmarini.railways.model.blocks;

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.util.List;

import static org.mmarini.railways.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways.model.RailwayConstants.TRACK_GAP;

/**
 * Describes the left deviator
 */
public class RightPlatformDeviator extends AbstractBlock {
    private static final List<String> CONNECTIONS = List.of(
            "nw",
            "sw",
            "ne"
    );
    private static final int[] ORIENTATIONS = new int[]{0, 0, 180 - 15};
    private static final Point2D[] LOCATIONS = new Point2D[]{
            new Point2D.Double(),
            new Point2D.Double(0, TRACK_GAP),
            new Point2D.Double(2 * SEGMENT_LENGTH, -TRACK_GAP / 2)
    };

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static RightPlatformDeviator create(JsonNode root, Locator locator, String id) {
        return new RightPlatformDeviator(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    public RightPlatformDeviator(String id) {
        super(id);
    }

    @Override
    public int indexOf(String text) {
        return CONNECTIONS.indexOf(text);
    }

    @Override
    public Point2D location(int index) {
        return LOCATIONS[index];
    }

    @Override
    public int orientation(int index) {
        return ORIENTATIONS[index];
    }
}
