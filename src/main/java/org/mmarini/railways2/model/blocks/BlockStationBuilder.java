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
import org.mmarini.railways2.model.SoundEvent;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Route;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.Utils.mkString;
import static org.mmarini.railways2.model.MathUtils.GRID_SIZE;
import static org.mmarini.railways2.model.MathUtils.snapToGrid;

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

    private final StationDef station;
    private final double gameDuration;
    private final LazyValue<Map<String, OrientedGeometry>> worldBlockGeometries;
    private final LazyValue<Map<String, String>> nodeIdByBlockPointId;
    private final LazyValue<Collection<NodeBuilderParams>> junctionNodeParams;
    private final LazyValue<Map<String, OrientedGeometry>> worldGeometryByBlockPointId;
    private final LazyValue<Map<String, Tuple2<Point2D, List<String>>>> junctionParamsByJunctionId;
    private final Random random;
    private final double frequency;
    private final Subscriber<SoundEvent> events;

    /**
     * Creates the builder
     *
     * @param station      the station definition
     * @param gameDuration the game duration (s)
     * @param frequency    the train frequency (#/s)
     * @param random       the random number generator
     * @param events       the event subscriber
     */
    public BlockStationBuilder(StationDef station, double gameDuration, double frequency, Random random, Subscriber<SoundEvent> events) {
        this.station = requireNonNull(station);
        this.gameDuration = gameDuration;
        this.frequency = frequency;
        this.random = random;
        this.events = events;
        this.worldBlockGeometries = new LazyValue<>(this::createsBlockGeometries);
        this.nodeIdByBlockPointId = new LazyValue<>(this::createNodeIdByBlockPointId);
        this.junctionNodeParams = new LazyValue<>(this::createJunctionNodes);
        this.worldGeometryByBlockPointId = new LazyValue<>(this::createWorldGeometryByBlockPointId);
        this.junctionParamsByJunctionId = new LazyValue<>(this::createJunctionParamsByJunctionId);
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
        StationStatus.Builder statusBuilder = new StationStatus.Builder(buildStationMap(), frequency, gameDuration,
                random, events);
        createRoutes().forEach(t ->
                statusBuilder.addRoute(t._1, t._2.toArray(String[]::new)));
        return statusBuilder.build();
    }

    /**
     * Returns the station map
     */
    StationMap buildStationMap() {
        validateBlocks().validateJunctions();
        StationBuilder builder = new StationBuilder(station.getId());
        // Generates the edges
        createGlobalEdgeParams().forEach(builder::addEdge);
        createNodeBuilders().forEach(builder::addNode);
        return builder.build();
    }

    /**
     * Returns the global edge builders
     */
    Stream<EdgeBuilderParams> createGlobalEdgeParams() {
        return station.getBlocks().stream()
                .flatMap(b -> b.getEdgeParams().stream()
                        .map(eb -> getGlobalEdgeParams(b.getId(), eb)));
    }

    /**
     * Returns the inner nodes builder
     */
    Stream<NodeBuilderParams> createInnerNodeBuilders() {
        return station.getBlocks().stream()
                .flatMap(block -> {
                    UnaryOperator<OrientedGeometry> tr = getWorldBlockGeometry(block.getId()).getBlock2World();
                    return block.getInnerParams(p -> tr.apply(new OrientedGeometry(p, 0)).getPoint());
                });
    }

    /**
     * Returns the inner routes
     */
    Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> createInnerRoutes() {
        return station.getBlocks().stream()
                .flatMap(block -> block.getInnerRouteParams().stream()
                        .map(params ->
                                params.setV2(params._2.stream()
                                        .map(id -> block.getId() + "." + id)
                                        .collect(Collectors.toList()))));
    }

    /**
     * Returns the junctions
     */
    List<NodeBuilderParams> createJunctionNodes() {
        return Tuple2.stream(getJunctionParamsByJunctionId())
                .map(t -> {
                    String junctionId = t._1;
                    Point2D location = t._2._1;
                    List<String> blockPointIds = t._2._2;
                    String edgeId0 = station.decodeConnection(blockPointIds.get(0)).getEdgeId();
                    String edgeId1 = station.decodeConnection(t._2._2.get(1)).getEdgeId();
                    return NodeBuilderParams.create(junctionId, location.getX(), location.getY(), edgeId0, edgeId1);
                }).collect(Collectors.toList());
    }

    /**
     * Returns the junction parameters by junction id
     */
    private Map<String, Tuple2<Point2D, List<String>>> createJunctionParamsByJunctionId() {
        // Groups block points by distance
        Map<Point2D, List<String>> blockPointMap = new HashMap<>();
        for (Map.Entry<String, OrientedGeometry> entry : getWorldGeometryByBlockPointId().entrySet()) {
            String blockPointId = entry.getKey();
            OrientedGeometry geometry = entry.getValue();
            // Find reference point
            Optional<Point2D> refPointOpt = blockPointMap.keySet().stream().filter(
                    point -> geometry.getPoint().distance(point) < GRID_SIZE / 2
            ).findAny();
            // Add reference point and block point
            refPointOpt.ifPresentOrElse(refPoint -> blockPointMap
                            .computeIfPresent(refPoint, (point, list) -> {
                                list.add(blockPointId);
                                return list;
                            }),
                    () ->
                            blockPointMap.computeIfAbsent(geometry.getPoint(), p -> new ArrayList<>(List.of(blockPointId)))

            );
        }

        return Tuple2.stream(blockPointMap).
                map(t -> {
                    // sort block point id
                    Point2D point = t._1;
                    List<String> blockPointIds = t._2.stream()
                            .sorted()
                            .collect(Collectors.toList());
                    // Get the node id (first block id)
                    String nodeId = blockPointIds.get(0);
                    return Tuple2.of(nodeId, Tuple2.of(snapToGrid(point), blockPointIds));
                })
                .collect(Tuple2.toMap());
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
     * Returns node identifier by block point identifier
     */
    private Map<String, String> createNodeIdByBlockPointId() {
        return Tuple2.stream(getJunctionParamsByJunctionId())
                .flatMap(t ->
                        t._2._2.stream()
                                .map(blockPointId -> Tuple2.of(blockPointId, t._1)))
                .collect(Tuple2.toMap());
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
     * Returns the world geometry by block point
     */
    Map<String, OrientedGeometry> createWorldGeometryByBlockPointId() {
        // Create global point by block point identifier
        return station.getBlockPoints().stream()
                .map(blockPoint -> {
                    Block block = blockPoint.getBlock();
                    OrientedGeometry worldBlockGeometry = getWorldBlockGeometry(block.getId());
                    UnaryOperator<OrientedGeometry> tr = worldBlockGeometry.getBlock2World();
                    OrientedGeometry entryGeometry = blockPoint.getEntryGeometry();
                    OrientedGeometry worldPointGeo = tr.apply(entryGeometry);
                    return Tuple2.of(blockPoint.toString(), worldPointGeo);
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
        OrientedGeometry geometry = new OrientedGeometry(new Point2D.Double(), station.getOrientation());
        return traverseForWorldBlockGeometry(new HashMap<>(), ref, geometry);
    }

    /**
     * Returns the global edge builder
     * <p>
     * Transforms the local edge id to global edge id prefixing with block id and dot
     * Transforms the local node ids to global node ids prefixing with block id and dot
     * then mapping to global node name (the junction naming)
     * </p>
     *
     * @param blockId           the block id
     * @param edgeBuilderParams the local edge builder
     */
    EdgeBuilderParams getGlobalEdgeParams(String blockId, EdgeBuilderParams edgeBuilderParams) {
        // Transform local reference to global reference
        String prefix = blockId + ".";
        return edgeBuilderParams.setId(prefix + edgeBuilderParams.getId())
                .setNode0(getGlobalNodeId((prefix + edgeBuilderParams.getNode0())))
                .setNode1(getGlobalNodeId(prefix + edgeBuilderParams.getNode1()));
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
     * Returns the junction parameters (snap point + block point list) of a junction
     *
     * @param junctionId the junction id
     */
    Tuple2<Point2D, List<String>> getJunctionParams(String junctionId) {
        return getJunctionParamsByJunctionId().get(junctionId);
    }

    /**
     * Returns the junction parameters (snap point + block point list) by junction id (lazy value)
     */
    private Map<String, Tuple2<Point2D, List<String>>> getJunctionParamsByJunctionId() {
        return junctionParamsByJunctionId.get();
    }

    /**
     * Returns the node definition by block point id
     */
    Map<String, String> getNodeIdByBlockPointId() {
        return nodeIdByBlockPointId.get();
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
    OrientedGeometry getWorldBlockGeometry(String blockId) {
        return getWorldBlockGeometries().get(blockId);
    }

    /**
     * Returns the world geometry of the block point
     *
     * @param blockPointId the block point identifier
     */
    OrientedGeometry getWorldGeometry(String blockPointId) {
        OrientedGeometry result = getWorldGeometryByBlockPointId().get(blockPointId);
        if (result == null) {
            throw new IllegalArgumentException(format("The station does not contain block point [%s]", blockPointId));
        }
        return result;
    }

    /**
     * Returns the world geometry by block point identifier (lazy value)
     */
    private Map<String, OrientedGeometry> getWorldGeometryByBlockPointId() {
        return worldGeometryByBlockPointId.get();
    }

    /**
     * Traverses the tree of connections and builds the block geometries
     *
     * @param worldBlockGeometries the worlds block geometry by block
     * @param ref                  the block reference
     * @param worldRefGeometry     the world block reference geometry
     */
    private Map<String, OrientedGeometry> traverseForWorldBlockGeometry(Map<String, OrientedGeometry> worldBlockGeometries, Block ref, OrientedGeometry worldRefGeometry) {
        worldBlockGeometries.put(ref.getId(), worldRefGeometry);
        // retrieves the declared junctions for the ref block
        List<BlockJunction> links = station.getJunctions(ref);
        // Get the geometry transformation for the given block
        UnaryOperator<OrientedGeometry> block2World = worldRefGeometry.getBlock2World();
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
                OrientedGeometry worldBlockGeo = blockConnGeo.getWorldBlockGeo(worldConnGeo);
                // run recursively
                traverseForWorldBlockGeometry(worldBlockGeometries, otherBlock, worldBlockGeo);
            }
        }
        return worldBlockGeometries;
    }

    /**
     * Validates the blocks
     */
    BlockStationBuilder validateBlocks() {
        // Validates blocks
        Map<String, OrientedGeometry> geometries = getWorldBlockGeometries();
        List<String> missing = station.getBlocks().stream()
                .map(Block::getId)
                .filter(Predicate.not(geometries::containsKey))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(format("Missing connection with blocks [%s]",
                    mkString(missing, ", ")));
        }
        return this;
    }

    /**
     * Validates the junctions
     */
    void validateJunctions() {
        Map<String, String> result = getNodeIdByBlockPointId();
        // Validates
        Map<String, List<Tuple2<String, String>>> blockPointsByNode = Tuple2.stream(result)
                .collect(Collectors.groupingBy(Tuple2::getV2));
        // Duplicated
        List<String> duplicated = Tuple2.stream(blockPointsByNode)
                .filter(t -> t._2.size() > 2)
                .map(t -> {
                    String junctionId = t._1;
                    Tuple2<Point2D, List<String>> params = getJunctionParams(junctionId);
                    Point2D p = params._1;
                    List<Tuple2<String, String>> blockPointIds = t._2;
                    return format("%s(%.3f, %.3f)-[%s]",
                            junctionId,
                            p.getX(), p.getY(),
                            mkString(blockPointIds.stream()
                                            .map(Tuple2::getV1)
                                            .filter(id -> !t._1.equals(id))
                                    , ", "));
                })
                .collect(Collectors.toList());
        if (!duplicated.isEmpty()) {
            throw new IllegalArgumentException(format("More then one junction: %s",
                    mkString(duplicated, ", ")));
        }
        // Missing
        List<String> missing = Tuple2.stream(blockPointsByNode)
                .filter(t -> t._2.size() < 2)
                .map(t -> t._2.get(0)._1)
                .sorted()
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(format("No junctions for nodes [%s]",
                    mkString(missing, ", ")));
        }
    }
}
