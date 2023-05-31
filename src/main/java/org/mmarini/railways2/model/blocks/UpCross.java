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
 * The oblique track is between SW --- NE
 */
public class UpCross extends AbstractBlock {
    public static final List<EdgeBuilderParams> EDGE_BUILDERS = List.of(
            EdgeBuilderParams.track("sw.sws", "sw", "sws"),
            EdgeBuilderParams.track("w.ws", "w", "ws"),
            EdgeBuilderParams.track("sws.es", "sws", "es"),
            EdgeBuilderParams.track("sws.nes", "sws", "nes"),
            EdgeBuilderParams.track("ws.es", "ws", "es"),
            EdgeBuilderParams.track("ws.nes", "ws", "nes"),
            EdgeBuilderParams.track("es.e", "es", "e"),
            EdgeBuilderParams.track("nes.ne", "nes", "ne")
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "sw", "sw.sws",
            "w", "w.ws",
            "e", "es.e",
            "ne", "nes.ne"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(DoubleSlipSwitch::through, List.of("ws", "es", "sws", "nes"))
    );
    private static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "w", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP / 2), 0),
            "e", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, TRACK_GAP / 2), -180),
            "sw", new OrientedGeometry(new Point2D.Double(0, 0), SWITCH_ANGLE_DEG),
            "ne", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, TRACK_GAP), SWITCH_ANGLE_DEG - 180)
    );
    private static final List<NodeBuilderParams> INNER_NODES = List.of(
            NodeBuilderParams.create("sws", SWITCH_GAP, -PLATFORM_SWITCH_Y, "sw.sws", "sws.nes", "sws.es"),
            NodeBuilderParams.create("ws", SWITCH_GAP, 0, "w.ws", "ws.es", "ws.nes"),
            NodeBuilderParams.create("es", SWITCH_LENGTH - SWITCH_GAP, 0, "es.e", "ws.es", "sws.es"),
            NodeBuilderParams.create("nes", SWITCH_LENGTH - SWITCH_GAP, PLATFORM_SWITCH_Y, "nes.ne", "sws.nes", "ws.nes")
    );

    /**
     * Returns the up-cross from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static UpCross create(JsonNode root, Locator locator, String id) {
        return create(id);
    }

    /**
     * Returns the up-cross
     *
     * @param id the id of up-cross
     */
    public static UpCross create(String id) {
        return new UpCross(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    protected UpCross(String id) {
        super(id, GEOMETRY_BY_ID, INNER_NODES, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }

}
