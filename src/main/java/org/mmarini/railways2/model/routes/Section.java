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

import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.geometry.Edge;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Describes a section of edges delimited by safety signals.
 * The section is delimited by semaphores, entry nodes, exit nodes;
 */
public class Section {

    /**
     * Returns the section
     *
     * @param exit0 the exit of one of terminal route of section (section edge toward inner node)
     * @param exit1 the exit of other terminal route of section (section edge toward inner node)
     * @param edges the edges
     */
    public static Section create(Direction exit0, Direction exit1, List<Edge> edges) {
        String id = edges.stream().map(Edge::getId).min(String::compareTo).orElseThrow();
        return new Section(id, exit0, exit1, edges);
    }

    private final String id;
    private final Direction exit0;
    private final Direction exit1;
    private final List<Edge> edges;
    private Collection<Section> crossingSections;

    /**
     * Creates a section
     *
     * @param id    the section identifier
     * @param exit0 the exit of one of terminal route of section (section edge toward inner node)
     * @param exit1 the exit of other terminal route of section (section edge toward inner node)
     * @param edges the list of edges
     */
    protected Section(String id, Direction exit0, Direction exit1, List<Edge> edges) {
        this.id = requireNonNull(id);
        this.exit0 = requireNonNull(exit0);
        this.exit1 = requireNonNull(exit1);
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
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Returns the exit of one of terminal route of section (section edge toward inner node)
     */
    public Direction getExit0() {
        return exit0;
    }

    /**
     * Returns the exit of other terminal route of section (section edge toward inner node)
     */
    public Direction getExit1() {
        return exit1;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Section.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("terminal0=" + exit0)
                .add("terminal1=" + exit1)
                .add("edges=" + edges)
                .toString();
    }

}
