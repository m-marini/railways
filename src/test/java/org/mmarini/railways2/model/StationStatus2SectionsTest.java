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
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class StationStatus2SectionsTest {

    static StationMap stationMap;
    static StationStatus status;

    /**
     * Station map
     * <pre>
     * Entry(a) --ab--  Exit(b)
     * Entry(c) --cd--  Exit(d)
     * </pre>
     */
    @BeforeAll
    static void createRoutesConfig() {
        stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(100, 0), "ab")
                .addNode("c", new Point2D.Double(200, 0), "cd")
                .addNode("d", new Point2D.Double(300, 0), "cd")
                .addEdge(Track.builder("ab"), "a", "b")
                .addEdge(Track.builder("cd"), "c", "d")
                .build();
        status = new StationStatus.Builder(stationMap)
                .addRoute(Entry::create, "a")
                .addRoute(Exit::create, "b")
                .addRoute(Entry::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void createSections() {
        Edge ab = stationMap.getEdge("ab");
        Edge cd = stationMap.getEdge("cd");

        Collection<Section> sections = status.createSections();

        assertThat(sections, hasSize(2));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("edges", containsInAnyOrder(cd)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("cd")),
                hasProperty("crossingSections", empty()))));
    }

    @Test
    void createTrainByExit() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Exit bRoute = status.getRoute("b");
        Exit dRoute = status.getRoute("d");
        Train t1 = Train.create("t1", 1, aRoute, bRoute)
                .setState(Train.EXITING_STATE)
                .setExitingNode(bRoute);
        Train t2 = Train.create("t2", 1, aRoute, bRoute)
                .setState(Train.EXITING_STATE)
                .setExitingNode(dRoute);
        Train t3 = Train.create("t3", 1, aRoute, bRoute);
        status = status.setTrains(t1, t2, t3);

        // When ...
        Map<Exit, Train> map = status.createTrainByExit();

        // Than ...
        assertNotNull(map);
        assertEquals(2, map.size());
        assertThat(map, hasEntry(bRoute, t1));
        assertThat(map, hasEntry(dRoute, t2));
    }

    @Test
    void createTrainBySection() {
        // Give ...
        Node b = stationMap.getNode("b");
        Node d = stationMap.getNode("d");
        Entry aRoute = status.getRoute("a");
        Exit bRoute = status.getRoute("b");
        Edge ab = stationMap.getEdge("ab");
        Edge cd = stationMap.getEdge("cd");
        Train t1 = Train.create("t1", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(ab, b, 0));
        Train t2 = Train.create("t2", 1, aRoute, bRoute)
                .setLocation(EdgeLocation.create(cd, d, 0));
        status = status.setTrains(t1, t2);

        // When ...
        Map<Section, Train> map = status.createTrainBySection();

        // Than ...
        assertNotNull(map);
        assertEquals(2, map.size());
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("ab")),
                equalTo(t1)
        ));
        assertThat(map, hasEntry(
                hasProperty("id", equalTo("cd")),
                equalTo(t2)
        ));
    }

    @Test
    void isExitClear() {
        // Give ...
        Entry aRoute = status.getRoute("a");
        Exit bRoute = status.getRoute("b");
        Exit dRoute = status.getRoute("d");
        Train t1 = Train.create("t1", 1, aRoute, bRoute)
                .setState(Train.EXITING_STATE)
                .setExitingNode(bRoute);
        Train t2 = Train.create("t2", 1, aRoute, bRoute);
        status = status.setTrains(t1, t2);

        // When ... Then ...
        assertFalse(status.isExitClear(bRoute));
        assertTrue(status.isExitClear(dRoute));
    }
}