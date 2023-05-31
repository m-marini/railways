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

import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * The edge builder
 */
public class EdgeBuilderParams {
    /**
     * Returns the curve builder
     *
     * @param id    the curve identifier
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     * @param angle the angle (RAD)
     */
    public static EdgeBuilderParams curve(String id, String node0, String node1, double angle) {
        return new EdgeBuilderParams(id, id1 -> Curve.builder(id1, angle), node0, node1);
    }

    /**
     * Returns the platform builder
     *
     * @param id    the platform identifier
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     */
    public static EdgeBuilderParams platform(String id, String node0, String node1) {
        return new EdgeBuilderParams(id, Platform::builder, node0, node1);
    }

    /**
     * Returns the track builder
     *
     * @param id    the track identifier
     * @param node0 the node0 identifier
     * @param node1 the node1 identifier
     */
    public static EdgeBuilderParams track(String id, String node0, String node1) {
        return new EdgeBuilderParams(id, Track::builder, node0, node1);
    }

    private final String id;
    private final Function<String, BiFunction<Node, Node, Edge>> edgeBuilderFunc;
    private final String node0;
    private final String node1;

    /**
     * Creates the edge builder
     *
     * @param id              the edge identifier
     * @param edgeBuilderFunc the edge builder function
     * @param node0           the node0 identifier
     * @param node1           the node1 identifier
     */
    protected EdgeBuilderParams(String id, Function<String, BiFunction<Node, Node, Edge>> edgeBuilderFunc, String node0, String node1) {
        this.id = requireNonNull(id);
        this.edgeBuilderFunc = requireNonNull(edgeBuilderFunc);
        this.node0 = requireNonNull(node0);
        this.node1 = requireNonNull(node1);
    }

    /**
     * Returns the builder function
     */
    public BiFunction<Node, Node, Edge> getBuilder() {
        return edgeBuilderFunc.apply(id);
    }

    /**
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the edge builder with different id
     *
     * @param id the identifier
     */
    public EdgeBuilderParams setId(String id) {
        return !this.id.equals(id) ?
                new EdgeBuilderParams(id, edgeBuilderFunc, node0, node1) : this;
    }

    /**
     * Returns the node0 identifier
     */
    public String getNode0() {
        return node0;
    }

    /**
     * Returns the edge builder with different node0
     *
     * @param node0 the node0
     */
    public EdgeBuilderParams setNode0(String node0) {
        return !this.node0.equals(node0) ?
                new EdgeBuilderParams(id, edgeBuilderFunc, node0, node1) : this;
    }

    /**
     * Returns the node1 identifier
     */
    public String getNode1() {
        return node1;
    }

    /**
     * Returns the edge builder with different node1
     *
     * @param node1 the node1
     */
    public EdgeBuilderParams setNode1(String node1) {
        return !this.node1.equals(node1) ?
                new EdgeBuilderParams(id, edgeBuilderFunc, node0, node1) : this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EdgeBuilderParams.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("node0='" + node0 + "'")
                .add("node1='" + node1 + "'")
                .toString();
    }
}
