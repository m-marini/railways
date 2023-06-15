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

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.EdgeBuilderParams;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.railways2.model.routes.Signal;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.schema.Validator.objectPropertiesRequired;
import static org.mmarini.yaml.schema.Validator.positiveInteger;

/**
 * Describes a list of semaphores identified by a name.
 */
public class Signals extends AbstractBlock {
    public static final double SIGNAL_GAP = 1;
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numSignals", positiveInteger()),
            List.of("numSignals")
    );

    /**
     * Returns the signals
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Signals create(JsonNode root, Locator locator, String id) {
        VALIDATOR.apply(locator).accept(root);
        int numSignals = locator.path("numSignals").getNode(root).asInt();
        return create(id, numSignals);
    }

    /**
     * Returns the signals
     *
     * @param id         the id of platform
     * @param numSignals the number of signals
     */
    public static Signals create(String id, int numSignals) {
        Map<String, OrientedGeometry> geometryById1 = IntStream.range(0, numSignals)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of(i + 1 + ".w", new OrientedGeometry(
                                new Point2D.Double(0, i * TRACK_GAP), 0)),
                        Tuple2.of(i + 1 + ".e", new OrientedGeometry(
                                new Point2D.Double(SIGNAL_GAP * 2, i * TRACK_GAP), -180))))
                .collect(Tuple2.toMap());
        List<NodeBuilderParams> innerPointById1 = IntStream.range(0, numSignals)
                .mapToObj(i -> NodeBuilderParams.create(
                        i + 1 + ".signal",
                        SIGNAL_GAP, i * TRACK_GAP,
                        i + 1 + ".trackw",
                        i + 1 + ".tracke"))
                .collect(Collectors.toList());
        List<EdgeBuilderParams> edgeBuilderParams = IntStream.range(0, numSignals)
                .boxed()
                .flatMap(i -> {
                    String suffixId = String.valueOf(i + 1);
                    return Stream.of(
                            EdgeBuilderParams.track(suffixId + ".trackw", suffixId + ".w", suffixId + ".signal"),
                            EdgeBuilderParams.track(suffixId + ".tracke", suffixId + ".signal", suffixId + ".e")
                    );
                }).collect(Collectors.toList());
        Map<String, String> edgeByBlockPoint = IntStream.range(0, numSignals)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of(i + 1 + ".w", i + 1 + ".trackw"),
                        Tuple2.of(i + 1 + ".e", i + 1 + ".tracke")
                )).collect(Tuple2.toMap());
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams = IntStream.range(0, numSignals)
                .mapToObj(i -> Tuple2.<Function<Node[], ? extends Route>, List<String>>of(Signal::create, List.of(i + 1 + ".signal")))
                .collect(Collectors.toList());
        return new Signals(id, geometryById1, innerPointById1, edgeBuilderParams, edgeByBlockPoint, innerRouteParams);
    }

    /**
     * Creates the list of semaphores
     *
     * @param id                the identifier
     * @param geometryById      the geometry by identifier
     * @param innerPointById    the inner point by identifier
     * @param edgeBuilderParams the edge builders
     * @param edgeByBlockPoint  the edge by block point
     * @param innerRouteParams  the inner route parameters
     */
    protected Signals(String id, Map<String, OrientedGeometry> geometryById, List<NodeBuilderParams> innerPointById, List<EdgeBuilderParams> edgeBuilderParams, Map<String, String> edgeByBlockPoint, List<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams) {
        super(id, geometryById, innerPointById, edgeBuilderParams, edgeByBlockPoint, innerRouteParams);
    }
}
