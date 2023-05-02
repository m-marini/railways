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

import static org.mmarini.yaml.schema.Validator.objectPropertiesRequired;
import static org.mmarini.yaml.schema.Validator.positiveInteger;

/**
 * Describes a list of semaphores identified by a name.
 */
public class Semaphores extends AbstractBlock {
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "numSemaphores", positiveInteger()),
            List.of("numSemaphores")
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Semaphores create(JsonNode root, Locator locator, String id) {
        VALIDATOR.apply(locator).accept(root);
        int numSemaphores = locator.path("numSemaphores").getNode(root).asInt();
        return new Semaphores(id, numSemaphores);
    }
    private final int numSemaphores;
    private final List<String> connections;

    /**
     * Creates the list of semaphores
     *
     * @param id            the identifier
     * @param numSemaphores the number of semaphores
     */
    public Semaphores(String id, int numSemaphores) {
        super(id);
        this.numSemaphores = numSemaphores;
        this.connections = IntStream.range(1, numSemaphores + 1)
                .boxed()
                .flatMap(i -> Stream.of("e." + i, "w." + i))
                .collect(Collectors.toList());
    }

    /**
     * Returns the number of semaphores
     */
    public int getNumSemaphores() {
        return numSemaphores;
    }

    @Override
    public int indexOf(String text) {
        return connections.indexOf(text);
    }

    @Override
    public Point2D location(int index) {
        throw new NotImplementedException();
    }

    @Override
    public int orientation(int index) {
        return (index % 2) == 0 ? 0 : -180;
    }
}
