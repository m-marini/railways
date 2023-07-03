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

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.SoundEvent;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.Junction;
import org.mmarini.railways2.model.routes.Route;
import org.reactivestreams.Subscriber;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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
public class BlockStationStatusBuilder {
    private final StationDef station;
    private final double gameDuration;
    private final Random random;
    private final double frequency;
    private final Subscriber<SoundEvent> events;
    private final BlockBuilder stationMapBuilder;

    /**
     * Creates the builder
     *
     * @param station      the station definition
     * @param gameDuration the game duration (s)
     * @param frequency    the train frequency (#/s)
     * @param random       the random number generator
     * @param events       the event subscriber
     */
    public BlockStationStatusBuilder(StationDef station, double gameDuration, double frequency, Random random, Subscriber<SoundEvent> events) {
        this.station = requireNonNull(station);
        this.stationMapBuilder = new BlockBuilder(station);
        this.gameDuration = gameDuration;
        this.frequency = frequency;
        this.random = random;
        this.events = events;
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
        StationMap stationMap = stationMapBuilder.buildStationMap();
        StationStatus.Builder statusBuilder = new StationStatus.Builder(stationMap, frequency, gameDuration,
                random, events);
        createRoutes().forEach(t ->
                statusBuilder.addRoute(t._1, t._2.toArray(String[]::new)));
        return statusBuilder.build();
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
     * Returns the junction routes
     */
    Stream<Tuple2<Function<Node[], ? extends Route>, List<String>>> createJunctionRoutes() {
        Function<Node[], ? extends Route> f = Junction::create;
        return stationMapBuilder.getJunctionNodeParams().stream()
                .map(params ->
                        Tuple2.of(f, List.of(params.getId()))
                );
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
     * Returns the world geometry of the block point
     *
     * @param blockPointId the block point identifier
     */
    public OrientedGeometry getWorldGeometry(String blockPointId) {
        return stationMapBuilder.getWorldGeometry(blockPointId);
    }
}
