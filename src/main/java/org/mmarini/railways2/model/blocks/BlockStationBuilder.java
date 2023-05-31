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

import org.mmarini.LazyValue;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Route;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.toRadians;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.mkString;
import static org.mmarini.railways2.model.MathUtils.normalizeDeg;
import static org.mmarini.railways2.model.MathUtils.snap;
import static org.mmarini.railways2.model.RailwayConstants.DEFAULT_TRAIN_FREQUENCY;

/**
 * Creates a station from station definition.
 * <p>
 *     <ul>
 * <li>The block geometries (location and orientation of blocks) are constructed by traversing the station definitions.</li>
 * <li>The block geometries are validated to ensure that all blocks are defined.</li>
 * <li>Each connection points are mapped to the grid snap points.</li>
 * <li>Each connection is joint to the corresponding connection by grid location point.</li>
 * <li>Each connection is validated to ensure it is joint to other connection and orientation is consistent.</li>
 * <li>Than nodes are created for each connection plus the inner block node definition.</li>
 * <li>The edges between nodes are created.</li>
 * <li>Finally the routes are created and mapped to the nodes.</li>
 *     </ul>
 * </p>
 */
public class BlockStationBuilder {
    public static final double GRID_SIZE = 1e-3;

    /**
     * Returns the transformation from the block geometry to the world geometry
     * for the given block geometry expressed in world geometry
     *
     * @param worldBlockGeo the world block geometry
     */
    static UnaryOperator<OrientedGeometry> block2WorldGeo(OrientedGeometry worldBlockGeo) {
        Point2D point = worldBlockGeo.getPoint();
        AffineTransform tr = AffineTransform.getTranslateInstance(point.getX(), point.getY());
        tr.rotate(toRadians(worldBlockGeo.getOrientation()));
        int beta2 = worldBlockGeo.getOrientation();
        return pointBlockGeo -> {
            int beta0 = normalizeDeg(beta2 + pointBlockGeo.getOrientation());
            Point2D p0 = tr.transform(pointBlockGeo.getPoint(), null);
            return new OrientedGeometry(p0, beta0);
        };
    }

    /**
     * Returns the world block geometry given the world point geometry and block point geometry
     *
     * @param worldPointGeo the point world geometry
     * @param blockPointGeo the point block geometry
     */
    static OrientedGeometry worldBlockGeo(OrientedGeometry worldPointGeo, OrientedGeometry blockPointGeo) {
        Point2D p0 = worldPointGeo.getPoint();
        Point2D p1 = blockPointGeo.getPoint();
        int alpha0 = worldPointGeo.getOrientation();
        int alpha1 = blockPointGeo.getOrientation();
        int alpha2 = normalizeDeg(alpha0 - alpha1);
        AffineTransform tr = AffineTransform.getTranslateInstance(p0.getX(), p0.getY());
        tr.rotate(toRadians(alpha2));
        tr.translate(-p1.getX(), -p1.getY());
        Point2D p2 = new Point2D.Double();
        tr.transform(p2, p2);
        return new OrientedGeometry(p2, alpha2);
    }

    private final Station station;
    private final LazyValue<Map<String, OrientedGeometry>> worldBlockGeometries;
    private final LazyValue<Map<String, Point2D>> snapPointByBlockPoint;
    private final LazyValue<Map<String, String>> nodeIdByBlockPointId;
    private final LazyValue<Collection<NodeBuilderParams>> junctionNodeParams;

    /**
     * Creates the builder
     *
     * @param station the station definition
     */
    public BlockStationBuilder(Station station) {
        this.station = requireNonNull(station);
        worldBlockGeometries = new LazyValue<>(this::createsBlockGeometries);
        snapPointByBlockPoint = new LazyValue<>(this::createSnapPoints);
        nodeIdByBlockPointId = new LazyValue<>(this::createNodeByBlockPoint);
        junctionNodeParams = new LazyValue<>(this::createJunctionNodes);
    }

    /**
     * Returns the station built from the document
     * <p>
     * The build is performed in a sets of phases:
     * <ol>
     * <li>computes the location points of each block based on connections between blocks</li>
     * <li>makes unique the locations for each node</li>
     * <li>builds nodes and edges list by node</li>
     * <li>builds edges with node identifiers</li>
     * <li>builds station map</li>
     * <li>builds routes with nodes references</li>
     * <li>builds initial status</li>
     * </l>
     * </p>
     */
    public StationStatus build() {
        StationStatus.Builder statusBuilder = new StationStatus.Builder(buildStationMap(), DEFAULT_TRAIN_FREQUENCY);
        createRoutes().forEach(t -> statusBuilder.addRoute(t._1, t._2.toArray(String[]::new)));
        return statusBuilder.build();
    }

    /**
     * Returns the station map
     */
    StationMap buildStationMap() {
        StationBuilder builder = new StationBuilder(station.getId());
        // Generates the edges
        createGlobalEdgeBuilders().forEach(builder::addEdge);
        createNodeBuilders().forEach(builder::addNode);
        return builder.build();
    }

