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

package org.mmarini.railways2.model.routes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways.Matchers;
import org.mmarini.railways2.model.geometry.*;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways.Matchers.emptyOptional;

class DoubleSlipSwitchTest {

    private static final double LENGTH = 100;
    private StationMap station;
    private DoubleSlipSwitch route;

    /*
      a - b - c - d
            X
      e - f - g - h
     */
    @BeforeEach
    void beforeEach() {
        station = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc", "bg")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "cd", "bc", "fc")
                .addNode("d", new Point2D.Double(LENGTH * 3, 0), "cd")
                .addNode("e", new Point2D.Double(0, 50), "ef")
                .addNode("f", new Point2D.Double(LENGTH, 50), "ef", "fg", "fc")
                .addNode("g", new Point2D.Double(LENGTH * 2, 50), "gh", "fg", "bg")
                .addNode("h", new Point2D.Double(LENGTH * 3, 50), "gh")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("cd", "c", "d")
                .addTrack("ef", "e", "f")
                .addTrack("fg", "f", "g")
                .addTrack("gh", "g", "h")
                .addTrack("bg", "b", "g")
                .addTrack("fc", "f", "c")
                .build();
    }

    void createSwitch(boolean through) {
        Node[] nodes = new Node[]{
                station.getNode("b"),
                station.getNode("c"),
                station.getNode("f"),
                station.getNode("g")
        };
        route = through ?
                DoubleSlipSwitch.through(nodes) :
                DoubleSlipSwitch.diverging(nodes);
    }

    @Test
    void divergingDiverging() {
        // Given
        createSwitch(false);

        // When ... Then ...
        assertSame(route, route.diverging());
    }

    @Test
    void divergingThrough() {
        // Given
        createSwitch(true);

        // When ... Then ...
        assertTrue(route.through().isThrough());
    }

    @Test
    void getCrossingEdgesDiverging() {
        // Given
        createSwitch(false);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge fg = station.getEdge("fg");
        Edge gh = station.getEdge("gh");
        Edge bg = station.getEdge("bg");
        Edge fc = station.getEdge("fc");

        // When ... Then ...
        assertThat(route.getCrossingEdges(new Direction(ab, b)), containsInAnyOrder(ef, cd));
        assertThat(route.getCrossingEdges(new Direction(ab, a)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, c)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(cd, d)), empty());
        assertThat(route.getCrossingEdges(new Direction(cd, c)), containsInAnyOrder(ab, gh));
        assertThat(route.getCrossingEdges(new Direction(ef, e)), empty());
        assertThat(route.getCrossingEdges(new Direction(ef, f)), containsInAnyOrder(ab, gh));
        assertThat(route.getCrossingEdges(new Direction(fg, g)), empty());
        assertThat(route.getCrossingEdges(new Direction(fg, f)), empty());
        assertThat(route.getCrossingEdges(new Direction(gh, h)), empty());
        assertThat(route.getCrossingEdges(new Direction(gh, g)), containsInAnyOrder(ef, cd));
        assertThat(route.getCrossingEdges(new Direction(bg, g)), empty());
        assertThat(route.getCrossingEdges(new Direction(bg, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(fc, c)), empty());
        assertThat(route.getCrossingEdges(new Direction(fc, f)), empty());
    }

    @Test
    void getCrossingEdgesThrough() {
        // Given
        createSwitch(true);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge fg = station.getEdge("fg");
        Edge gh = station.getEdge("gh");
        Edge bg = station.getEdge("bg");
        Edge fc = station.getEdge("fc");

        // When ... Then ...
        assertThat(route.getCrossingEdges(new Direction(ab, b)), containsInAnyOrder(ef, gh));
        assertThat(route.getCrossingEdges(new Direction(ab, a)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, c)), empty());
        assertThat(route.getCrossingEdges(new Direction(bc, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(cd, d)), empty());
        assertThat(route.getCrossingEdges(new Direction(cd, c)), containsInAnyOrder(ef, gh));
        assertThat(route.getCrossingEdges(new Direction(ef, e)), empty());
        assertThat(route.getCrossingEdges(new Direction(ef, f)), containsInAnyOrder(ab, cd));
        assertThat(route.getCrossingEdges(new Direction(fg, g)), empty());
        assertThat(route.getCrossingEdges(new Direction(fg, f)), empty());
        assertThat(route.getCrossingEdges(new Direction(gh, h)), empty());
        assertThat(route.getCrossingEdges(new Direction(gh, g)), containsInAnyOrder(ab, cd));
        assertThat(route.getCrossingEdges(new Direction(bg, g)), empty());
        assertThat(route.getCrossingEdges(new Direction(bg, b)), empty());
        assertThat(route.getCrossingEdges(new Direction(fc, c)), empty());
        assertThat(route.getCrossingEdges(new Direction(fc, f)), empty());
    }

