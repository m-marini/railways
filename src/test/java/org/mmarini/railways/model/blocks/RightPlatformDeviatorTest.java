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

import org.junit.jupiter.api.Test;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.railways.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class RightPlatformDeviatorTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways.model.blocks.RightPlatformDeviator");

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(RightPlatformDeviator.class));
        assertThat(block, hasProperty("id", equalTo("name")));
    }

    @Test
    void indexOf() {
        RightPlatformDeviator block = new RightPlatformDeviator("id");
        assertEquals(0, block.indexOf("nw"));
        assertEquals(1, block.indexOf("sw"));
        assertEquals(2, block.indexOf("ne"));
        assertEquals(-1, block.indexOf("none"));
    }

    @Test
    void location() {
        RightPlatformDeviator block = new RightPlatformDeviator("id");
        assertEquals(new Point2D.Double(), block.location(0));
        assertEquals(new Point2D.Double(0, TRACK_GAP), block.location(1));
        assertEquals(new Point2D.Double(2 * SEGMENT_LENGTH, -TRACK_GAP / 2), block.location(2));
    }

    @Test
    void orientation() {
        RightPlatformDeviator block = new RightPlatformDeviator("id");
        assertEquals(0, block.orientation(0));
        assertEquals(0, block.orientation(1));
        assertEquals(180 - 15, block.orientation(2));
    }

}