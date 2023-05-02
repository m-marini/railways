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
import org.junit.jupiter.api.Test;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mmarini.railways.TestFunctions.text;

class StationBuilderTest {

    public static final String DOC = text("---",
            "name: station",
            "orientation: 10",
            "blocks:",
            "  Platform:",
            "    class: org.mmarini.railways.model.blocks.Platforms",
            "    numPlatforms: 8",
            "    length: 10",
            "links: ",
            "  -",
            "    - Platform.1",
            "    - Platform.2",
            "  -",
            "    - Platform.3",
            "    - Platform.4");

    static StationBuilder createBuilder(String doc) throws IOException {
        JsonNode root = Utils.fromText(doc);
        return new StationBuilder(root, Locator.root());
    }

    @Test
    void createBlocks() throws IOException {
        StationBuilder builder = createBuilder(DOC);
        builder.createBlocks();
        assertThat(builder.blocks, hasEntry(
                equalTo("Platform"),
                allOf(
                        isA(Platforms.class),
                        hasProperty("id", equalTo("Platform")),
                        hasProperty("numPlatforms", equalTo(8)),
                        hasProperty("length", equalTo(10))
                )));
    }

    @Test
    void loadLinks() throws IOException {
        StationBuilder builder = createBuilder(DOC);
        builder.loadLinks();
        assertThat(builder.links, hasEntry("Platform.1", "Platform.2"));
        assertThat(builder.links, hasEntry("Platform.3", "Platform.4"));
    }

    @Test
    void validate() throws IOException {
        StationBuilder builder = createBuilder(DOC);
        builder.validate();
        assertThat(builder.blocks, hasEntry(
                equalTo("Platform"),
                allOf(
                        isA(Platforms.class),
                        hasProperty("id", equalTo("Platform")),
                        hasProperty("numPlatforms", equalTo(8)),
                        hasProperty("length", equalTo(10))
                )));
    }
}