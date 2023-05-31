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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.geom.Point2D;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mmarini.railways.Matchers.pointCloseTo;
import static org.mmarini.railways2.model.Matchers.orientedGeometry;

class BlockStationBuilderTest {

    public static final double EPSILON = 1e-3;

    @ParameterizedTest
    @CsvSource(value = {
            "1,1,0,     0,0,0,   1,1,0", // Identity
            "1,1,90,     0,0,0,   1,1,90", // Identity
            "-1,-1,-180,     0,0,-180,   1,1,0", // Rotate 180DEG
            "-1,-1,-170,     0,0,-180,   1,1,10", // Rotate 180DEG
            "-1,1,100,     0,0,90,   1,1,10", // Rotate 180DEG
            "1,1,0,     1,1,0,   0,0,0", // translate
            "2,3,0,     1,1,0,   1,2,0", // translate
            "0,2,110,     1,1,90,   1,1,20", // translate and rotate
    })
    void block2WorldGeo(double worldPointX, double worldPointY, int worldPointDeg,
                        double worldBlockX, double worldBlockY, int worldBlockDeg,
                        double blockPointX, double blockPointY, int blockPointDeg) {
        // Given ...
        OrientedGeometry worldBlockGeo = new OrientedGeometry(new Point2D.Double(worldBlockX, worldBlockY), worldBlockDeg);
        OrientedGeometry blockPointGeo = new OrientedGeometry(new Point2D.Double(blockPointX, blockPointY), blockPointDeg);

        // When ...
        OrientedGeometry wordPointGeo = BlockStationBuilder.block2WorldGeo(worldBlockGeo).apply(blockPointGeo);

        // Then ...
        assertThat(wordPointGeo, orientedGeometry(
                pointCloseTo(worldPointX, worldPointY, EPSILON),
                equalTo(worldPointDeg)
        ));
    }

    @ParameterizedTest
    @CsvSource(value = { // wp, wb, bp -> wb, wp, bp
            "0,0,0,   1,1,0,     1,1,0", // Identity
            "0,0,0,   1,1,90,     1,1,90", // Identity
            "0,0,-180,   -1,-1,-180,     1,1,0", // Rotate 180DEG
            "0,0,-180,   -1,-1,-170,     1,1,10", // Rotate 180DEG
            "0,0,90,   -1,1,100,     1,1,10", // Rotate 180DEG
            "1,1,0,   1,1,0,     0,0,0", // translate
            "1,1,0,   2,3,0,     1,2,0", // translate
            "1,1,90,   0,2,110,     1,1,20", // translate and rotate

    })
    void worldBlockGeo(double worldBlockX, double worldBlockY, int worldBlockDeg,
                       double worldPointX, double worldPointY, int worldPointDeg,
                       double blockPointX, double blockPointY, int blockPointDeg) {
        // Given ...
        OrientedGeometry worldPointGeo = new OrientedGeometry(new Point2D.Double(worldPointX, worldPointY), worldPointDeg);
        OrientedGeometry blockPointGeo = new OrientedGeometry(new Point2D.Double(blockPointX, blockPointY), blockPointDeg);

        // When ...
        OrientedGeometry worldBlockGeo = BlockStationBuilder.worldBlockGeo(worldPointGeo, blockPointGeo);

        // Then ...
        assertThat(worldBlockGeo, orientedGeometry(
                pointCloseTo(worldBlockX, worldBlockY, EPSILON),
                equalTo(worldBlockDeg)
        ));
    }
}