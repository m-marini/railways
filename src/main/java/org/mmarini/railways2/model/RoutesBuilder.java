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

package org.mmarini.railways2.model;

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create default routes using station map
 */
public class RoutesBuilder {
    private final StationMap stationMap;
    private final List<Tuple2<String[], Function<Node[], ? extends Route>>> builders;

    /**
     * Creates the route builder
     *
     * @param stationMap the station map
     */
    public RoutesBuilder(StationMap stationMap) {
        this.stationMap = stationMap;
        this.builders = new ArrayList<>();
    }

    /**
     * Returns the builder with a new route
     *
     * @param builder the route builder
     * @param nodes   the nodes
     */
    public RoutesBuilder addRoute(Function<Node[], ? extends Route> builder, String... nodes) {
        builders.add(Tuple2.of(nodes, builder));
        return this;
    }

    /**
     * Returns the route list
     */
    public List<Route> build() {
        return builders.stream()
                .map(t -> {
                    Node[] nodes = Arrays.stream(t._1)
                            .map(stationMap::getNode)
                            .toArray(Node[]::new);
                    return t._2.apply(nodes);
                }).collect(Collectors.toList());
    }
}
