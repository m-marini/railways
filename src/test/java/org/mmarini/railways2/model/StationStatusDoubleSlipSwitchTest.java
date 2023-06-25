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

import org.junit.jupiter.api.Test;
import org.mmarini.railways2.model.geometry.Edge;
import org.mmarini.railways2.model.geometry.StationBuilder;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.railways2.model.routes.DoubleSlipSwitch;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class StationStatusDoubleSlipSwitchTest extends WithStationStatusTest {

    public static final double GAME_DURATION = 300d;
    private static final double LENGTH = 100;
    private Subscriber<SoundEvent> events;

    @Test
    void createSectionsDiverging() {
        // Given ...
        createStatus(false);
        Collection<Section> sections = status.createSections();

        // Then ...
        Edge ab = edge("ab");
        Edge cd = edge("cd");
        Edge ef = edge("ef");
        Edge gh = edge("gh");
        Edge bg = edge("bg");
        Edge fc = edge("fc");
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
        createStatus(true);
        Collection<Section> sections = status.createSections();

        // Then ...
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        Edge cd = edge("cd");
        Edge ef = edge("ef");
        Edge fg = edge("fg");
        Edge gh = edge("gh");
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

    /**
     * StationDef map
     * <pre>
     * Entry(a) --ab-- b --bc-- c --cd-- Exit(d)
     *                   --bg-- g
     *                   --fc-- c
     * Entry(e) --ef-- f --fg-- g --gh-- Exit(h)
     * </pre>
     */
    void createStatus(boolean through) {
        this.events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
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
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Entry::create, "e")
                .addRoute(DoubleSlipSwitch.create(through), "b", "c", "f", "g")
                .addRoute(Exit::create, "d")
                .addRoute(Exit::create, "h")
                .build();
    }

    @Test
    void isConsistentDiverging() {
        // Given ...
        createStatus(false);
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", LENGTH)
                .addTrain(3, "a", "d", "ef", "f", LENGTH)
                .build();

        // When ... Then ...
        assertFalse(status.isConsistent());
    }

    @Test
    void isConsistentThrough() {
        // Given ...
        createStatus(true);
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", LENGTH)
                .addTrain(3, "a", "d", "ef", "f", LENGTH)
                .build();

        // When ... Then ...
        assertFalse(status.isConsistent());
    }

    @Test
    void isSectionWithTrain() {
        // Given ...
        createStatus(true);
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", LENGTH)
                .build();

        // When ... Then ...
        assertTrue(status.isSectionWithTrain(edge("cd")));
        assertFalse(status.isSectionWithTrain(edge("gh")));
        assertFalse(status.isSectionWithTrain(edge("bg")));
    }

    @Test
    void toggleToDiverging() {
        // Given ...
        createStatus(true);

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b");

        // Then ...
        assertFalse(status1.<DoubleSlipSwitch>getRoute("b").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleToThrough() {
        // Given ...
        createStatus(false);

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b");

        // Then ...
        assertTrue(status1.<DoubleSlipSwitch>getRoute("b").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleUnclear() {
        // Given ...
        createStatus(false);
        status = withTrain()
                .addTrain(3, "a", "d", "ab", "b", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.toggleDoubleSlipSwitch("b");

        // Then ...
        assertSame(status, status1);
    }
}