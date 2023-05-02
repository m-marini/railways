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

package org.mmarini.railways2.model.geometry;

import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Class template with id and points attributes
 */
public abstract class AbstractEdge implements Edge {
    private final String id;
    private final Node node0;
    private final Node node1;

    /**
     * Create the edge
     *
     * @param id    the edge identifier
     * @param node0 the first node
     * @param node1 the second node
     */
    protected AbstractEdge(String id, Node node0, Node node1) {
        this.id = requireNonNull(id);
        this.node0 = requireNonNull(node0);
        this.node1 = requireNonNull(node1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractEdge that = (AbstractEdge) o;
        return id.equals(that.id) && node0.equals(that.node0) && node1.equals(that.node1);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Node getNode0() {
        return node0;
    }

    @Override
    public Node getNode1() {
        return node1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, node0, node1);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add(getId())
                .toString();
    }
}
