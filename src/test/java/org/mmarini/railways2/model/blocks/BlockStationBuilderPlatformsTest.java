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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.EdgeBuilderParams;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.*;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mmarini.ArgumentsGenerator.createStream;
import static org.mmarini.ArgumentsGenerator.uniform;
import static org.mmarini.railways.Matchers.*;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;
import static org.mmarini.railways2.model.MathUtils.normalizeDeg;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_GAP;
import static org.mmarini.railways2.model.blocks.Platforms.PLATFORM_SIGNAL_GAP;

class BlockStationBuilderPlatformsTest {

    public static final double EPSILON = 1e-3;
    public static final int NUM_COACHES = 10;
    public static final double PLATFORM_LENGTH = COACH_LENGTH * NUM_COACHES + PLATFORM_GAP + PLATFORM_SIGNAL_GAP * 2;
    public static final double GAME_DURATION = 300d;
    public static final double FREQUENCY = 0.1;

    static Stream<Arguments> orientationValues() {
        return createStream(10, 1234,
                uniform(-180, 179));
    }

    private BlockStationBuilder builder;

    @Test
    void build() {
        // Given ...
        setUp(0);

        // When ...
        StationStatus status = builder.build();

        // Then ...
        assertThat(status.getRoute("west.in"), allOf(isA(Entry.class),
                hasToString("Entry[west.in]")
        ));
        assertThat(status.getRoute("west.out"), allOf(isA(Exit.class),
                hasToString("Exit[west.out]")
        ));
        assertThat(status.getRoute("east.in"), allOf(isA(Entry.class),
                hasToString("Entry[east.in]")
        ));
        assertThat(status.getRoute("east.out"), allOf(isA(Exit.class),
                hasToString("Exit[east.out]")
        ));
        assertThat(status.getRoute("p.1.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signalw]")
        ));
        assertThat(status.getRoute("p.2.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.2.signalw]")
        ));
        assertThat(status.getRoute("p.1.signale"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signale]")
        ));
        assertThat(status.getRoute("p.2.signale"), allOf(isA(Signal.class),
                hasToString("Signal[p.2.signale]")
        ));
        assertThat(status.getRoute("p.1.signalw"), allOf(isA(Signal.class),
                hasToString("Signal[p.1.signalw]")
        ));
        assertThat(status.getRoute("p.1.w"), allOf(isA(Junction.class),
                hasToString("Junction[p.1.w]")
        ));
        assertThat(status.getRoute("p.2.w"), allOf(isA(Junction.class),
                hasToString("Junction[p.2.w]")
        ));
        assertThat(status.getRoute("east.entry"), allOf(isA(Junction.class),
                hasToString("Junction[east.entry]")
        ));
        assertThat(status.getRoute("east.exit"), allOf(isA(Junction.class),
                hasToString("Junction[east.exit]")
        ));
    }

    @Test
    void buildStationMap() {
        // Given ...
        setUp(0);

        // When ...
        StationMap map = builder.buildStationMap();

        // Then ...
        assertNotNull(map);
        assertEquals("station", map.getId());
        assertThat(map.getNode("west.in"), allOf(
                hasProperty("location", pointCloseTo(-1, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains(hasToString("Track[west.entry.in]")))
        ));
        assertThat(map.getNode("p.1.w"), allOf(
                hasProperty("location", pointCloseTo(0, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder(
                        hasToString("Track[p.1.trackw]"),
                        hasToString("Track[west.exit.out]")
                ))
        ));
        assertThat(map.getNode("p.1.signalw"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, 0, EPSILON)),
                hasProperty("edges", contains(
                        hasToString("Track[p.1.trackw]"),
                        hasToString("Platform[p.1.platform]")
                ))
        ));
        assertThat(map.getNode("p.1.signale"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH - PLATFORM_SIGNAL_GAP, 0, EPSILON)),
                hasProperty("edges", contains(
                        hasToString("Platform[p.1.platform]"),
                        hasToString("Track[p.1.tracke]")
                ))
        ));
        assertThat(map.getNode("east.entry"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder(
                        hasToString("Track[p.1.tracke]"),
                        hasToString("Track[east.entry.in]")
                ))
        ));
        assertThat(map.getNode("east.in"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH + 1, 0, EPSILON)),
                hasProperty("edges", contains(
                        hasToString("Track[east.entry.in]")
                ))
        ));

        assertThat(map.getNode("west.out"), allOf(
                hasProperty("location", pointCloseTo(-1, 0, EPSILON)),
                hasProperty("edges", contains(hasToString("Track[west.exit.out]")))
        ));
        assertThat(map.getNode("p.2.w"), allOf(
                hasProperty("location", pointCloseTo(0, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder(
                        hasToString("Track[p.2.trackw]"),
                        hasToString("Track[west.entry.in]")
                ))
        ));
        assertThat(map.getNode("p.2.signalw"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains(
                        hasToString("Track[p.2.trackw]"),
                        hasToString("Platform[p.2.platform]")
                ))
        ));
        assertThat(map.getNode("p.2.signale"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH - PLATFORM_SIGNAL_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains(
                        hasToString("Platform[p.2.platform]"),
                        hasToString("Track[p.2.tracke]")
                ))
        ));
        assertThat(map.getNode("east.exit"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder(
                        hasToString("Track[p.2.tracke]"),
                        hasToString("Track[east.exit.out]")
                ))
        ));
        assertThat(map.getNode("east.out"), allOf(
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH + 1, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder(
                        hasToString("Track[east.exit.out]")
                ))
        ));

        // Edges
        assertThat(map.getEdge("west.exit.out"), allOf(
                hasProperty("node0", hasToString("Node[p.1.w]")),
                hasProperty("node1", hasToString("Node[west.out]"))
        ));
        assertThat(map.getEdge("p.1.trackw"), allOf(
                hasProperty("node0", hasToString("Node[p.1.w]")),
                hasProperty("node1", hasToString("Node[p.1.signalw]"))
        ));
        assertThat(map.getEdge("p.1.platform"), allOf(
                hasProperty("node0", hasToString("Node[p.1.signalw]")),
                hasProperty("node1", hasToString("Node[p.1.signale]"))
        ));
        assertThat(map.getEdge("p.1.tracke"), allOf(
                hasProperty("node0", hasToString("Node[p.1.signale]")),
                hasProperty("node1", hasToString("Node[east.entry]"))
        ));
        assertThat(map.getEdge("east.entry.in"), allOf(
                hasProperty("node0", hasToString("Node[east.entry]")),
                hasProperty("node1", hasToString("Node[east.in]"))
        ));

        assertThat(map.getEdge("west.entry.in"), allOf(
                hasProperty("node0", hasToString("Node[p.2.w]")),
                hasProperty("node1", hasToString("Node[west.in]"))
        ));
        assertThat(map.getEdge("p.2.trackw"), allOf(
                hasProperty("node0", hasToString("Node[p.2.w]")),
                hasProperty("node1", hasToString("Node[p.2.signalw]"))
        ));
        assertThat(map.getEdge("p.2.platform"), allOf(
                hasProperty("node0", hasToString("Node[p.2.signalw]")),
                hasProperty("node1", hasToString("Node[p.2.signale]"))
        ));
        assertThat(map.getEdge("p.2.tracke"), allOf(
                hasProperty("node0", hasToString("Node[p.2.signale]")),
                hasProperty("node1", hasToString("Node[east.exit]"))
        ));
        assertThat(map.getEdge("east.exit.out"), allOf(
                hasProperty("node0", hasToString("Node[east.exit]")),
                hasProperty("node1", hasToString("Node[east.out]"))
        ));
    }

    @Test
    void createGlobalEdgeBuilders() {
        // Given ...
        setUp(0);

        // When ...
        List<EdgeBuilderParams> builders = builder.createGlobalEdgeParams().collect(Collectors.toList());

        // Then ...
        assertThat(builders, hasSize(10));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("west.entry.in")),
                hasProperty("node0", equalTo("p.2.w")),
                hasProperty("node1", equalTo("west.in"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("west.exit.out")),
                hasProperty("node0", equalTo("p.1.w")),
                hasProperty("node1", equalTo("west.out"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("east.exit.out")),
                hasProperty("node0", equalTo("east.exit")),
                hasProperty("node1", equalTo("east.out"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("east.entry.in")),
                hasProperty("node0", equalTo("east.entry")),
                hasProperty("node1", equalTo("east.in"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.1.platform")),
                hasProperty("node0", equalTo("p.1.signalw")),
                hasProperty("node1", equalTo("p.1.signale"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.2.platform")),
                hasProperty("node0", equalTo("p.2.signalw")),
                hasProperty("node1", equalTo("p.2.signale"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.1.trackw")),
                hasProperty("node0", equalTo("p.1.w")),
                hasProperty("node1", equalTo("p.1.signalw"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.2.trackw")),
                hasProperty("node0", equalTo("p.2.w")),
                hasProperty("node1", equalTo("p.2.signalw"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.2.tracke")),
                hasProperty("node0", equalTo("p.2.signale")),
                hasProperty("node1", equalTo("east.exit"))
        )));
        assertThat(builders, hasItem(allOf(
                hasProperty("id", equalTo("p.1.tracke")),
                hasProperty("node0", equalTo("p.1.signale")),
                hasProperty("node1", equalTo("east.entry"))
        )));
    }

    @Test
    void createInnerNodeBuilders() {
        // Given ...
        setUp(0);

        // When ...
        List<NodeBuilderParams> nodes = builder.createInnerNodeBuilders().collect(Collectors.toList());

        // Then ...
        assertThat(nodes, hasSize(8));
        // Inner node
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.in")),
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH + 1, 0, EPSILON)),
                hasProperty("edges", contains("east.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.out")),
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH + 1, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("east.exit.out")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("west.in")),
                hasProperty("location", pointCloseTo(-1, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("west.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("west.out")),
                hasProperty("location", pointCloseTo(-1, 0, EPSILON)),
                hasProperty("edges", contains("west.exit.out")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.signalw")),
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, 0, EPSILON)),
                hasProperty("edges", contains("p.1.trackw", "p.1.platform")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.signale")),
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH - PLATFORM_GAP, 0, EPSILON)),
                hasProperty("edges", contains("p.1.platform", "p.1.tracke")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.signalw")),
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("p.2.trackw", "p.2.platform")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.signale")),
                hasProperty("location", pointCloseTo(PLATFORM_LENGTH - PLATFORM_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("p.2.platform", "p.2.tracke")))));
    }

    @Test
    void createInnerRoutes() {
        // Given ...
        setUp(0);

        // When ...
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> routes = builder.createInnerRoutes().collect(Collectors.toList());

        // Then ...
        assertThat(routes, hasSize(8));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("west.in"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("west.out"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("east.in"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("east.out"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.1.signalw"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.1.signale"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.2.signalw"))));
        assertThat(routes, hasItem(tupleOf(isA(Function.class),
                contains("p.2.signale"))));
    }

    @Test
    void createJunctionRoutes() {
        // Given ...
        setUp(0);

        // When ...
        List<Tuple2<Function<Node[], ? extends Route>, List<String>>> junctions = builder.createJunctionRoutes().collect(Collectors.toList());

        // Then ...
        assertThat(junctions, hasSize(4));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("p.1.w"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("p.2.w"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("east.entry"))));
        assertThat(junctions, hasItem(tupleOf(isA(Function.class),
                contains("east.exit"))));
    }

    @Test
    void createNodeBuilders() {
        // Given ...
        setUp(0);

        // When ...
        List<NodeBuilderParams> nodes = builder.createNodeBuilders().collect(Collectors.toList());

        // Then ...
        double length = PLATFORM_LENGTH;
        assertThat(nodes, hasSize(12));
        // Junctions
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.w")),
                hasProperty("location", pointCloseTo(0, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder("west.exit.out", "p.1.trackw")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.w")),
                hasProperty("location", pointCloseTo(0, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder("west.entry.in", "p.2.trackw")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.entry")),
                hasProperty("location", pointCloseTo(length, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder("p.1.tracke", "east.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.exit")),
                hasProperty("location", pointCloseTo(length, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder("p.2.tracke", "east.exit.out")))));

        // Inner node
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.in")),
                hasProperty("location", pointCloseTo(length + 1, 0, EPSILON)),
                hasProperty("edges", contains("east.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.out")),
                hasProperty("location", pointCloseTo(length + 1, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("east.exit.out")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("west.in")),
                hasProperty("location", pointCloseTo(-1, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("west.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("west.out")),
                hasProperty("location", pointCloseTo(-1, 0, EPSILON)),
                hasProperty("edges", contains("west.exit.out")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.signalw")),
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, 0, EPSILON)),
                hasProperty("edges", contains("p.1.trackw", "p.1.platform")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.signale")),
                hasProperty("location", pointCloseTo(length - PLATFORM_GAP, 0, EPSILON)),
                hasProperty("edges", contains("p.1.platform", "p.1.tracke")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.signalw")),
                hasProperty("location", pointCloseTo(PLATFORM_SIGNAL_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("p.2.trackw", "p.2.platform")))));

        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.signale")),
                hasProperty("location", pointCloseTo(length - PLATFORM_GAP, TRACK_GAP, EPSILON)),
                hasProperty("edges", contains("p.2.platform", "p.2.tracke")))));
    }

    @Test
    void getGlobalEdgeBuilder() {
        // Given ...
        setUp(0);

        // When ...
        EdgeBuilderParams eb = builder.getGlobalEdgeParams("west",
                EdgeBuilderParams.track("t", "entry", "in"));

        // Then ...
        assertNotNull(eb);
        assertEquals("west.t", eb.getId());
        assertEquals("p.2.w", eb.getNode0());
        assertEquals("west.in", eb.getNode1());

    }

    @Test
    void getGlobalNodeId() {
        // Given ...
        setUp(0);

        // When ... // Then ...
        assertEquals("p.1.w", builder.getGlobalNodeId("west.exit"));
        assertEquals("p.2.w", builder.getGlobalNodeId("west.entry"));
        assertEquals("p.1.w", builder.getGlobalNodeId("p.1.w"));
        assertEquals("p.2.w", builder.getGlobalNodeId("p.2.w"));
        assertEquals("east.entry", builder.getGlobalNodeId("p.1.e"));
        assertEquals("east.exit", builder.getGlobalNodeId("p.2.e"));
        assertEquals("east.entry", builder.getGlobalNodeId("east.entry"));
        assertEquals("east.exit", builder.getGlobalNodeId("east.exit"));
    }

    @Test
    void getJunctionNodeParams() {
        // Given ...
        setUp(0);

        // When ...
        Collection<NodeBuilderParams> nodes = builder.getJunctionNodeParams();

        // Then ...
        double length = PLATFORM_LENGTH;
        assertThat(nodes, hasSize(4));
        // Junctions
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.1.w")),
                hasProperty("location", pointCloseTo(0, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder("west.exit.out", "p.1.trackw")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("p.2.w")),
                hasProperty("location", pointCloseTo(0, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder("p.2.trackw", "west.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.entry")),
                hasProperty("location", pointCloseTo(length, 0, EPSILON)),
                hasProperty("edges", containsInAnyOrder("p.1.tracke", "east.entry.in")))));
        assertThat(nodes, hasItem(allOf(
                hasProperty("id", equalTo("east.exit")),
                hasProperty("location", pointCloseTo(length, TRACK_GAP, EPSILON)),
                hasProperty("edges", containsInAnyOrder("p.2.tracke", "east.exit.out")))));
    }

    @ParameterizedTest
    @MethodSource("orientationValues")
    void getJunctionParams(int orientation) {
        // Given ...
        setUp(orientation);

        // When ...
        Tuple2<Point2D, List<String>> pw1 = builder.getJunctionParams("p.1.w");
        Tuple2<Point2D, List<String>> pw2 = builder.getJunctionParams("p.2.w");
        Tuple2<Point2D, List<String>> eastEntry = builder.getJunctionParams("east.entry");
        Tuple2<Point2D, List<String>> eastExit = builder.getJunctionParams("east.exit");

        // Then ...
        double x1 = PLATFORM_LENGTH * cos(toRadians(orientation));
        double y1 = PLATFORM_LENGTH * sin(toRadians(orientation));
        double x2 = -TRACK_GAP * sin(toRadians(orientation));
        double y2 = TRACK_GAP * cos(toRadians(orientation));
        assertThat(pw1, tupleOf(
                pointSnapTo(0, 0),
                contains("p.1.w", "west.exit")));
        assertThat(pw2, tupleOf(
                pointSnapTo(x2, y2),
                contains("p.2.w", "west.entry")));
        assertThat(eastEntry, tupleOf(
                pointSnapTo(x1, y1),
                contains("east.entry", "p.1.e")));
        assertThat(eastExit, tupleOf(
                pointSnapTo(x1 + x2, y1 + y2),
                contains("east.exit", "p.2.e")));
    }

    @ParameterizedTest
    @MethodSource("orientationValues")
    void getNodeByBlockPoint(int orientation) {
        // Given ...
        setUp(orientation);

        // When ...
        Map<String, String> nodes = builder.getNodeIdByBlockPointId();

        // Then ...
        assertThat(nodes, hasEntry("p.1.w", "p.1.w"));
        assertThat(nodes, hasEntry("west.exit", "p.1.w"));
        assertThat(nodes, hasEntry("p.2.w", "p.2.w"));
        assertThat(nodes, hasEntry("west.entry", "p.2.w"));
        assertThat(nodes, hasEntry("east.entry", "east.entry"));
        assertThat(nodes, hasEntry("p.1.e", "east.entry"));
        assertThat(nodes, hasEntry("east.exit", "east.exit"));
        assertThat(nodes, hasEntry("p.2.e", "east.exit"));
        /*
        double length = NUM_COACHES * COACH_LENGTH + PLATFORM_GAP + PLATFORM_SIGNAL_GAP * 2;
        double x0 = length * cos(toRadians(orientation));
        double y0 = length * sin(toRadians(orientation));
        double x1 = -TRACK_GAP * sin(toRadians(orientation));
        double y1 = TRACK_GAP * cos(toRadians(orientation));
        assertThat(nodes, hasEntry(equalTo("p.1.w"), allOf(
                hasProperty("id", equalTo("p.1.w")),
                hasProperty("location", pointCloseTo(0, 0, EPSILON1)))));
        assertThat(nodes, hasEntry(equalTo("west.exit"), allOf(
                hasProperty("id", equalTo("p.1.w")),
                hasProperty("location", pointCloseTo(0, 0, EPSILON1)))));

        assertThat(nodes, hasEntry(equalTo("p.2.w"), allOf(
                hasProperty("id", equalTo("p.2.w")),
                hasProperty("location", pointCloseTo(x1, y1, EPSILON1)))));
        assertThat(nodes, hasEntry(equalTo("west.entry"), allOf(
                hasProperty("id", equalTo("p.2.w")),
                hasProperty("location", pointCloseTo(x1, y1, EPSILON1)))));

        assertThat(nodes, hasEntry(equalTo("east.entry"), allOf(
                hasProperty("id", equalTo("east.entry")),
                hasProperty("location", pointCloseTo(x0, y0, EPSILON1)))));
        assertThat(nodes, hasEntry(equalTo("p.1.e"), allOf(
                hasProperty("id", equalTo("east.entry")),
                hasProperty("location", pointCloseTo(x0, y0, EPSILON1)))));

        assertThat(nodes, hasEntry(equalTo("east.exit"), allOf(
                hasProperty("id", equalTo("east.exit")),
                hasProperty("location", pointCloseTo(x0 + x1, y0 + y1, EPSILON1)))));
        assertThat(nodes, hasEntry(equalTo("p.2.e"), allOf(
                hasProperty("id", equalTo("east.exit")),
                hasProperty("location", pointCloseTo(x0 + x1, y0 + y1, EPSILON1)))));

         */
    }

    @ParameterizedTest
    @MethodSource("orientationValues")
    void getWorldBlockGeometry(int orientation) {
        // Given ...
        setUp(orientation);

        // When ...
        OrientedGeometry p = builder.getWorldBlockGeometry("p");
        OrientedGeometry west = builder.getWorldBlockGeometry("west");
        OrientedGeometry east = builder.getWorldBlockGeometry("east");


        // Then ...
        assertThat(p, orientedGeometry(
                pointCloseTo(0, 0, EPSILON),
                equalTo(orientation)));
        assertThat(west, orientedGeometry(
                pointCloseTo(-TRACK_GAP * sin(toRadians(orientation)),
                        TRACK_GAP * cos(toRadians(orientation)), EPSILON),
                equalTo(normalizeDeg(orientation + 180))));
        assertThat(east, orientedGeometry(
                pointCloseTo((COACH_LENGTH * NUM_COACHES + PLATFORM_GAP + PLATFORM_SIGNAL_GAP * 2) * cos(toRadians(orientation)),
                        (COACH_LENGTH * NUM_COACHES + PLATFORM_GAP + PLATFORM_SIGNAL_GAP * 2) * sin(toRadians(orientation)),
                        EPSILON),
                equalTo(orientation)));
    }

    @ParameterizedTest
    @MethodSource("orientationValues")
    void getWorldGeometry(int orientation) {
        // Given ...
        setUp(orientation);

        // When ...
        OrientedGeometry pw1 = builder.getWorldGeometry("p.1.w");
        OrientedGeometry pe1 = builder.getWorldGeometry("p.1.e");
        OrientedGeometry pw2 = builder.getWorldGeometry("p.2.w");
        OrientedGeometry pe2 = builder.getWorldGeometry("p.2.e");
        OrientedGeometry eastEntry = builder.getWorldGeometry("east.entry");
        OrientedGeometry eastExit = builder.getWorldGeometry("east.exit");
        OrientedGeometry westEntry = builder.getWorldGeometry("west.entry");
        OrientedGeometry westExit = builder.getWorldGeometry("west.exit");

        // Then ...
        double x1 = PLATFORM_LENGTH * cos(toRadians(orientation));
        double y1 = PLATFORM_LENGTH * sin(toRadians(orientation));
        double x2 = -TRACK_GAP * sin(toRadians(orientation));
        double y2 = TRACK_GAP * cos(toRadians(orientation));
        assertThat(pw1.getPoint(), pointCloseTo(0, 0, EPSILON));
        assertThat(pw2.getPoint(), pointCloseTo(x2, y2, EPSILON));
        assertThat(pe1.getPoint(), pointCloseTo(x1, y1, EPSILON));
        assertThat(pe2.getPoint(), pointCloseTo(x1 + x2, y1 + y2, EPSILON));
        assertThat(eastExit.getPoint(), pointCloseTo(x1 + x2, y1 + y2, EPSILON));
        assertThat(westExit.getPoint(), pointCloseTo(0, 0, EPSILON));
        assertThat(eastEntry.getPoint(), pointCloseTo(x1, y1, EPSILON));
        assertThat(westEntry.getPoint(), pointCloseTo(x2, y2, EPSILON));
    }

    /*
     * west.entry --- p.2.w --- p.2.e --- east.exit
     *
     * west.exit  --- p.1.w --- p.1.e --- east.entry
     */
    void setUp(int orientation) {
        Wayout east = Wayout.create("east");
        Platforms platforms = Platforms.create("p", 2, NUM_COACHES);
        Wayout west = Wayout.create("west");
        List<Block> blocks = List.of(east, platforms, west);
        Map<String, String> links = Map.of(
                "west.entry", "p.2.w",
                "east.entry", "p.1.e");
        StationDef station = StationDef.create("station", orientation, blocks, links);
        this.builder = new BlockStationBuilder(station, GAME_DURATION, FREQUENCY, null);
    }
}