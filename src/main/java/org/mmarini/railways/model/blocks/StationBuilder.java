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
import org.mmarini.Tuple2;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.util.List;
import java.util.Map;

import static org.mmarini.yaml.Utils.DYNAMIC_OBJECT;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Creates a station builder
 */
public class StationBuilder {
    private static final Validator BLOCKS_VALIDATOR = objectAdditionalProperties(DYNAMIC_OBJECT);

    private static final Validator LINK_VALIDATOR = array(
            arrayItems(string()),
            minItems(2),
            maxItems(2));
    private static final Validator LINKS_VALIDATOR = arrayItems(LINK_VALIDATOR);
    public final static Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "name", string(),
                    "orientation", integer(minimum(0), maximum(359)),
                    "blocks", BLOCKS_VALIDATOR,
                    "links", LINKS_VALIDATOR
            ),
            List.of("name", "orientation", "blocks", "links")
    );
    private final Locator locator;
    private final JsonNode root;
    Map<String, Block> blocks;
    Map<String, String> links;

    /**
     * Creates the builder
     *
     * @param root    the json document
     * @param locator the station node locator
     */
    public StationBuilder(JsonNode root, Locator locator) {
        this.root = root;
        this.locator = locator;
    }

    /**
     * Returns the station built from the document
     *
     * @return
     */
    public Station build() {
        throw new NotImplementedException();
    }

    void createBlocks() {
        this.blocks = locator.path("blocks").propertyNames(root)
                .map(t -> t.setV2(
                        Utils.<Block>createObject(root, t._2,
                                new Object[]{t._1}, new Class[]{String.class})))
                .collect(Tuple2.toMap());
    }

    void loadLinks() {
        links = locator.path("links").elements(root)
                .map(loc -> Tuple2.of(loc.getNode(root).get(0).asText(),
                        loc.getNode(root).get(1).asText()))
                .collect(Tuple2.toMap());
    }

    /**
     * Validates the document and the station definition
     */
    void validate() {
        VALIDATOR.apply(locator).accept(root);
        createBlocks();
        loadLinks();

    }
}
