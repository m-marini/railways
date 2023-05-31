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
import org.mmarini.railways2.model.geometry.EdgeBuilder;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mmarini.railways2.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Describes a line tracks
 */
public class Tracks extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numTracks", positiveInteger(),
                    "length", positiveNumber()),
            List.of("numTracks", "length")
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Tracks create(JsonNode root, Locator locator, String id) {
        VALIDATOR.apply(locator).accept(root);
        int numTracks = locator.path("numTracks").getNode(root).asInt();
        int length = locator.path("length").getNode(root).asInt();
        return create(id, numTracks, length);
    }

    /**
     * Returns the platform from json definition
     *
     * @param id        the id of platform
     * @param numTracks the number of tracks
     * @param length    the length
     */
    public static Tracks create(String id, int numTracks, int length) {
        Map<String, OrientedGeometry> geometryById = IntStream.range(0, numTracks)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of(
                                "w" + (i + 1), new OrientedGeometry(
                                        new Point2D.Double(0, i * TRACK_GAP), 0)),
                        Tuple2.of(
                                "e" + (i + 1), new OrientedGeometry(
                                        new Point2D.Double(length * SEGMENT_LENGTH, i * TRACK_GAP), 180)
                        )))
                .collect(Tuple2.toMap());

        List<EdgeBuilder> edgeBuilders = IntStream.range(0, numTracks)
                .mapToObj(i -> {
                    String id1 = String.valueOf(i + 1);
                    return EdgeBuilder.track("w" + id1 + ".e" + id1, "w" + id1, "e" + id1);
                }).collect(Collectors.toList());
        Map<String, String> edgeByBlockPoint = IntStream.range(0, numTracks)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of("w" + (i + 1), "w" + (i + 1) + ".e" + (i + 1)),
                        Tuple2.of("e" + (i + 1), "w" + (i + 1) + ".e" + (i + 1))
                )).collect(Tuple2.toMap());
        return new Tracks(id, geometryById, edgeBuilders, edgeByBlockPoint);
    }

    /**
     * Creates the abstract block
     *
     * @param id               the identifier
     * @param geometryById     the geometry by id
     * @param edgeBuilders     the edge builder
     * @param edgeByBlockPoint the edge by block point
     */
    protected Tracks(String id, Map<String, OrientedGeometry> geometryById, List<EdgeBuilder> edgeBuilders, Map<String, String> edgeByBlockPoint) {
        super(id, geometryById, List.of(), edgeBuilders, edgeByBlockPoint, List.of());
    }
}
