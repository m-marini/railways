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

package org.mmarini.railways2.model.blocks;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Defines a block point by block and id
 */
public class BlockPoint {
    private final Block block;
    private final String id;

    /**
     * Creates a block point
     *
     * @param block the block the id
     * @param id    the point identifier
     */
    public BlockPoint(Block block, String id) {
        this.block = requireNonNull(block);
        this.id = requireNonNull(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPoint that = (BlockPoint) o;
        return block.equals(that.block) && id.equals(that.id);
    }

    /**
     * Returns the block
     */
    public <T extends Block> T getBlock() {
        return (T) block;
    }

    /**
     * Returns the edge id of the block point
     */
    public String getEdgeId() {
        return block.getId() + "." + block.getEdgeId(id);
    }

    /**
     * Returns the geometry of point
     */
    OrientedGeometry getEntryGeometry() {
        return block.getEntryGeometry(id);
    }

    /**
     * Returns the point identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, id);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(block.getId())
                .append(".")
                .append(id)
                .toString();
    }
}
