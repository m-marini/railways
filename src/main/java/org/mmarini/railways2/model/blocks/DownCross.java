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
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mmarini.railways2.model.RailwayConstants.*;

/**
 * Describes the down cross block.
 * The oblique track is between NW --- SE
 */
public class DownCross extends AbstractBlock {
    public static final List<EdgeBuilderParams> EDGE_BUILDERS = List.of(
            EdgeBuilderParams.track("w.ws", "w", "ws"),
            EdgeBuilderParams.track("nw.nws", "nw", "nws"),
            EdgeBuilderParams.track("ws.ses", "ws", "ses"),
            EdgeBuilderParams.track("ws.es", "ws", "es"),
            EdgeBuilderParams.track("nws.ses", "nws", "ses"),
            EdgeBuilderParams.track("nws.es", "nws", "es"),
            EdgeBuilderParams.track("ses.se", "ses", "se"),
            EdgeBuilderParams.track("es.e", "es", "e")
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "w", "w.ws",
            "nw", "nw.nws",
            "se", "ses.se",
            "e", "es.e"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(DoubleSlipSwitch::through, List.of("ws", "es", "nws", "ses"))
    );
    private static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "w", new OrientedGeometry(new Point2D.Double(), 0),
            "e", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, 0), -180),
            "nw", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP / 2), -SWITCH_ANGLE_DEG),
            "se", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, -TRACK_GAP / 2), 180 - SWITCH_ANGLE_DEG)
    );
    private static final List<NodeBuilderParams> INNER_NODES = List.of(
            NodeBuilderParams.create("nws", SWITCH_GAP, PLATFORM_SWITCH_Y, "nw.nws", "nws.ses", "nws.es"),
            NodeBuilderParams.create("ws", SWITCH_GAP, 0, "w.ws", "ws.es", "ws.ses"),
            NodeBuilderParams.create("es", SWITCH_LENGTH - SWITCH_GAP, 0, "es.e", "ws.es", "nws.es"),
            NodeBuilderParams.create("ses", SWITCH_LENGTH - SWITCH_GAP, -PLATFORM_SWITCH_Y, "ses.se", "nws.ses", "ws.ses")
    );

    /**
     * Returns the down cross from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of down cross
     */
    public static DownCross create(JsonNode root, Locator locator, String id) {
        return create(id);
    }


    /**
     * Returns the down cross from json definition
     *
     * @param id the id of down cross
     */
    public static DownCross create(String id) {
        return new DownCross(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    protected DownCross(String id) {
        super(id, GEOMETRY_BY_ID, INNER_NODES, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }
}
