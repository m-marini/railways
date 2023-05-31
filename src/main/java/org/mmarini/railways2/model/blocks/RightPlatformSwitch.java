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
 * Describes the right platform switch
 */
public class RightPlatformSwitch extends AbstractBlock {
    public static final List<EdgeBuilderParams> EDGE_BUILDERS = List.of(
            EdgeBuilderParams.track("entry.switch", "entry", "switch"),
            EdgeBuilderParams.track("switch.t1", "switch", "t1"),
            EdgeBuilderParams.track("t2.through", "t2", "through"),
            EdgeBuilderParams.track("d1.diverged", "d1", "diverged"),
            EdgeBuilderParams.curve("switch.d1", "switch", "d1", -SWITCH_ANGLE_DEG),
            EdgeBuilderParams.curve("t1.t2", "t1", "t2", -SWITCH_ANGLE_DEG)
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "entry", "entry.switch",
            "through", "t2.through",
            "diverged", "d1.diverged"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(Switch::through, List.of("switch")),
            Tuple2.of(Junction::create, List.of("t1")),
            Tuple2.of(Junction::create, List.of("t2")),
            Tuple2.of(Junction::create, List.of("d1"))
    );
    private static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "diverged", new OrientedGeometry(new Point2D.Double(), 0),
            "through", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP), 0),
            "entry", new OrientedGeometry(new Point2D.Double(2 * SWITCH_LENGTH, TRACK_GAP * 3 / 2), SWITCH_ANGLE_DEG - 180)
    );
    private static final List<NodeBuilderParams> INNER_NODES = List.of(
            NodeBuilderParams.create("switch", 2 * SWITCH_LENGTH - SWITCH_GAP, PLATFORM_SWITCH_Y, "entry.switch", "switch.t1", "switch.d1"),
            NodeBuilderParams.create("d1", SWITCH_LENGTH + SWITCH_GAP, 0, "switch.d1", "d1.diverged"),
            NodeBuilderParams.create("t1", SWITCH_LENGTH - SWITCH_GAP, -TRACK_GAP + PLATFORM_SWITCH_Y, "switch.t1", "t1.t2"),
            NodeBuilderParams.create("t2", SWITCH_GAP, -TRACK_GAP, "t1.t2", "t2.through")
    );

    /**
     * Returns the platform from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of platform
     */
    public static RightPlatformSwitch create(JsonNode root, Locator locator, String id) {
        return create(id);
    }

    /**
     * Returns the platform from json definition
     *
     * @param id the id of platform
     */
    public static RightPlatformSwitch create(String id) {
        return new RightPlatformSwitch(id);
    }

    /**
     * Creates the abstract block
     *
     * @param id the identifier
     */
    protected RightPlatformSwitch(String id) {
        super(id, GEOMETRY_BY_ID, INNER_NODES, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }
}