    @Test
    void getExitDiverging() {
        createSwitch(false);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge fg = station.getEdge("fg");
        Edge gh = station.getEdge("gh");
        Edge bg = station.getEdge("bg");
        Edge fc = station.getEdge("fc");

        // When ...
        Optional<Direction> dirAB = route.getExit(new Direction(ab, b));
        Optional<Direction> dirCB = route.getExit(new Direction(bc, b));
        Optional<Direction> dirGB = route.getExit(new Direction(bg, b));
        Optional<Direction> dirBC = route.getExit(new Direction(bc, c));
        Optional<Direction> dirDC = route.getExit(new Direction(cd, c));
        Optional<Direction> dirFC = route.getExit(new Direction(fc, c));
        Optional<Direction> dirEF = route.getExit(new Direction(ef, f));
        Optional<Direction> dirCF = route.getExit(new Direction(fc, f));
        Optional<Direction> dirGF = route.getExit(new Direction(fg, f));
        Optional<Direction> dirFG = route.getExit(new Direction(fg, g));
        Optional<Direction> dirBG = route.getExit(new Direction(bg, g));
        Optional<Direction> dirHG = route.getExit(new Direction(gh, g));

        // Then ...
        // a-b-g-h
        // e-f-c-d
        assertFalse(route.isThrough());
        assertThat(dirAB, Matchers.optionalOf(new Direction(bg, g)));
        assertThat(dirCB, emptyOptional());
        assertThat(dirGB, Matchers.optionalOf(new Direction(ab, a)));
        assertThat(dirBC, emptyOptional());
        assertThat(dirDC, Matchers.optionalOf(new Direction(fc, f)));
        assertThat(dirFC, Matchers.optionalOf(new Direction(cd, d)));
        assertThat(dirEF, Matchers.optionalOf(new Direction(fc, c)));
        assertThat(dirCF, Matchers.optionalOf(new Direction(ef, e)));
        assertThat(dirGF, emptyOptional());
        assertThat(dirBG, Matchers.optionalOf(new Direction(gh, h)));
        assertThat(dirFG, emptyOptional());
        assertThat(dirHG, Matchers.optionalOf(new Direction(bg, b)));
    }

    @Test
    void getExitThrow() {
        createSwitch(true);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge fg = station.getEdge("fg");
        Edge gh = station.getEdge("gh");
        Edge bg = station.getEdge("bg");
        Edge fc = station.getEdge("fc");

        // When ...
        Optional<Direction> dirAB = route.getExit(new Direction(ab, b));
        Optional<Direction> dirCB = route.getExit(new Direction(bc, b));
        Optional<Direction> dirGB = route.getExit(new Direction(bg, b));
        Optional<Direction> dirBC = route.getExit(new Direction(bc, c));
        Optional<Direction> dirDC = route.getExit(new Direction(cd, c));
        Optional<Direction> dirFC = route.getExit(new Direction(fc, c));
        Optional<Direction> dirEF = route.getExit(new Direction(ef, f));
        Optional<Direction> dirCF = route.getExit(new Direction(fc, f));
        Optional<Direction> dirGF = route.getExit(new Direction(fg, f));
        Optional<Direction> dirFG = route.getExit(new Direction(fg, g));
        Optional<Direction> dirBG = route.getExit(new Direction(bg, g));
        Optional<Direction> dirHG = route.getExit(new Direction(gh, g));

        // Then ...
        // a-b-c-d
        // e-f-g-h
        assertTrue(route.isThrough());
        assertThat(dirAB, Matchers.optionalOf(new Direction(bc, c)));
        assertThat(dirCB, Matchers.optionalOf(new Direction(ab, a)));
        assertThat(dirGB, emptyOptional());
        assertThat(dirBC, Matchers.optionalOf(new Direction(cd, d)));
        assertThat(dirDC, Matchers.optionalOf(new Direction(bc, b)));
        assertThat(dirFC, emptyOptional());
        assertThat(dirEF, Matchers.optionalOf(new Direction(fg, g)));
        assertThat(dirCF, emptyOptional());
        assertThat(dirGF, Matchers.optionalOf(new Direction(ef, e)));
        assertThat(dirBG, emptyOptional());
        assertThat(dirFG, Matchers.optionalOf(new Direction(gh, h)));
        assertThat(dirHG, Matchers.optionalOf(new Direction(fg, f)));
    }

    @Test
    void getExitsDiverging() {
        // Given ...
        createSwitch(false);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge gh = station.getEdge("gh");
        Edge bg = station.getEdge("bg");
        Edge fc = station.getEdge("fc");

        // When ...
        Collection<Direction> exits = route.getValidExits();

        // Then ...
        // a-b-g-h
        // e-f-c-d
        assertThat(exits, containsInAnyOrder(
                new Direction(ab, a),
                new Direction(bg, b),
                new Direction(bg, g),
                new Direction(gh, h),
                new Direction(ef, e),
                new Direction(fc, f),
                new Direction(fc, c),
                new Direction(cd, d)
        ));
    }

    @Test
    void getExitsThrough() {
        // Given ...
        createSwitch(true);
        Node a = station.getNode("a");
        Node b = station.getNode("b");
        Node c = station.getNode("c");
        Node d = station.getNode("d");
        Node e = station.getNode("e");
        Node f = station.getNode("f");
        Node g = station.getNode("g");
        Node h = station.getNode("h");
        Edge ab = station.getEdge("ab");
        Edge bc = station.getEdge("bc");
        Edge cd = station.getEdge("cd");
        Edge ef = station.getEdge("ef");
        Edge fg = station.getEdge("fg");
        Edge gh = station.getEdge("gh");

        // When ...
        Collection<Direction> exits = route.getValidExits();

        // Then ...
        // a-b-c-d
        // e-f-g-h
        assertThat(exits, containsInAnyOrder(
                new Direction(ab, a),
                new Direction(bc, b),
                new Direction(bc, c),
                new Direction(cd, d),
                new Direction(ef, e),
                new Direction(fg, g),
                new Direction(fg, f),
                new Direction(gh, h)
        ));
    }

    @Test
    void throughDiverging() {
        createSwitch(true);
        assertFalse(route.diverging().isThrough());
    }

    @Test
    void throughThrough() {
        createSwitch(true);
        assertSame(route, route.through());
    }
}