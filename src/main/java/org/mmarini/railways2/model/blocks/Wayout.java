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
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;

/**
 * Describes a way out (one entry and one exit)
 */
public class Wayout extends AbstractBlock {

    public static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "entry", new OrientedGeometry(new Point2D.Double(0, 0), 0),
            "exit", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP), 0)
    );
    public static final List<NodeBuilderParams> INNER_NODES = List.of(
            NodeBuilderParams.create("in", 1, 0, "entry.in"),
            NodeBuilderParams.create("out", 1, TRACK_GAP, "exit.out"));
    public static final List<EdgeBuilder> EDGE_BUILDERS = List.of(
            EdgeBuilder.track("entry.in", "entry", "in"),
            EdgeBuilder.track("exit.out", "exit", "out")
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "entry", "entry.in",
            "exit", "exit.out"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(Entry::create, List.of("in")),
            Tuple2.of(Exit::create, List.of("out"))
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static Wayout create(JsonNode root, Locator locator, String id) {
        return create(id);
    }

    public static Wayout create(String id) {
        return new Wayout(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    protected Wayout(String id) {
        super(id, GEOMETRY_BY_ID, INNER_NODES, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }
}
