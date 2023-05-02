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
import org.mmarini.NotImplementedException;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mmarini.yaml.schema.Validator.*;

/**
 * Describes curveed tracks.
 */
public class Curves extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numTracks", positiveInteger(),
                    "angle", integer(minimum(-180), maximum(180))),
            List.of("numTracks", "angle")
    );

    @Override
    public Point2D location(int index) {
        throw new NotImplementedException();
    }

    @Override
    public int orientation(int index) {
        throw new NotImplementedException();
    }

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
        return new Curves(id, numTracks, angle);
    }

    private final int numTracks;
    private final int angle;
    private final List<String> connections;

    /**
     * Creates the abstract block
     *
     * @param id        the identifier
     * @param numTracks the number of tracks
     * @param angle     the CW angle (DEG)
     */
    public Curves(String id, int numTracks, int angle) {
        super(id);
        this.numTracks = numTracks;
        this.angle = angle;
        this.connections = IntStream.range(1, numTracks + 1)
                .boxed()
                .flatMap(i -> Stream.of("e." + i, "w." + i))
                .collect(Collectors.toList());
    }

    /**
     * Returns the CW angle (DEG)
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Returns the number of tracks
     */
    public int getNumTracks() {
        return numTracks;
    }

    @Override
    public int indexOf(String text) {
        return connections.indexOf(text);
    }
}
