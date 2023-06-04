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

import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.schema.Validator.objectPropertiesRequired;
import static org.mmarini.yaml.schema.Validator.positiveInteger;

/**
 * Describes a list of platforms identified by a name.
 * The platforms block is composed by a given number of platforms and
 * the length of platform in multiple of coach length plus a platform gap
 */
public class Platforms extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numPlatforms", positiveInteger(),
                    "length", positiveInteger()),
            List.of("numPlatforms", "length")
    );
    public static final double PLATFORM_GAP = 1;
    public static final double PLATFORM_SIGNAL_GAP = 1;

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Platforms create(JsonNode root, Locator locator, String id) {
        VALIDATOR.apply(locator).accept(root);
        int numPlatforms = locator.path("numPlatforms").getNode(root).asInt();
        int length = locator.path("length").getNode(root).asInt();
        return create(id, numPlatforms, length);
    }

    /**
     * Returns the platforms
     *
     * @param id           the id of platform
     * @param numPlatforms the number of platforms
     * @param length       the length
     */
    public static Platforms create(String id, int numPlatforms, int length) {
        double totalLength = length * COACH_LENGTH + PLATFORM_GAP + 2 * PLATFORM_SIGNAL_GAP;
        Map<String, OrientedGeometry> geometryById = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of(
                                "w" + (i + 1), new OrientedGeometry(
                                        new Point2D.Double(0, i * TRACK_GAP), 0)),
                        Tuple2.of(
                                "e" + (i + 1), new OrientedGeometry(
                                        new Point2D.Double(totalLength, i * TRACK_GAP), -180)
                        )))
                .collect(Tuple2.toMap());
        List<NodeBuilderParams> innerPoints = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> {
                    String suffix = String.valueOf(i + 1);
                    return Stream.of(
                            NodeBuilderParams.create("signalw" + suffix,
                                    PLATFORM_SIGNAL_GAP, i * TRACK_GAP,
                                    "w" + suffix + ".signalw" + suffix,
                                    "p" + suffix),
                            NodeBuilderParams.create(
                                    "signale" + suffix,
                                    totalLength - PLATFORM_SIGNAL_GAP, i * TRACK_GAP,
                                    "p" + suffix,
                                    "signale" + suffix + ".e" + suffix));
                })
                .collect(Collectors.toList());
        List<EdgeBuilderParams> edgeBuilderParams = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> {
                    String suffixId = String.valueOf(i + 1);
                    return Stream.of(
                            EdgeBuilderParams.track("w" + suffixId + ".signalw" + suffixId,
                                    "w" + suffixId, "signalw" + suffixId),
                            EdgeBuilderParams.platform("p" + suffixId,
                                    "signalw" + suffixId, "signale" + suffixId),
                            EdgeBuilderParams.track("signale" + suffixId + ".e" + suffixId,
                                    "signale" + suffixId, "e" + suffixId)
                    );
                })
                .collect(Collectors.toList());
        Map<String, String> edgeByBlockPoint = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of("w" + (i + 1), "w" + (i + 1) + ".signalw" + (i + 1)),
                        Tuple2.of("e" + (i + 1), "signale" + (i + 1) + ".e" + (i + 1))
                )).collect(Tuple2.toMap());
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.<Function<Node[], ? extends Route>, List<String>>of(Signal::create, List.of("signalw" + (i + 1))),
                        Tuple2.<Function<Node[], ? extends Route>, List<String>>of(Signal::create, List.of("signale" + (i + 1)))
                ))
                .collect(Collectors.toList());
        return new Platforms(id, geometryById, innerPoints, edgeBuilderParams, edgeByBlockPoint, innerRouteParams);
    }

    /**
     * Creates a platform
     *
     * @param id                the id
     * @param geometryById      the geometry by id
     * @param innerPoints       the inner points
     * @param edgeBuilderParams the edge builders
     * @param edgeByBlockPoint  edge by block point
     * @param innerRouteParams  the inner route parameters
     */
    protected Platforms(String id, Map<String, OrientedGeometry> geometryById, List<NodeBuilderParams> innerPoints, List<EdgeBuilderParams> edgeBuilderParams, Map<String, String> edgeByBlockPoint, List<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams) {
        super(id, geometryById, innerPoints, edgeBuilderParams, edgeByBlockPoint, innerRouteParams);
    }
}
