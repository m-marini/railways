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

import java.util.Map;

import static java.lang.String.format;

/**
 * Describes the connection point of a block
 */
public class BlockConnection {
    /**
     * Returns the block name
     *
     * @param connection the connection string
     */
    public static String blockName(String connection) {
        int from = connection.indexOf(".");
        return from > 0 ? connection.substring(0, from) : null;
    }

    /**
     * Returns the block name
     *
     * @param connection the connection string
     */
    public static String connectionName(String connection) {
        int from = connection.indexOf(".");
        return from > 0 ? connection.substring(from + 1) : null;
    }

    /**
     * Returns the block connection
     *
     * @param connection the connection text
     * @param blocks     the block map
     */
    public static BlockConnection create(String connection, Map<String, Block> blocks) {
        String id = blockName(connection);
        if (id == null) {
            return null;
        }
        String linkId = connectionName(connection);
        if (linkId == null) {
            return null;
        }
        Block block = blocks.get(id);
        if (block == null) {
            throw new IllegalArgumentException(format("Block \"%s\" not found", id));
        }
        int index = block.indexOf(linkId);
        if (index < 0) {
            throw new IllegalArgumentException(format("Connection \"%s\" not found", connection));
        }
        return new BlockConnection(block, index);
    }

    private final Block block;
    private final int index;

    /**
     * Creates the block connection
     *
     * @param block the block
     * @param index the index
     */
    public BlockConnection(Block block, int index) {
        this.block = block;
        this.index = index;
    }

    /**
     * Returns the block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns the index
     */
    public int getIndex() {
        return index;
    }
}
