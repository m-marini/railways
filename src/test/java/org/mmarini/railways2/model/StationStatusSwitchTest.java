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
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.Entry;
import org.mmarini.railways2.model.routes.Exit;
import org.mmarini.railways2.model.routes.Section;
import org.mmarini.railways2.model.routes.Switch;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mmarini.railways2.Matchers.*;
import static org.mmarini.railways2.model.Matchers.isSectionWith;
import static org.mockito.Mockito.verify;

class StationStatusSwitchTest extends WithStationStatusTest {

    public static final double LENGTH = 100;
    public static final double GAP = 10;
    public static final double GAME_DURATION = 300d;
    private Subscriber<SoundEvent> events;

    @Test
    void createSectionsDiverging() {
        // Given ...
        createSwitch(false);

        // WHen ...
        Collection<Section> sections = status.createSections();

        // Then ...
        Edge ab = edge("ab");
        Edge bd = edge("bd");
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
        // Given ...
        createSwitch(true);

        // When ...
        Collection<Section> sections = status.createSections();

        // Then ...
        Edge ab = edge("ab");
        Edge bc = edge("bc");
        assertThat(sections, hasSize(1));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("edges", containsInAnyOrder(ab, bc)))));
        assertThat(sections, hasItem(allOf(
                hasProperty("id", equalTo("ab")),
                hasProperty("crossingSections", empty()))));
    }

    /**
     * StationDef map
     * <pre>
     *                           --bd-- Exit(d)
     * Entry(a) --ab-- Switch(b) --bc-- Exit(c)
     * </pre>
     */
    void createSwitch(boolean through) {
        this.events = Mockito.mock();
        StationMap stationMap = new StationBuilder("station")
                .addNode("a", new Point2D.Double(), "ab")
                .addNode("b", new Point2D.Double(LENGTH, 0), "ab", "bc", "bd")
                .addNode("c", new Point2D.Double(2 * LENGTH, 0), "bc")
                .addNode("d", new Point2D.Double(2 * LENGTH, GAP), "bd")
                .addTrack("ab", "a", "b")
                .addTrack("bc", "b", "c")
                .addTrack("bd", "b", "d")
                .build();
        status = new StationStatus.Builder(stationMap, 1, GAME_DURATION, null, events)
                .addRoute(Entry::create, "a")
                .addRoute(Switch.create(through), "b")
                .addRoute(Exit::create, "c")
                .addRoute(Exit::create, "d")
                .build();
    }

    @Test
    void findSectionDeviated() {
        // Given ...
        createSwitch(false);

        // When ...
        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(direction("ab", "b"));

        // Then ...
        Node b = node("b");
        Edge bc = edge("bc");
        Edge bd = edge("bd");
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "bd", "b", "ab", "bd"),
                empty()
        )));

        section = status.findSection(new Direction(bd, b));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bd", "b", "ab", "b", "ab", "bd"),
                empty()
        )));

        section = status.findSection(new Direction(bc, b));
        assertThat(section, emptyOptional());
    }

    @Test
    void findSectionDirect() {
        // Given ...
        createSwitch(true);

        // When ...
        Optional<Tuple2<Section, Set<Edge>>> section = status.findSection(direction("ab", "b"));

        // Then ...
        Node b = node("b");
        Edge bc = edge("bc");
        Edge bd = edge("bd");
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("ab", "b", "bc", "b", "ab", "bc"),
                empty()
        )));

        section = status.findSection(new Direction(bc, b));
        assertThat(section, optionalOf(tupleOf(
                isSectionWith("bc", "b", "ab", "b", "ab", "bc"),
                empty()
        )));

        assertThat(status.findSection(new Direction(bd, b)), emptyOptional());
    }

    @Test
    void toggleSwitchToDiverging() {
        // Given ...
        createSwitch(true);

        // When ...
        StationStatus status1 = status.toggleSwitch("b");

        // Then ...
        assertFalse(status1.<Switch>getRoute("b").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleSwitchToThrough() {
        // Given ...
        createSwitch(false);

        // When ...
        StationStatus status1 = status.toggleSwitch("b");

        // Then ...
        assertTrue(status1.<Switch>getRoute("b").isThrough());
        verify(events).onNext(SoundEvent.SWITCH);
    }

    @Test
    void toggleSwitchTrainInSection() {
        // Given ...
        createSwitch(true);
        status = withTrain()
                .addTrain(3, "a", "c", "ab", "b", LENGTH)
                .build();

        // When ...
        StationStatus status1 = status.toggleSwitch("b");

        // Then ...
        assertSame(status, status1);
    }
}