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
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.railways2.model.routes.Switch;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mmarini.railways2.model.RailwayConstants.*;

/**
 * Describes the right switch
 */
public class RightSwitch extends AbstractBlock {
    public static final List<EdgeBuilderParams> EDGE_BUILDERS = List.of(
            EdgeBuilderParams.track("entry.switch", "entry", "switch"),
            EdgeBuilderParams.track("switch.through", "switch", "through"),
            EdgeBuilderParams.curve("switch.d", "switch", "d", -SWITCH_ANGLE_RAD),
            EdgeBuilderParams.track("d.diverged", "d", "diverged")
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "entry", "entry.switch",
            "through", "switch.through",
            "diverged", "d.diverged"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(Switch::through, List.of("switch")),
            Tuple2.of(Junction::create, List.of("d"))
    );
    private static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "entry", new OrientedGeometry(new Point2D.Double(), 0),
            "through", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, 0), -180),
            "diverged", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH, -TRACK_GAP / 2), 180 - SWITCH_ANGLE_DEG)
    );
    private static final List<NodeBuilderParams> INNER_POINTS = List.of(
            NodeBuilderParams.create("switch", SWITCH_GAP, 0, "entry.switch", "switch.through", "switch.d"),
            NodeBuilderParams.create("d", SWITCH_LENGTH - SWITCH_GAP, -PLATFORM_SWITCH_Y, "switch.d", "d.diverged")
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static RightSwitch create(JsonNode root, Locator locator, String id) {
        return new RightSwitch(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    public RightSwitch(String id) {
        super(id, GEOMETRY_BY_ID, INNER_POINTS, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }
}
