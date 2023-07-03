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

package org.mmarini.railways2.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.blocks.BlockBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.*;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mmarini.railways2.model.geometry.StationMap.DIRECTION_VALIDATOR;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * Loads station status from json node
 */
public class StationStatusLoader {
    public static final Validator ROUTE_VALIDATOR = objectProperties(Map.of(
            "id", string(),
            "lock", arrayItems(DIRECTION_VALIDATOR),
            "through", booleanValue()
    ));
    public static final Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "version", string(values("2.0")),
                    "trainFrequency", positiveNumber(),
                    "performance", ExtendedPerformance.VALIDATOR,
                    "routes", arrayItems(ROUTE_VALIDATOR),
                    "trains", arrayItems(StationStatus.TRAIN_VALIDATOR)
            ), List.of(
                    "version",
                    "trainFrequency",
                    "performance",
                    "routes",
                    "trains"
            )
    );

    /**
     * Returns the station status from dump json node
     *
     * @param root       the root document
     * @param locator    the dump locator
     * @param stationDef the station definition
     */
    public static StationStatus fromJson(JsonNode root, Locator locator, StationDef stationDef) {
        VALIDATOR.apply(locator).accept(root);
        BlockBuilder mapBuilder = new BlockBuilder(stationDef);
        StationMap stationMap = mapBuilder.buildStationMap();
        ExtendedPerformance performance = ExtendedPerformance.fromJson(root, locator.path("performance"));
        double trainFrequency = locator.path("trainFrequency").getNode(root).asDouble();
        Function<Node[], ? extends Route> f = Junction::create;
        Stream<Tuple2<? extends Function<Node[], ? extends Route>, List<String>>> junctionBuilders = mapBuilder.getJunctionNodeParams().stream()
                .map(params ->
                        Tuple2.of(f, List.of(params.getId()))
                );
        Map<String, Route> routesById = mapBuilder.buildRoutes().stream().collect(Collectors.toMap(
                Route::getId, Function.identity()));
        List<Route> routes = locator.path("routes").elements(root)
                .map(routeLocator -> {
                    String id = routeLocator.path("id").getNode(root).asText();
                    Route route = routesById.get(id);
                    if (route instanceof Switch) {
                        boolean through = routeLocator.path("through").getNode(root).asBoolean();
                        return through ? ((Switch) route).through() : ((Switch) route).diverging();
                    } else if (route instanceof DoubleSlipSwitch) {
                        boolean through = routeLocator.path("through").getNode(root).asBoolean();
                        return through ? ((DoubleSlipSwitch) route).through() : ((DoubleSlipSwitch) route).diverging();
                    } else if (route instanceof Signal) {
                        Direction[] locks = routeLocator.path("locks").elements(root)
                                .map(edgeLocator -> {
                                    Edge edge = stationMap.getEdge(edgeLocator.getNode(root).asText());
                                    return new Direction(edge, route.getNodes().get(0));
                                })
                                .toArray(Direction[]::new);
                        return ((Signal) route).setLocks(locks);
                    } else {
                        return route;
                    }
                })
                .collect(Collectors.toList());

        StationStatus status = StationStatus.create(stationMap, routes, trainFrequency, performance);

        List<Train> trains = locator.path("trains").elements(root)
                .map(loc -> status.trainFromJson(root, loc))
                .collect(Collectors.toList());

        return status.setTrains(trains);
    }
}
