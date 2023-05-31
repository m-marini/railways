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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Junctions between two block points
 */
public class BlockJunction {
    /**
     * Returns the normalized junction
     *
     * @param a the block point
     * @param b the block point
     */
    public static BlockJunction create(BlockPoint a, BlockPoint b) {
        return a.toString().compareTo(b.toString()) <= 0 ? new BlockJunction(a, b) : new BlockJunction(b, a);
    }

    private final BlockPoint from;
    private final BlockPoint to;

    /**
     * Creates block junction
     *
     * @param from the source point
     * @param to   the target point
     */
    protected BlockJunction(BlockPoint from, BlockPoint to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
        if (from.toString().compareTo(to.toString()) > 0) {
            throw new IllegalArgumentException(format("%s must be greater then %s", from, to));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockJunction that = (BlockJunction) o;
        return from.equals(that.from) && to.equals(that.to);
    }

    /**
     * Returns the block point of the block
     *
     * @param block the block
     */
    public BlockPoint getByBlock(Block block) {
        if (from.getBlock().equals(block)) {
            return from;
        } else if (to.getBlock().equals(block)) {
            return to;
        } else {
            throw new IllegalArgumentException(format("block %s not in junction %s", block, this));
        }
    }

    /**
     * Returns the source point
     */
    public BlockPoint getFrom() {
        return from;
    }

    /**
     * Returns the other block
     *
     * @param self the self block
     */
    public BlockPoint getOther(BlockPoint self) {
        if (from.equals(self)) {
            return to;
        } else if (to.equals(self)) {
            return from;
        } else {
            throw new IllegalArgumentException(format("point %s not in junction %s", self, this));
        }
    }

    /**
     * Returns the target point
     */
    public BlockPoint getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(from)
                .append(" - ")
                .append(to).toString();
    }

}