    /**
     * Returns the global edge builders
     */
    Stream<EdgeBuilder> createGlobalEdgeBuilders() {
        return station.getBlocks().stream()
                .flatMap(b -> b.getEdgeBuilders().stream()
                        .map(eb -> getGlobalEdgeBuilder(b.getId(), eb)));
    }

    /**
     * Returns the inner nodes builder
     */
    Stream<NodeBuilderParams> createInnerNodeBuilders() {
        return station.getBlocks().stream()
                .flatMap(block -> {
                    UnaryOperator<OrientedGeometry> tr = block2WorldGeo(getWorldBlockGeometry(block.getId()));
                    return block.getInnerParams(p -> tr.apply(new OrientedGeometry(p, 0)).getPoint());
                });
    }

    /**
     * Returns the inner routes
     */
    Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> createInnerRoutes() {
        return station.getBlocks().stream()
                .flatMap(block -> block.getInnerRouteParams().stream()
                        .map(parms ->
                                parms.setV2(parms._2.stream()
                                        .map(id -> block.getId() + "." + id)
                                        .collect(Collectors.toList()))));
    }

    /**
     * Returns the junctions
     */
    List<NodeBuilderParams> createJunctionNodes() {
        return Tuple2.stream(
                        Tuple2.stream(getNodeIdByBlockPointId())
                                .collect(Collectors.groupingBy(Tuple2::getV2)))
                .map(t -> {
                    String nodeId = t._1;
                    String edgeId0 = station.decodeConnection(t._2.get(0)._1).getEdgeId();
                    String edgeId1 = station.decodeConnection(t._2.get(1)._1).getEdgeId();
                    Point2D location = getSnapPointsByBlockPoint().get(nodeId);
                    return NodeBuilderParams.create(nodeId, location.getX(), location.getY(), edgeId0, edgeId1);
                }).collect(Collectors.toList());
    }

    /**
     * Returns the junction routes
     */
    Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> createJunctionRoutes() {
        Function<Node[], ? extends Route> f = Junction::create;
        return getJunctionNodeParams().stream()
                .map(params ->
                        Tuple2.of(f, List.of(params.getId()))
                );
    }

    /**
     * Returns the node building parameters
     */
    Stream<NodeBuilderParams> createNodeBuilders() {
        Stream<NodeBuilderParams> junctions = getJunctionNodeParams().stream();
        Stream<NodeBuilderParams> inners = createInnerNodeBuilders();
        return Stream.concat(junctions, inners);
    }

