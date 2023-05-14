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

package org.mmarini.railways2.swing;

import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.toDegrees;
import static org.mmarini.railways2.swing.GraphConstants.*;

/**
 * Displays the station map
 */
public class MapPanel extends JComponent {
    public static final Color BACKGROUND_COLOR = Color.getHSBColor(0.278f, 0.10f,
            0.671f);
    private static final BasicStroke TRACK_STROCK = new BasicStroke((float) TRACK_GAUGE);
    private static final BasicStroke TRAIN_STROKE = new BasicStroke((float) COACH_WIDTH);
    private static final int BORDER = 10;
    private static final Consumer<Graphics2D> NONE_PAINTER = unused -> {
    };

    private static Consumer<Graphics2D> createCurvePainter(EdgeSegment segment, Stroke stroke, Color color) {
        Curve curve = segment.getEdge();
        double radius = curve.getRadius();
        Point2D center = curve.getCenter();
        double x = center.getX() - radius;
        double y = center.getY() - radius;
        float diameter = (float) (radius * 2);
        double start = toDegrees(-curve.getAngle(segment.getLocation0()));
        double end = toDegrees(-curve.getAngle(segment.getLocation1()));
        double extent = end - start;
        Arc2D shape = new Arc2D.Double(x, y, diameter, diameter, start, extent, Arc2D.OPEN);
        return createShapePainter(shape, color, stroke);
    }

    /**
     * Returns the painter of edge
     *
     * @param edge  the edge
     * @param green the color
     */
    private static Consumer<Graphics2D> createPainter(Edge edge, boolean green) {
        if (edge instanceof Platform) {
            return createTrackPainter(
                    EdgeSegment.createFullSegment(edge),
                    green ? PLATFORM_GREEN_COLOR : PLATFORM_RED_COLOR);
        } else if (edge instanceof Track) {
            return createTrackPainter(
                    EdgeSegment.createFullSegment(edge),
                    green ? TRACK_GREEN_COLOR : TRACK_RED_COLOR);
        } else if (edge instanceof Curve) {
            return createCurvePainter(
                    EdgeSegment.createFullSegment(edge),
                    TRACK_STROCK,
                    green ? TRACK_GREEN_COLOR : TRACK_RED_COLOR);
        } else {
            return NONE_PAINTER;
        }
    }

    /**
     * Returns the segment painter
     *
     * @param segment the segment
     */
    private static Consumer<Graphics2D> createSegmentPainter(EdgeSegment segment) {
        Edge edge = segment.getEdge();
        if (edge instanceof Curve) {
            return createCurvePainter(segment, MapPanel.TRAIN_STROKE, GraphConstants.TRAIN_COLOR);
        } else if (edge instanceof Track) {
            return createTrackPainter(segment, GraphConstants.TRAIN_COLOR);
        } else {
            return NONE_PAINTER;
        }

    }

    /**
     * Returns the shape painter
     *
     * @param color  the shape color
     * @param shape  the shape
     * @param stroke the stroke
     */
    private static Consumer<Graphics2D> createShapePainter(Shape shape, Color color, Stroke stroke) {
        return gr -> {
            gr.setColor(color);
            gr.setStroke(stroke);
            gr.draw(shape);
        };
    }

    private static Consumer<Graphics2D> createTrackPainter(EdgeSegment segment, Color color) {
        Line2D shape = new Line2D.Float(segment.getLocation0().getLocation(), segment.getLocation1().getLocation());
        return createShapePainter(shape, color, TRACK_STROCK);
    }

    /**
     * Returns the train painter
     *
     * @param status the station status
     */
    private static Stream<Consumer<Graphics2D>> createTrainPainter(StationStatus status) {
        return status.getTrains()
                .stream().flatMap(status::getTrainSegments)
                .map(MapPanel::createSegmentPainter);
    }
    private Rectangle2D mapBounds;
    private List<Consumer<Graphics2D>> painters;

    /**
     * Creates the map panel
     */
    public MapPanel() {
        painters = List.of();
        setBackground(BACKGROUND_COLOR);
        mapBounds = new Rectangle2D.Float(0, 0, 500, 500);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g.create();
        Dimension size = getSize();
        gr.setColor(getBackground());
        gr.fillRect(0, 0, size.width, size.height);
        Rectangle2D.Float panelRect = new Rectangle2D.Float(
                BORDER,
                BORDER,
                size.width - BORDER - BORDER,
                size.height - BORDER - BORDER);
        gr.translate(panelRect.getCenterX(), panelRect.getCenterY());
        gr.scale((panelRect.getWidth()) / mapBounds.getWidth(),
                -panelRect.getHeight() / mapBounds.getHeight());
        gr.translate(-mapBounds.getCenterX(), -mapBounds.getCenterY());
        painters.forEach(c -> c.accept(gr));
    }

    /**
     * Paints the station status
     * Paints in order the tracks not in section, tracks in section, trains
     *
     * @param status status
     */
    public void paintStation(StationStatus status) {
        mapBounds = status.getBounds();
        Collection<? extends Edge> edges = status.getStationMap().getEdges().values();
        Stream<Consumer<Graphics2D>> redPainters = edges.stream()
                .filter(e -> status.getSection(e).isEmpty())
                .map(edge -> createPainter(edge, false));
        Stream<Consumer<Graphics2D>> greenPainters = edges.stream()
                .filter(e -> status.getSection(e).isPresent())
                .map(edge -> createPainter(edge, true));
        Stream<Consumer<Graphics2D>> trainPainters = status.getTrains().stream()
                .flatMap(train -> createTrainPainter(status));
        painters = Stream.concat(
                        Stream.concat(redPainters, greenPainters),
                        trainPainters)
                .collect(Collectors.toList());
        repaint();
    }
}
