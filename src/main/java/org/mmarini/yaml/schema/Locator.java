/*
 *
 * Copyright (c) 2021 Marco Marini, marco.marini@mmarini.org
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

package org.mmarini.yaml.schema;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.stream;

public class Locator {

    private static final Locator ROOT = new Locator(JsonPointer.empty());

    /**
     *
     */
    public static Locator locate(String path) {
        return ROOT.path(path);
    }

    /**
     *
     */
    public static Locator root() {
        return ROOT;
    }

    public final JsonPointer pointer;

    public Locator(JsonPointer pointer) {
        this.pointer = requireNonNull(pointer);
    }

    public Stream<Tuple2<Integer, Locator>> elementIndices(JsonNode root) {
        requireNonNull(root);
        return IntStream.range(0, getNode(root).size())
                .mapToObj(idx -> Tuple2.of(idx, path(String.valueOf(idx))));
    }

    /**
     * @param root the root element
     */
    public Stream<Locator> elements(JsonNode root) {
        requireNonNull(root);
        return IntStream.range(0, getNode(root).size())
                .mapToObj(String::valueOf)
                .map(this::path);
    }

    /**
     * @param root the root element
     */
    public JsonNode getNode(JsonNode root) {
        return root.at(pointer);
    }

    /**
     *
     */
    public JsonPointer getPointer() {
        return pointer;
    }

    /**
     *
     */
    public Locator parent() {
        return parent(1);
    }

    /**
     * @param levels number of levels
     */
    public Locator parent(int levels) {
        assert levels >= 0;
        JsonPointer ptr = pointer;
        for (int i = 0; i < levels; i++) {
            ptr = ptr.head();
        }
        return new Locator(ptr);
    }

    /**
     * @param path the path
     */
    public Locator path(Locator path) {
        requireNonNull(path);
        return path(path.pointer);
    }

    /**
     * @param path the path
     */
    public Locator path(JsonPointer path) {
        requireNonNull(path);
        return new Locator(pointer.append(path));
    }

    /**
     * @param path the path
     */
    public Locator path(String path) {
        requireNonNull(path);
        return path(JsonPointer.valueOf("/" + path));
    }

    /**
     * Returns the stream of property locators
     */
    public Stream<Locator> properties(JsonNode root) {
        requireNonNull(root);
        return stream(getNode(root).fieldNames())
                .map(this::path);
    }

    /**
     * Returns the stream of property name and locator
     *
     * @param root the root element
     */
    public Stream<Tuple2<String, Locator>> propertyNames(JsonNode root) {
        requireNonNull(root);
        return stream(getNode(root).fieldNames())
                .map(name ->
                        Tuple2.of(name, path(name)));
    }

    /**
     * @param root the root element
     */
    public int size(JsonNode root) {
        return getNode(root).size();
    }

    @Override
    public String toString() {
        return pointer.toString();
    }
}
