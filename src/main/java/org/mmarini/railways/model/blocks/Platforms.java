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
 * Describes a list of platforms identified by a name.
 */
public class Platforms extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numPlatforms", positiveInteger(),
                    "length", positiveInteger()),
            List.of("numPlatforms", "length")
    );

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
        return new Platforms(id, numPlatforms, length);
    }

    private final int numPlatforms;
    private final int length;
    private final List<String> connections;
    private final Point2D[] locations;

    /**
     * Creates a platform
     *
     * @param id           the id
     * @param numPlatforms the number of platforms
     * @param length       the length in number of coaches
     */
    public Platforms(String id, int numPlatforms, int length) {
        super(id);
        this.numPlatforms = numPlatforms;
        this.length = length;
        this.connections = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> Stream.of(
                        "w." + (numPlatforms - i),
                        "e." + (numPlatforms - i)))
                .collect(Collectors.toList());
        this.locations = IntStream.range(0, numPlatforms)
                .boxed()
                .flatMap(i -> Stream.of(
                        new Point2D.Double(0, i * TRACK_GAP),
                        new Point2D.Double(length * SEGMENT_LENGTH, i * TRACK_GAP)))
                .toArray(Point2D[]::new);
    }

    /**
     * Returns the length in number of coaches
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the number of platform
     */
    public int getNumPlatforms() {
        return numPlatforms;
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
