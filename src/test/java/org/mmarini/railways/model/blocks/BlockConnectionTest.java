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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockConnectionTest {

    static Map<String, Block> createBlocks() {
        return Stream.of(
                new Platforms("platform", 10, 10),
                new Wayout("wayout")
        ).collect(Collectors.toMap(
                Block::getId,
                t -> t
        ));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a.b,a",
            "a,",
            ".a,",
    })
    void blockName(String text, String expected) {
        String actual = BlockConnection.blockName(text);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a.b.1,b.1",
            "a,",
            ".a,",
    })
    void connectionName(String text, String expected) {
        String actual = BlockConnection.connectionName(text);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "wayout.entry,wayout,0",
    })
    void create(String text, String blockId, int index) {
        Map<String, Block> blocks = createBlocks();
        BlockConnection result = BlockConnection.create(text, blocks);
        assertThat(result, hasProperty("block",
                hasProperty("id", equalTo(blockId))));
        assertThat(result, hasProperty("index", equalTo(index)));
    }
}