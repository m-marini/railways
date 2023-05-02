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

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.railways.TestFunctions.text;
import static org.mmarini.yaml.Utils.createObject;
import static org.mmarini.yaml.Utils.fromText;

class CurvesTest {

    public static final String DOC = text("---",
            "class: org.mmarini.railways.model.blocks.Curves",
            "numTracks: 2",
            "angle: 30");

    @Test
    void create() throws IOException {
        Block block = createObject(fromText(DOC), Locator.root(),
                new Object[]{"name"}, new Class[]{String.class});
        assertThat(block, isA(Curves.class));
        assertThat(block, hasProperty("id", equalTo("name")));
        assertThat(block, hasProperty("numTracks", equalTo(2)));
        assertThat(block, hasProperty("angle", equalTo(30)));
    }

    @Test
    void indexOf() {
        Curves block = new Curves("id", 2, 30);
        assertEquals(0, block.indexOf("e.1"));
        assertEquals(1, block.indexOf("w.1"));
        assertEquals(2, block.indexOf("e.2"));
        assertEquals(3, block.indexOf("w.2"));
        assertEquals(-1, block.indexOf("none"));
    }

}