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

package org.mmarini.railways2.model.route;

import org.mmarini.railways2.model.geometry.Edge;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Describes a section of edges delimited by safety signals.
 * The section is delimited by semaphores, entry nodes, exit nodes;
 */
public class Section {

    /**
     * Returns the section
     *
     * @param terminal0 one of the terminal of section
     * @param terminal1 other terminal of section
     * @param edges     the edges
     */
    public static Section create(RouteDirection terminal0, RouteDirection terminal1, Collection<Edge> edges) {
        String id = edges.stream().map(Edge::getId).min(String::compareTo).orElseThrow();
        return new Section(id, terminal0, terminal1, edges);
    }

    private final String id;
    private final RouteDirection terminal0;
    private final RouteDirection terminal1;
    private final Collection<Edge> edges;
    private Collection<Section> crossingSections;

    /**
     * Creates a section
     *
     * @param id        the section identifier
     * @param terminal0 one of the terminal of section
     * @param terminal1 other terminal of section
     * @param edges     the list of edges
     */
    protected Section(String id, RouteDirection terminal0, RouteDirection terminal1, Collection<Edge> edges) {
        this.id = requireNonNull(id);
        this.terminal0 = terminal0;
        this.terminal1 = terminal1;
        this.edges = requireNonNull(edges);
        this.crossingSections = List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return id.equals(section.id);
    }

    /**
     * Returns the collection of crossing sections
     */
    public Collection<Section> getCrossingSections() {
        return crossingSections;
    }

    /**
     * Returns the Section with the collection of crossing sections set
     *
     * @param crossingSections the crossing sectins
     */
    public Section setCrossingSections(Collection<Section> crossingSections) {
        this.crossingSections = crossingSections;
        return this;
    }

    /**
     * Returns the edges
     */
    public Collection<Edge> getEdges() {
        return edges;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns one of the terminal
     */
    public Optional<RouteDirection> getTerminal0() {
        return Optional.ofNullable(terminal0);
    }

    /**
     * Returns the other terminal
     */
    public Optional<RouteDirection> getTerminal1() {
        return Optional.ofNullable(terminal1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Section.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("terminal0=" + terminal0)
                .add("terminal1=" + terminal1)
                .add("edges=" + edges)
                .toString();
    }

}
