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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.model.routes.Switch;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mmarini.railways.TestFunctions.section;

class StationStatusSwitchSwitchTest {

    static StationMap stationMap;
    static StationStatus status;

    /**
     * Station map
     * <pre>
     *                           --bd-- Exit(d)
     * Entry(a) --ab-- Switch(b) --bc-- Exit(c)
     * </pre>
     */
    @BeforeAll
    static void createRoutesConfig() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(200, 0), "bc")
                .addNode("d", new Point2D.Double(200, 10), "bd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("bc"), "b", "c")
                .addEdge(Track.builder("bd"), "b", "d")
                .build();
    }

    @Test
    void createSectionsDiverging() {
        createSwitch(false);
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(1));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bd)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
    }

    @Test
    void createSectionsThrough() {
        createSwitch(true);
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(1));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bc)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
    }

    void createSwitch(boolean through) {
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(nodes -> through ? Switch.through(nodes) : Switch.diverging(nodes), "b")
                .addRoute(Exit::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void findSectionDeviated() {
        createSwitch(false);
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(new Direction(ab, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ab, b), new Direction(bd, b), ab, bd));

        section = status.findSection(new Direction(bd, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(bd, b), new Direction(ab, b), ab, bd));

        section = status.findSection(new Direction(bc, b));
        assertFalse(section.isPresent());
    }

    @Test
    void findSectionDirect() {
        createSwitch(true);
        Node a = stationMap.getNode("a");
        Node b = stationMap.getNode("b");
        Node c = stationMap.getNode("c");
        Node d = stationMap.getNode("d");
        Edge ab = stationMap.getEdge("ab");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(new Direction(ab, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(ab, b), new Direction(bc, b), ab, bc));
        assertThat(section.orElseThrow()._2, empty());

        section = status.findSection(new Direction(bc, b));
        assertTrue(section.isPresent());
        assertThat(section.orElseThrow()._1, section(new Direction(bc, b), new Direction(ab, b), ab, bc));
        assertThat(section.orElseThrow()._2, empty());

        assertFalse(status.findSection(new Direction(bd, b)).isPresent());
    }

/*
    @Test
    void findSectionTerminalDeviated() {
        createSwitch(false);
        SingleNodeRoute a = conf.getRoute("a");
        Edge bc = stationMap.getEdge("bc");
        Edge bd = stationMap.getEdge("bd");

        Optional<RouteDirection> dirOpt = conf.findSectionTerminal(new OrientedLocation(bc, false, 0));
        assertFalse(dirOpt.isPresent());

        dirOpt = conf.findSectionTerminal(new OrientedLocation(bd, false, 0));
        assertTrue(dirOpt.isPresent());

        RouteDirection dir = dirOpt.orElseThrow();
        assertThat(dir, routeDirection(a, 0));
    }

 */
}