    /**
     * Returns the node by block point after link validation
     */
    private Map<String, String> createNodeByBlockPoint() {
        // Creates node id by block point id
        Map<String, String> result = Tuple2.stream(
                        // Create block point list by snap point
                        Tuple2.stream(getSnapPointsByBlockPoint())
                                .collect(Collectors.groupingBy(Tuple2::getV2)))
                .flatMap(t -> {
                    // Find node id = min of block point id
                    String nodeId = t._2.stream()
                            .map(Tuple2::getV1)
                            .min(Comparator.naturalOrder())
                            .orElseThrow();
                    // returns block point ids, node id
                    return t._2.stream().map(t1 -> Tuple2.of(t1._1, nodeId));
                })
                .collect(Tuple2.toMap());
        // Validates
        Map<String, List<Tuple2<String, String>>> blockPointsByNode = Tuple2.stream(result)
                .collect(Collectors.groupingBy(Tuple2::getV2));
        // Duplicated
        List<String> duplicated = Tuple2.stream(blockPointsByNode)
                .filter(t -> t._2.size() > 2)
                .map(t -> format("[%s]",
                        mkString(t._2.stream().map(Tuple2::getV1), ",")))
                .collect(Collectors.toList());
        if (!duplicated.isEmpty()) {
            throw new IllegalArgumentException(format("%s have more then one connection",
                    mkString(duplicated, ", ")));
        }
        // Missing
        List<String> missing = Tuple2.stream(blockPointsByNode)
                .filter(t -> t._2.size() < 2)
                .map(t -> t._2.get(0)._1)
                .sorted()
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(format("[%s] have no connection",
                    mkString(missing, ", ")));
        }
        return result;
    }

    /**
     * Returns the routes building parameters
     */
    private Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> createRoutes() {
        Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> junctionRoutes = createJunctionRoutes();
        Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRoutes = createInnerRoutes();
        return Stream.concat(junctionRoutes, innerRoutes);
    }

    /**
     * Returns the snap point by block point
     */
    private Map<String, Point2D> createSnapPoints() {
        return station.getBlockPoints().stream()
                .map(blockPoint -> {
                    Block block = blockPoint.getBlock();
                    OrientedGeometry worldBlockGeometry = getWorldBlockGeometry(block.getId());
                    UnaryOperator<OrientedGeometry> tr = block2WorldGeo(worldBlockGeometry);
                    OrientedGeometry entryGeometry = blockPoint.getEntryGeometry();
                    OrientedGeometry worldPointGeo = tr.apply(entryGeometry);
                    Point2D snapPoint = snap(worldPointGeo.getPoint(), GRID_SIZE);
                    return Tuple2.of(blockPoint.toString(), snapPoint);
                }).collect(Tuple2.toMap());
    }

    /**
     * Returns the block geometries by traversing the station definitions
     */
    private Map<String, OrientedGeometry> createsBlockGeometries() {
        // find reference platform
        Platforms ref = station.getBlocks().stream()
                .filter(b -> b instanceof Platforms)
                .map(a -> (Platforms) a)
                .min(Comparator.comparing(AbstractBlock::getId))
                .orElseThrow();
        Map<String, OrientedGeometry> worldBlockGeometries = new HashMap<>();
        OrientedGeometry geometry = new OrientedGeometry(new Point2D.Double(), station.getOrientation());
        traverseForWorldBlockGeometry(worldBlockGeometries, ref, geometry);
        // Validates blocks
        List<String> missing = station.getBlocks().stream()
                .map(Block::getId)
                .filter(Predicate.not(worldBlockGeometries::containsKey))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(format("Missing connection with blocks [%s]",
                    mkString(missing, ", ")));
        }
        return worldBlockGeometries;
    }

    /**
     * Returns the global edge builder
     * <p>
     * Transforms the local edge id to global edge id prefixing with block id and dot
     * Transforms the local node ids to global node ids prefixing with block id and dot
     * then mapping to global node name (the junction naming)
     * </p>
     *
     * @param blockId     the block id
     * @param edgeBuilder the local edge builder
     */
    EdgeBuilder getGlobalEdgeBuilder(String blockId, EdgeBuilder edgeBuilder) {
        // Transform local reference to global reference
        String prefix = blockId + ".";
        return edgeBuilder.setId(prefix + edgeBuilder.getId())
                .setNode0(getGlobalNodeId((prefix + edgeBuilder.getNode0())))
                .setNode1(getGlobalNodeId(prefix + edgeBuilder.getNode1()));
    }

    /**
     * Returns the global node identifier
     * Maps the full local alias node identifier to the station node identifiers
     *
     * @param aliasId the full local alias node identifier
     */
    String getGlobalNodeId(String aliasId) {
        String nodeId = getNodeIdByBlockPointId().get(aliasId);
        return nodeId == null ? aliasId : nodeId;
    }

    /**
     * Returns the junction node parameters
     */
    Collection<NodeBuilderParams> getJunctionNodeParams() {
        return junctionNodeParams.get();
    }

    /**
     * Returns the node definition by block point id
     */
    Map<String, String> getNodeIdByBlockPointId() {
        return nodeIdByBlockPointId.get();
    }

    /**
     * Returns the connection points (lazy value)
     */
    Map<String, Point2D> getSnapPointsByBlockPoint() {
        return snapPointByBlockPoint.get();
    }

    /**
     * Returns the world block geometry by block (lazy value)
     */
    Map<String, OrientedGeometry> getWorldBlockGeometries() {
        return worldBlockGeometries.get();
    }

    /**
     * Returns the world geometry of a block
     *
     * @param blockId the block
     */
    private OrientedGeometry getWorldBlockGeometry(String blockId) {
        return getWorldBlockGeometries().get(blockId);
    }

    /**
     * Traverses the tree of connections and builds the block geometries
     *
     * @param worldBlockGeometries the worlds block geometry by block
     * @param ref                  the block reference
     * @param worldRefGeometry     the world block reference geometry
     */
    private void traverseForWorldBlockGeometry(Map<String, OrientedGeometry> worldBlockGeometries, Block ref, OrientedGeometry worldRefGeometry) {
        worldBlockGeometries.put(ref.getId(), worldRefGeometry);
        // retrieves the declared junctions for the ref block
        List<BlockJunction> links = station.getJunctions(ref);
        // Get the geometry transformation for the given block
        UnaryOperator<OrientedGeometry> block2World = block2WorldGeo(worldRefGeometry);
        // for each connection adds geometry to block without geometry
        for (BlockJunction link : links) {
            BlockPoint selfPoint = link.getByBlock(ref);
            BlockPoint otherPoint = link.getOther(selfPoint);
            Block otherBlock = otherPoint.getBlock();
            if (!worldBlockGeometries.containsKey(otherBlock.getId())) {
                // gets the world connection geometry
                OrientedGeometry worldConnGeo = block2World.apply(selfPoint.getEntryGeometry());
                // gets the block connection geometry
                OrientedGeometry blockConnGeo = otherPoint.getEntryGeometry().opposite();
                // gets the world block geometry
                OrientedGeometry worldBlockGeo = worldBlockGeo(worldConnGeo, blockConnGeo);
                // run recursively
                traverseForWorldBlockGeometry(worldBlockGeometries, otherBlock, worldBlockGeo);
            }
        }
    }
}
