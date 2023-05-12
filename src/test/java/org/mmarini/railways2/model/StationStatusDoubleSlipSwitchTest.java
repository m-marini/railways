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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;

import java.awt.geom.Point2D;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StationStatusDoubleSlipSwitchTest {

    private static final double LENGTH = 100;
    StationMap stationMap;
    StationStatus status;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab-- b --bc-- c --cd-- Exit(d)
     *                   --bg-- g
     *                   --fc-- c
     * Entry(e) --ef-- f --fg-- g --gh-- Exit(h)
     * </pre>
     */
    @BeforeEach
    void beforeEach() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc", "bg")
                .addNode("c", new Point2D.Double(LENGTH * 2, 0), "cd", "bc", "fc")
                .addNode("d", new Point2D.Double(LENGTH * 3, 0), "cd")
                .addNode("e", new Point2D.Double(0, 50), "ef")
                .addNode("f", new Point2D.Double(LENGTH, 50), "ef", "fg", "fc")
                .addNode("g", new Point2D.Double(LENGTH * 2, 50), "gh", "fg", "bg")
                .addNode("h", new Point2D.Double(LENGTH * 3, 50), "gh")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("cd"), "c", "d")
                .addEdge(Track.builder("ef"), "e", "f")
                .addEdge(Track.builder("fg"), "f", "g")
                .addEdge(Track.builder("gh"), "g", "h")
                .addEdge(Track.builder("bg"), "b", "g")
                .addEdge(Track.builder("fc"), "f", "c")
                .build();
    }

    @Test
    void createSectionsDiverging() {
        // Given ...
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Node e = stationMap.getNode("e");
        Node f = stationMap.getNode("f");
        Node g = stationMap.getNode("g");
        Node h = stationMap.getNode("h");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");
        Edge ef = stationMap.getEdge("ef");
        Edge fg = stationMap.getEdge("fg");
        Edge gh = stationMap.getEdge("gh");
        Edge bg = stationMap.getEdge("bg");
        Edge fc = stationMap.getEdge("fc");
        createStatus(false);
        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bg, gh)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("cd"))
                )))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("edges", containsInAnyOrder(ef, fc, cd)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("ab"))
                )))));
    }

    @Test
    void createSectionsTrough() {
        // Given ...
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge cd = stationMap.getEdge("cd");
        Edge ef = stationMap.getEdge("ef");
        Edge fg = stationMap.getEdge("fg");
        Edge gh = stationMap.getEdge("gh");
        createStatus(true);
        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bc, cd)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("ef"))
                )))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ef")),
                hasProperty("edges", containsInAnyOrder(ef, fg, gh)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ef")),
                hasProperty("crossingSections", contains(
                        hasProperty("id", equalTo("ab"))
                )))));
    }

    void createStatus(boolean through) {
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Entry::create, "e")
                .addRoute(DoubleSlipSwitch.create(through), "b", "c", "f", "g")
                .addRoute(Exit::create, "d")
                .addRoute(Exit::create, "h")
                .build();
    }
}