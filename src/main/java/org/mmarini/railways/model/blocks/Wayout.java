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

import static org.mmarini.railways.model.RailwayConstants.TRACK_GAP;

/**
 * Describes a way out (one entry and one exit)
 */
public class Wayout extends AbstractBlock {

    private static final Point2D[] LOCATIONS = new Point2D[]{
            new Point2D.Double(0, 0),
            new Point2D.Double(0, TRACK_GAP)
    };

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Wayout create(JsonNode root, Locator locator, String id) {
        return new Wayout(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    public Wayout(String id) {
        super(id);
    }

    @Override
    public int indexOf(String text) {
        switch (text) {
            case "entry":
                return 0;
            case "exit":
                return 1;
            default:
                return -1;
        }
    }

    @Override
    public Point2D location(int index) {
        return LOCATIONS[index];
    }

    @Override
    public int orientation(int index) {
        return -180;
    }
}
