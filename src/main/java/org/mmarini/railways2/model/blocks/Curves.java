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
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.mmarini.railways2.model.MathUtils.normalizeDeg;
import static org.mmarini.railways2.model.RailwayConstants.RADIUS;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Describes curved tracks.
 * Each curves block is composed by tracks at a given angle.
 * The radius of the inner tracks is the standard radius, each outer tracks is separated by TRACK_GAP value.
 */
public class Curves extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numTracks", positiveInteger(),
                    "angle", integer(minimum(-180), maximum(180))),
            List.of("numTracks", "angle")
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Curves create(JsonNode root, Locator locator, String id) {
        VALIDATOR.apply(locator).accept(root);
        int numTracks = locator.path("numTracks").getNode(root).asInt();
        int angle = locator.path("angle").getNode(root).asInt();
        return Curves.create(id, numTracks, angle);
    }

    /**
     * Returns the platform from json definition
     *
     * @param id        the id of platform
     * @param numTracks the number of tracks
     * @param angle     the curve angle
     */
    public static Curves create(String id, int numTracks, int angle) {
        Map<String, OrientedGeometry> geometryById;
        double sin = sin(toRadians(angle));
        double cos = cos(toRadians(angle));
        int eastEntry = normalizeDeg(angle + 180);
        if (angle >= 0) {
            // Left curve
            double y0 = RADIUS + (numTracks - 1) * TRACK_GAP;
            geometryById = IntStream.range(0, numTracks)
                    .boxed()
                    .flatMap(i -> {
                        double radius = RADIUS + (numTracks - i - 1) * TRACK_GAP;
                        return Stream.of(
                                Tuple2.of(
                                        "w" + (i + 1), new OrientedGeometry(
                                                new Point2D.Double(0, i * TRACK_GAP), 0)),
                                Tuple2.of(
                                        "e" + (i + 1), new OrientedGeometry(
                                                new Point2D.Double(sin * radius, y0 - radius * cos), eastEntry)
                                ));
                    })
                    .collect(Tuple2.toMap());
        } else {
            // Right curve
            double y0 = -RADIUS;
            geometryById = IntStream.range(0, numTracks)
                    .boxed()
                    .flatMap(i -> {
                        double radius = RADIUS + i * TRACK_GAP;
                        return Stream.of(
                                Tuple2.of(
                                        "w" + (i + 1), new OrientedGeometry(
                                                new Point2D.Double(0, i * TRACK_GAP), 0)),
                                Tuple2.of(
                                        "e" + (i + 1), new OrientedGeometry(
                                                new Point2D.Double(-sin * radius, y0 + radius * cos), eastEntry)
                                ));
                    })
                    .collect(Tuple2.toMap());
        }
        List<EdgeBuilderParams> edgeBuilderParams = IntStream.range(0, numTracks)
                .mapToObj(i -> {
                    String id1 = String.valueOf(i + 1);
                    return EdgeBuilderParams.curve("w" + id1 + ".e" + id1,
                            "w" + id1, "e" + id1, angle);
                })
                .collect(Collectors.toList());
        Map<String, String> edgeByBlockPoint = IntStream.range(0, numTracks)
                .boxed()
                .flatMap(i -> Stream.of(
                        Tuple2.of("w" + (i + 1), "w" + (i + 1) + ".e" + (i + 1)),
                        Tuple2.of("e" + (i + 1), "w" + (i + 1) + ".e" + (i + 1))
                )).collect(Tuple2.toMap());
        return new Curves(id, angle, geometryById, edgeBuilderParams, edgeByBlockPoint);
    }

    private final int angle;

    /**
     * Creates the abstract block
     *
     * @param id                the identifier
     * @param angle             the CCW angle (DEG)
     * @param geometryById      the geometry by id
     * @param edgeBuilderParams the edge builders
     * @param edgeByBlockPoint  the edge by block point
     */
    protected Curves(String id, int angle, Map<String, OrientedGeometry> geometryById, List<EdgeBuilderParams> edgeBuilderParams, Map<String, String> edgeByBlockPoint) {
        super(id, geometryById, List.of(), edgeBuilderParams, edgeByBlockPoint, List.of());
        this.angle = angle;
    }

    /**
     * Returns the CW angle (DEG)
     */
    public int getAngle() {
        return angle;
    }
}
