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
import org.mmarini.railways2.model.routes.CrossRoute;
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Route;
import org.mmarini.yaml.schema.Locator;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mmarini.railways2.model.RailwayConstants.*;

/**
 * Describes the central cross block.
 */
public class CentralCross extends AbstractBlock {
    public static final List<EdgeBuilderParams> EDGE_BUILDERS = List.of(
            EdgeBuilderParams.track("sw.s1", "sw", "s1"),
            EdgeBuilderParams.track("w1.s2", "w1", "s2"),
            EdgeBuilderParams.track("w2.s3", "w2", "s3"),
            EdgeBuilderParams.track("nw.s4", "nw", "s4"),

            EdgeBuilderParams.curve("s1.s5", "s1", "s5", -SWITCH_ANGLE_RAD),
            EdgeBuilderParams.track("s1.s6", "s1", "s6"),
            EdgeBuilderParams.track("s2.s5", "s2", "s5"),
            EdgeBuilderParams.curve("s2.s6", "s2", "s6", SWITCH_ANGLE_RAD),

            EdgeBuilderParams.curve("s3.s7", "s3", "s7", -SWITCH_ANGLE_RAD),
            EdgeBuilderParams.track("s3.s8", "s3", "s8"),
            EdgeBuilderParams.track("s4.s7", "s4", "s7"),
            EdgeBuilderParams.curve("s4.s8", "s4", "s8", SWITCH_ANGLE_RAD),

            EdgeBuilderParams.track("s5.s9", "s5", "s9"),

            EdgeBuilderParams.track("s6.x", "s6", "x"),
            EdgeBuilderParams.track("s7.x", "s7", "x"),
            EdgeBuilderParams.track("x.s10", "x", "s10"),
            EdgeBuilderParams.track("x.s11", "x", "s11"),

            EdgeBuilderParams.track("s8.s12", "s8", "s12"),

            EdgeBuilderParams.curve("s9.s13", "s9", "s13", -SWITCH_ANGLE_RAD),
            EdgeBuilderParams.track("s9.s14", "s9", "s14"),
            EdgeBuilderParams.track("s10.s13", "s10", "s13"),
            EdgeBuilderParams.curve("s10.s14", "s10", "s14", SWITCH_ANGLE_RAD),

            EdgeBuilderParams.curve("s11.s15", "s11", "s15", -SWITCH_ANGLE_RAD),
            EdgeBuilderParams.track("s11.s16", "s11", "s16"),
            EdgeBuilderParams.track("s12.s15", "s12", "s15"),
            EdgeBuilderParams.curve("s12.s16", "s12", "s16", SWITCH_ANGLE_RAD),

            EdgeBuilderParams.track("s13.se", "s13", "se"),
            EdgeBuilderParams.track("s14.e1", "s14", "e1"),
            EdgeBuilderParams.track("s15.e2", "s15", "e2"),
            EdgeBuilderParams.track("s16.ne", "s16", "ne")
    );
    public static final Map<String, String> EDGE_BY_BLOCK_POINT = Map.of(
            "sw", "sw.s1",
            "w1", "w1.s2",
            "w2", "w2.s3",
            "nw", "nw.s4",
            "se", "s13.se",
            "e1", "s14.e1",
            "e2", "s15.e2",
            "ne", "s16.ne"
    );
    public static final List<Tuple2<Function<Node[], ? extends Route>, List<String>>> INNER_ROUTE_PARAMS = List.of(
            Tuple2.of(DoubleSlipSwitch::through, List.of("s2", "s5", "s1", "s6")),
            Tuple2.of(DoubleSlipSwitch::through, List.of("s3", "s8", "s4", "s7")),
            Tuple2.of(DoubleSlipSwitch::through, List.of("s9", "s14", "s10", "s13")),
            Tuple2.of(DoubleSlipSwitch::through, List.of("s12", "s15", "s11", "s16")),
            Tuple2.of(CrossRoute::create, List.of("x"))
    );
    private static final Map<String, OrientedGeometry> GEOMETRY_BY_ID = Map.of(
            "nw", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP * 1.5), -SWITCH_ANGLE_DEG),
            "w2", new OrientedGeometry(new Point2D.Double(0, TRACK_GAP), 0),
            "w1", new OrientedGeometry(new Point2D.Double(0, 0), 0),
            "sw", new OrientedGeometry(new Point2D.Double(0, -TRACK_GAP / 2), SWITCH_ANGLE_DEG),
            "ne", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH * 2, TRACK_GAP * 1.5), SWITCH_ANGLE_DEG - 180),
            "e2", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH * 2, TRACK_GAP), -180),
            "e1", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH * 2, 0), -180),
            "se", new OrientedGeometry(new Point2D.Double(SWITCH_LENGTH * 2, -TRACK_GAP / 2), 180 - SWITCH_ANGLE_DEG)
    );
    private static final List<NodeBuilderParams> INNER_NODES = List.of(
            NodeBuilderParams.create("s1", SWITCH_GAP, -PLATFORM_SWITCH_Y, "sw.s1", "s1.s6", "s1.s5"),
            NodeBuilderParams.create("s2", SWITCH_GAP, 0, "w1.s2", "s2.s5", "s2.s6"),
            NodeBuilderParams.create("s3", SWITCH_GAP, TRACK_GAP, "w2.s3", "s3.s8", "s3.s7"),
            NodeBuilderParams.create("s4", SWITCH_GAP, TRACK_GAP + PLATFORM_SWITCH_Y, "nw.s4", "s4.s7", "s4.s8"),
            NodeBuilderParams.create("s5", SWITCH_LENGTH - SWITCH_GAP, 0, "s5.s9", "s2.s5", "s1.s5"),
            NodeBuilderParams.create("s6", SWITCH_LENGTH - SWITCH_GAP, PLATFORM_SWITCH_Y, "s6.x", "s1.s6", "s2.s6"),
            NodeBuilderParams.create("s7", SWITCH_LENGTH - SWITCH_GAP, TRACK_GAP - PLATFORM_SWITCH_Y, "s7.x", "s4.s7", "s3.s7"),
            NodeBuilderParams.create("s8", SWITCH_LENGTH - SWITCH_GAP, TRACK_GAP, "s8.s12", "s3.s8", "s4.s8"),
            NodeBuilderParams.create("s9", SWITCH_LENGTH + SWITCH_GAP, 0, "s5.s9", "s9.s14", "s9.s13"),
            NodeBuilderParams.create("s10", SWITCH_LENGTH + SWITCH_GAP, PLATFORM_SWITCH_Y, "x.s10", "s10.s13", "s10.s14"),
            NodeBuilderParams.create("s11", SWITCH_LENGTH + SWITCH_GAP, TRACK_GAP - PLATFORM_SWITCH_Y, "x.s11", "s11.s16", "s11.s15"),
            NodeBuilderParams.create("s12", SWITCH_LENGTH + SWITCH_GAP, TRACK_GAP, "s8.s12", "s12.s15", "s12.s16"),
            NodeBuilderParams.create("s13", SWITCH_LENGTH * 2 - SWITCH_GAP, -PLATFORM_SWITCH_Y, "s13.se", "s10.s13", "s9.s13"),
            NodeBuilderParams.create("s14", SWITCH_LENGTH * 2 - SWITCH_GAP, 0, "s14.e1", "s9.s14", "s10.s14"),
            NodeBuilderParams.create("s15", SWITCH_LENGTH * 2 - SWITCH_GAP, TRACK_GAP, "s15.e2", "s12.s15", "s11.s15"),
            NodeBuilderParams.create("s16", SWITCH_LENGTH * 2 - SWITCH_GAP, TRACK_GAP + PLATFORM_SWITCH_Y, "s16.ne", "s11.s16", "s12.s16"),
            NodeBuilderParams.create("x", SWITCH_LENGTH, TRACK_GAP / 2, "s6.x", "x.s11", "s7.x", "x.s10")
    );

    /**
     * Returns the central cross from json definition
     *
     * @param root    the root doc
     * @param locator the locator
     * @param id      the id of down cross
     */
    public static CentralCross create(JsonNode root, Locator locator, String id) {
        return create(id);
    }

    /**
     * Returns the central cross block
     *
     * @param id the identifier
     */
    public static CentralCross create(String id) {
        return new CentralCross(id);
    }

    /**
     * Creates the central cross block
     *
     * @param id the identifier
     */
    protected CentralCross(String id) {
        super(id, GEOMETRY_BY_ID, INNER_NODES, EDGE_BUILDERS, EDGE_BY_BLOCK_POINT, INNER_ROUTE_PARAMS);
    }
}
