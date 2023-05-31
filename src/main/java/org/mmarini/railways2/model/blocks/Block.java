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
import org.mmarini.railways2.model.geometry.EdgeBuilder;
import org.mmarini.railways2.model.geometry.Node;
import org.mmarini.railways2.model.geometry.NodeBuilderParams;
import org.mmarini.railways2.model.routes.Route;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A generic block
 * A block consists of an ordered set of points and a key identified set of connections
 * Each connection is associated to entry and exit directions
 */
public interface Block {

    /**
     * Returns the block points
     */
    Collection<BlockPoint> getBlockPoints();

    /**
     * Returns the edge builder
     */
    List<EdgeBuilder> getEdgeBuilders();

    /**
     * Returns the edge identifier
     *
     * @param blockPointId the local block point identifier
     */
    String getEdgeId(String blockPointId);

    /**
     * Returns the geometry of the entry
     *
     * @param id the connection id
     */
    OrientedGeometry getEntryGeometry(String id);

    /**
     * Returns the identifier
     */
    String getId();

    /**
     * Returns the inner node building parameters list
     */
    List<NodeBuilderParams> getInnerParams();

    /**
     * Returns the inner global parametrs nodes
     *
     * @param transformer the transformation from inner point to world point
     */
    Stream<NodeBuilderParams> getInnerParams(UnaryOperator<Point2D> transformer);

    /**
     * Returns the inner routes
     */
    Collection<Tuple2<Function<Node[], ? extends Route>, List<String>>> getInnerRouteParams();

    /**
     * Returns the location of connection
     *
     * @param id the connection id
     */
    default Point2D getLocation(String id) {
        return getEntryGeometry(id).getPoint();
    }
}
