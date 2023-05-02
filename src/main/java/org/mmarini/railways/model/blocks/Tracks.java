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

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mmarini.railways.model.RailwayConstants.SEGMENT_LENGTH;
import static org.mmarini.railways.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.yaml.schema.Validator.objectPropertiesRequired;
import static org.mmarini.yaml.schema.Validator.positiveInteger;

/**
 * Describes a line tracks
 */
public class Tracks extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numTracks", positiveInteger(),
                    "length", positiveInteger()),
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
        return new Tracks(id, numTracks, length);
    }

    private final int numTracks;
    private final int length;
    private final List<String> connections;
    private final Point2D[] locations;

    /**
     * Creates the abstract block
     *
     * @param id        the identifier
     * @param numTracks the number of tracks
     * @param length    the length in number of horizontal unit blocks
     */
    public Tracks(String id, int numTracks, int length) {
        super(id);
        this.numTracks = numTracks;
        this.length = length;
        connections = IntStream.range(0, numTracks)
                .boxed()
                .flatMap(i -> Stream.of(
                        "e." + (numTracks - i),
                        "w." + (numTracks - i)))
                .collect(Collectors.toList());
        this.locations = IntStream.range(0, numTracks)
                .boxed()
                .flatMap(i -> Stream.of(
                        new Point2D.Double(0, i * TRACK_GAP),
                        new Point2D.Double(length * SEGMENT_LENGTH, i * TRACK_GAP)
                ))
                .toArray(Point2D[]::new);
    }

    /**
     * Returns the length in number of horizontal unit blocks
     */
    public int getLength() {
        return length;
    }

    /**
     * The number of tracks
     */
    public int getNumTracks() {
        return numTracks;
    }

    @Override
    public int indexOf(String text) {
        return connections.indexOf(text);
    }

    @Override
    public Point2D location(int index) {
        return locations[index];
    }

    @Override
    public int orientation(int index) {
        return (index % 2) == 0 ? 0 : -180;
    }
}
