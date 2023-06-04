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
import org.mmarini.railways2.model.geometry.EdgeBuilderParams;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.routes.Route;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * An abstract block with identifier
 */
public abstract class AbstractBlock implements Block {

    protected final String id;
    protected final Map<String, OrientedGeometry> geometryById;
    protected final List<NodeBuilderParams> innerParams;
    protected final List<EdgeBuilderParams> edgeBuilderParams;
    protected final Map<String, String> edgeByBlockPoint;
    protected final Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams;
    private final LazyValue<Collection<BlockPoint>> blockPoints;

    /**
     * Creates the abstract block
     *
     * @param id                the identifier
     * @param geometryById      the orientation by identifier
     * @param innerParams       the inner parameters by identifier
     * @param edgeBuilderParams the edge builders
     * @param edgeByBlockPoint  the edge by block point
     * @param innerRouteParams  the inner building route parameters
     */
    protected AbstractBlock(String id, Map<String, OrientedGeometry> geometryById,
                            List<NodeBuilderParams> innerParams, List<EdgeBuilderParams> edgeBuilderParams,
                            Map<String, String> edgeByBlockPoint, Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> innerRouteParams) {
        this.id = requireNonNull(id);
        this.geometryById = requireNonNull(geometryById);
        this.innerParams = requireNonNull(innerParams);
        this.edgeBuilderParams = requireNonNull(edgeBuilderParams);
        this.edgeByBlockPoint = requireNonNull(edgeByBlockPoint);
        this.innerRouteParams = innerRouteParams;
        this.blockPoints = new LazyValue<>(this::createBlockPoints);
    }

    /**
     * Returns the list of block points
     */
    private Collection<BlockPoint> createBlockPoints() {
        return geometryById.keySet().stream().map(id -> new BlockPoint(this, id)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return id.equals(that.id);
    }

    @Override
    public Collection<BlockPoint> getBlockPoints() {
        return blockPoints.get();
    }

    @Override
    public String getEdgeId(String blockPointId) {
        String edgeId = edgeByBlockPoint.get(blockPointId);
        if (edgeId == null) {
            throw new IllegalArgumentException(format("Block %s does not contain connection %s",
                    this.id, blockPointId));
        }
        return edgeId;
    }

    @Override
    public List<EdgeBuilderParams> getEdgeParams() {
        return edgeBuilderParams;
    }

    @Override
    public OrientedGeometry getEntryGeometry(String id) {
        OrientedGeometry geometry = getGeometryById().get(id);
        if (geometry == null) {
            throw new IllegalArgumentException(format("Block %s does not contain connection %s",
                    this.id, id));
        }
        return geometry;
    }

    /**
     * Returns the inner point by identifier
     */
    Map<String, OrientedGeometry> getGeometryById() {
        return geometryById;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Stream<NodeBuilderParams> getInnerParams(UnaryOperator<Point2D> transformer) {
        return getInnerParams().stream()
                .map(localParams -> localParams.setId(getId() + "." + localParams.getId())
                        .setLocation(transformer.apply(localParams.getLocation()))
                        .setEdges(localParams.getEdges().stream()
                                .map(id -> getId() + "." + id)
                                .collect(Collectors.toList())));
    }

    @Override
    public List<NodeBuilderParams> getInnerParams() {
        return innerParams;
    }

    @Override
    public Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> getInnerRouteParams() {
        return innerRouteParams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add(id)
                .toString();
    }
}
