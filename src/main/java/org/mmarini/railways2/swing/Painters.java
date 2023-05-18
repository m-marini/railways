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

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.*;
import org.mmarini.railways2.model.routes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static org.mmarini.railways2.model.RailwayConstants.COACH_LENGTH;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAUGE;
import static org.mmarini.railways2.model.geometry.MathUtils.RAD180;
import static org.mmarini.railways2.model.geometry.MathUtils.RAD_90;
import static org.mmarini.railways2.swing.GraphConstants.*;

/**
 * Displays the station
 */
public interface Painters {
    int COACH_X = -15;
    int COACH_Y = -125;
    int SIGNAL_X = -25;
    int SIGNAL_Y = -20;
    double SIGNAL_WIDTH = 0.9;
    double SIGNAL_HEIGHT = 2;
    int SWITCH_X = -25;
    int SWITCH_Y = -20;
    double SWITCH_WIDTH = 0.9;
    double SWITCH_HEIGHT = 1.8;

    BasicStroke STROKE0 = new BasicStroke(0f);
    BasicStroke LIGHTS_STROKE = new BasicStroke(0,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[]{
            0.1f, 0.7f}, 0f);
    BasicStroke TRACK_STROKE = new BasicStroke((float) TRACK_GAUGE);
    BasicStroke TRAIN_STROKE = new BasicStroke((float) COACH_WIDTH);

    Consumer<Graphics2D> HEAD_IMAGE_PAINTER = createImagePainter("/img/head.png", COACH_X, COACH_Y, COACH_WIDTH, COACH_LENGTH, RAD_90);
    Consumer<Graphics2D> COACH_IMAGE_PAINTER = createImagePainter("/img/coach.png", COACH_X, COACH_Y, COACH_WIDTH, COACH_LENGTH, RAD_90);
    Consumer<Graphics2D> TAIL_IMAGE_PAINTER = createImagePainter("/img/tail.png", COACH_X, COACH_Y, COACH_WIDTH, COACH_LENGTH, RAD_90);
    Consumer<Graphics2D> CLEAR_SIGNAL_IMAGE_PAINTER = createImagePainter("/img/sig0.png", SIGNAL_X, SIGNAL_Y, SIGNAL_WIDTH, SIGNAL_HEIGHT, RAD_90);
    Consumer<Graphics2D> LOCKED_SIGNAL_IMAGE_PAINTER = createImagePainter("/img/sig1.png", SIGNAL_X, SIGNAL_Y, SIGNAL_WIDTH, SIGNAL_HEIGHT, RAD_90);
    Consumer<Graphics2D> NOT_CLEAR_SIGNAL_IMAGE_PAINTER = createImagePainter("/img/sig2.png", SIGNAL_X, SIGNAL_Y, SIGNAL_WIDTH, SIGNAL_HEIGHT, RAD_90);
    Consumer<Graphics2D> CLEAR_LINE_IMAGE_PAINTER = createImagePainter("/img/line0.png", SIGNAL_X, SIGNAL_Y, SIGNAL_WIDTH, SIGNAL_HEIGHT, RAD_90);
    Consumer<Graphics2D> NOT_CLEAR_LINE_IMAGE_PAINTER = createImagePainter("/img/line1.png", SIGNAL_X, SIGNAL_Y, SIGNAL_WIDTH, SIGNAL_HEIGHT, RAD_90);
    Consumer<Graphics2D> CLEAR_THROUGH_IMAGE_PAINTER = createImagePainter("/img/sw0.png", SIGNAL_X, 0, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Consumer<Graphics2D> NOT_CLEAR_THROUGH_IMAGE_PAINTER = createImagePainter("/img/sw1.png", SWITCH_X, SWITCH_Y, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Consumer<Graphics2D> LOCKED_THROUGH_IMAGE_PAINTER = createImagePainter("/img/sw2.png", SWITCH_X, SWITCH_Y, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Consumer<Graphics2D> CLEAR_DIVERGE_IMAGE_PAINTER = createImagePainter("/img/sw3.png", SWITCH_X, SWITCH_Y, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Consumer<Graphics2D> NOT_CLEAR_DIVERGE_IMAGE_PAINTER = createImagePainter("/img/sw4.png", SWITCH_X, SWITCH_Y, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Consumer<Graphics2D> LOCKED_DIVERGE_IMAGE_PAINTER = createImagePainter("/img/sw5.png", SWITCH_X, SWITCH_Y, SWITCH_WIDTH, SWITCH_HEIGHT, RAD_90);
    Font LABEL_FONT = Font.decode("dialog bold");
    double LABEL_SCALE = 0.1;
    double LABEL_GAP = TRACK_GAUGE / 2 / LABEL_SCALE;

    /**
     * Returns the arc of a curve
     *
     * @param center the center
     * @param radius the radius
     * @param start  the start angle (RAD)
     * @param extent the extent angle (RAD)
     */
    static Arc2D createArc(Point2D center, double radius, double start, double extent) {
        double x = center.getX() - radius;
        double y = center.getY() - radius;
        double diameter = radius * 2;
        return new Arc2D.Double(x, y, diameter, diameter, toDegrees(start), toDegrees(extent), Arc2D.OPEN);
    }

    /**
     * Returns the coach painter
     *
     * @param coachPainter the coach image painter
     * @param coach        the coach location and orientation
     */
    static Consumer<Graphics2D> createCoachPainter(Consumer<Graphics2D> coachPainter, Tuple2<Point2D, Double> coach) {
        return transformAndPaint(coach._1, coach._2, coachPainter);
    }

    /**
     * Returns the painter of curve segment
     *
     * @param segment the segment
     * @param stroke  the stroke
     * @param color   the color
     */
    static Consumer<Graphics2D> createCurvePainter(EdgeSegment segment, Stroke stroke, Color color) {
        Curve curve = segment.getEdge();
        double start = -curve.getAngle(segment.getLocation0());
        double end = -curve.getAngle(segment.getLocation1());
        Arc2D shape = createArc(curve.getCenter(), curve.getRadius(), start, end - start);
        return createShapePainter(shape, color, stroke);
    }

    /**
     * Returns the image painter
     *
     * @param name   the resource name of image
     * @param x      the x translation (pixels)
     * @param y      the y translation (pixels)
     * @param width  the width of image in real world (m)
     * @param height the height of image in real world (m)
     * @param theta  the ccw rotation angle (RAD)
     */
    static Consumer<Graphics2D> createImagePainter(String name, double x, double y, double width, double height, double theta) {
        URL url = Painters.class.getResource(name);
        if (url != null) {
            Image image = new ImageIcon(url).getImage();
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            AffineTransform transform = AffineTransform.getRotateInstance(-theta);
            transform.scale(-width / imageWidth,
                    height / imageHeight);
            transform.translate(x, y);
            return gr -> gr.drawImage(image, transform, null);
        } else {
            return NONE_PAINTER;
        }
    }

    /**
     * Returns the label painter
     *
     * @param route the route
     */
    static Consumer<Graphics2D> createLabelPainter(Route route) {
        String label = route.getId();
        Point2D location = route.getNodes().get(0).getLocation();
        Direction entryDir = route.getNodes().get(0).getEntries().get(0);
        double orientation = toDegrees(new EdgeLocation(entryDir, 0).getOrientation());
        boolean vertical = abs(orientation) <= 135 && abs(orientation) >= 45;
        FontRenderContext ctx = new FontRenderContext(new AffineTransform(),
                false, false);
        Rectangle2D bounds = LABEL_FONT.getStringBounds(label, ctx);
        double cx = bounds.getCenterX();
        double cy = bounds.getCenterY();
        AffineTransform tr = AffineTransform.getTranslateInstance(location.getX(), location.getY());
        tr.scale(LABEL_SCALE, -LABEL_SCALE);
        tr.translate(vertical ? LABEL_GAP : -cx, cy - (vertical ? 0 : LABEL_GAP));
        return transformAndPaint(tr, gr -> {
            gr.setFont(LABEL_FONT);
            gr.setColor(Color.BLACK);
            gr.drawString(label, 0, 0);
        });
    }

    /**
     * Returns the labels painter
     *
     * @param status the status
     */
    static Consumer<Graphics2D> createLabelsPainter(StationStatus status) {
        return status.getRoutes().stream()
                .filter(r -> r instanceof Signal || r instanceof Exit || r instanceof Entry)
                .map(Painters::createLabelPainter)
                .reduce(Consumer::andThen)
                .orElseThrow();
    }

    /**
     * Returns the line painter for a given track
     *
     * @param track  the edge
     * @param stroke the stroke of line
     * @param color  the lights color
     */
    static Consumer<Graphics2D> createLinePainter(Track track, Stroke stroke, Color color) {
        Point2D p0 = track.getNode0().getLocation();
        Point2D p1 = track.getNode1().getLocation();
        Shape shape = new Line2D.Double(p0, p1);
        return gr -> {
            gr.setColor(color);
            gr.setStroke(stroke);
            gr.draw(shape);
        };
    }

    /**
     * Returns the line painter for a give curve
     *
     * @param curve  the edge
     * @param stroke the stroke of line
     * @param color  the lights color
     */
    static Consumer<Graphics2D> createLinePainter(Curve curve, Stroke stroke, Color color) {
        double radius = curve.getRadius();
        Point2D center = curve.getCenter();
        double start = -curve.getAngle0();
        double extent = -curve.getAngle();
        Arc2D arc = createArc(center, radius, start, extent);
        return gr -> {
            gr.setColor(color);
            gr.setStroke(stroke);
            gr.draw(arc);
        };
    }

    /**
     * Returns the painter of edge
     *
     * @param edge   the edge
     * @param stroke the stroke
     * @param color  the color
     */
    static Consumer<Graphics2D> createLinePainter(Edge edge, Stroke stroke, Color color) {
        if (edge instanceof Curve) {
            return createLinePainter((Curve) edge, stroke, color);
        } else if (edge instanceof Platform) {
            return createLinePainter((Platform) edge, stroke, color);
        } else {
            return createLinePainter((Track) edge, stroke, color);
        }
    }

    /**
     * Returns the train painter
     *
     * @param status the station status
     */
    static Consumer<Graphics2D> createLineTrainPainters(StationStatus status) {
        return status.getTrains()
                .stream().flatMap(status::getTrainSegments)
                .map(Painters::createSegmentPainter)
                .reduce(Consumer::andThen)
                .orElse(NONE_PAINTER);
    }

    /**
     * Returns the  edge painter
     *
     * @param route the route
     */
    static Consumer<Graphics2D> createPainter(Edge route) {
        return route instanceof Track ?
                createPainter((Track) route) :
                createPainter((Curve) route);
    }

    /**
     * Returns the curve painter
     *
     * @param curve the curve
     */
    static Consumer<Graphics2D> createPainter(Curve curve) {
        double radius = curve.getRadius();
        Point2D center = curve.getCenter();
        double start = -curve.getAngle0();
        double extent = -curve.getAngle();
        Arc2D innerArc = createArc(center, radius - TRACK_GAUGE / 2, start, extent);
        Arc2D outerArc = createArc(center, radius + TRACK_GAUGE / 2, start, extent);
        return gr -> {
            gr.setColor(TRACK_BLACK_COLOR);
            gr.setStroke(STROKE0);
            gr.draw(innerArc);
            gr.draw(outerArc);
        };
    }

    /**
     * Returns the track painter
     *
     * @param track the track
     */
    static Consumer<Graphics2D> createPainter(Track track) {
        Point2D p0 = track.getNode0().getLocation();
        Point2D p1 = track.getNode1().getLocation();
        double orientation = atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX());
        AffineTransform tr = AffineTransform.getTranslateInstance(p0.getX(), p0.getY());
        tr.rotate(orientation);
        Path2D path = new Path2D.Double();
        path.moveTo(0, -TRACK_GAUGE / 2);
        path.lineTo(track.getLength(), -TRACK_GAUGE / 2);
        path.moveTo(0, TRACK_GAUGE / 2);
        path.lineTo(track.getLength(), TRACK_GAUGE / 2);
        Shape shape = path.createTransformedShape(tr);
        return gr -> {
            gr.setColor(TRACK_BLACK_COLOR);
            gr.setStroke(STROKE0);
            gr.draw(shape);
        };
    }

    /**
     * Returns the exit route painter
     *
     * @param exit   the exit
     * @param status the station status
     */
    static Consumer<Graphics2D> createPainter(Exit exit, StationStatus status) {
        Node node = exit.getNodes().get(0);
        Point2D location = node.getLocation();
        Direction entryDirection = node.getEntries().get(0);
        double orientation = new EdgeLocation(entryDirection, 0).getOrientation();
        boolean isExitClear = status.isExitClear(exit);
        Consumer<Graphics2D> painter0 = isExitClear ? CLEAR_LINE_IMAGE_PAINTER : NOT_CLEAR_LINE_IMAGE_PAINTER;
        return transformAndPaint(location, orientation, painter0);
    }

    /**
     * Returns the signal route painter
     *
     * @param signal the signal
     * @param status the station status
     */
    static Consumer<Graphics2D> createPainter(Signal signal, StationStatus status) {
        Node node = signal.getNodes().get(0);
        Point2D location = node.getLocation();
        return node.getEntries().stream()
                .map(entry -> {
                    Direction exit = signal.getExit(entry).orElseThrow();
                    double orientation = new EdgeLocation(entry, 0).getOrientation();
                    boolean locked = signal.isLocked(entry);
                    boolean clear = status.isSectionClear(exit.getEdge());
                    return transformAndPaint(location, orientation,
                            locked ? LOCKED_SIGNAL_IMAGE_PAINTER :
                                    clear ? CLEAR_SIGNAL_IMAGE_PAINTER :
                                            NOT_CLEAR_SIGNAL_IMAGE_PAINTER);
                }).reduce(
                        Consumer::andThen
                ).orElse(NONE_PAINTER);
    }

    /**
     * Returns the double slip switch route painter
     *
     * @param switchRoute the double slip switch
     * @param status      the station status
     */
    static Consumer<Graphics2D> createPainter(DoubleSlipSwitch switchRoute, StationStatus status) {
        return switchRoute.getNodes().stream()
                .limit(2)
                .map(node -> {
                    Point2D location = node.getLocation();
                    Direction entry = node.getEntries().get(0);
                    double orientation = new EdgeLocation(entry, 0).getOrientation();
                    boolean through = switchRoute.isThrough();
                    Edge edge = entry.getEdge();
                    boolean locked = status.isSectionLocked(edge);
                    boolean clear = status.isSectionClear(edge);
                    return transformAndPaint(location, orientation,
                            through ? (clear ?
                                    (locked ? LOCKED_THROUGH_IMAGE_PAINTER : CLEAR_THROUGH_IMAGE_PAINTER) :
                                    NOT_CLEAR_THROUGH_IMAGE_PAINTER) :
                                    (clear ?
                                            (locked ? LOCKED_DIVERGE_IMAGE_PAINTER : CLEAR_DIVERGE_IMAGE_PAINTER) :
                                            NOT_CLEAR_DIVERGE_IMAGE_PAINTER)
                    );
                }).reduce(Consumer::andThen)
                .orElseThrow();
    }

    /**
     * Returns the switch route painter
     *
     * @param switchRoute the switch
     * @param status      the station status
     */
    static Consumer<Graphics2D> createPainter(Switch switchRoute, StationStatus status) {
        Node node = switchRoute.getNodes().get(0);
        Point2D location = node.getLocation();
        Direction entry = node.getEntries().get(0);
        double orientation = new EdgeLocation(entry, 0).getOrientation();
        boolean through = switchRoute.isThrough();
        Edge edge = entry.getEdge();
        boolean locked = status.isSectionLocked(edge);
        boolean clear = status.isSectionClear(edge);
        return transformAndPaint(location, orientation,
                through ? (clear ?
                        (locked ? LOCKED_THROUGH_IMAGE_PAINTER : CLEAR_THROUGH_IMAGE_PAINTER) :
                        NOT_CLEAR_THROUGH_IMAGE_PAINTER) :
                        (clear ?
                                (locked ? LOCKED_DIVERGE_IMAGE_PAINTER : CLEAR_DIVERGE_IMAGE_PAINTER) :
                                NOT_CLEAR_DIVERGE_IMAGE_PAINTER)
        );
    }

    /**
     * Returns the entry route painter
     *
     * @param entry  the entry
     * @param status the station status
     */
    static Consumer<Graphics2D> createPainter(Entry entry, StationStatus status) {
        Node node = entry.getNodes().get(0);
        Point2D location = node.getLocation();
        Direction entryDir = node.getEntries().get(0);
        double orientation = new EdgeLocation(entryDir, 0).getOrientation();
        Consumer<Graphics2D> painter = status.isSectionClear(entryDir.getEdge()) ?
                CLEAR_LINE_IMAGE_PAINTER :
                NOT_CLEAR_LINE_IMAGE_PAINTER;
        return transformAndPaint(location, orientation, NOT_CLEAR_LINE_IMAGE_PAINTER)
                .andThen(transformAndPaint(location, orientation + RAD180, painter));
    }

    /**
     * Returns route painter
     *
     * @param route  the route
     * @param status the station status
     */
    static Optional<Consumer<Graphics2D>> createPainter(Route route, StationStatus status) {
        if (route instanceof Entry) {
            return Optional.of(createPainter((Entry) route, status));
        } else if (route instanceof Exit) {
            return Optional.of(createPainter((Exit) route, status));
        } else if (route instanceof Signal) {
            return Optional.of(createPainter((Signal) route, status));
        } else if (route instanceof Switch) {
            return Optional.of(createPainter((Switch) route, status));
        } else if (route instanceof DoubleSlipSwitch) {
            return Optional.of(createPainter((DoubleSlipSwitch) route, status));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the painter of station status
     *
     * @param status the station status
     */
    static Consumer<Graphics2D> createPainters(StationStatus status) {
        // Generates the stream of train painters
        Consumer<Graphics2D> trainsPainter = status.getTrainsCoaches()
                .flatMap(Painters::createTrainPainter)
                .reduce(Consumer::andThen)
                .orElse(NONE_PAINTER);
        // Generates the stream of tracks painters
        Consumer<Graphics2D> edgesPainter = status.getStationMap().getEdges().values().stream()
                .map(Painters::createPainter)
                .reduce(Consumer::andThen)
                .orElseThrow();
        // Generates the stream of red lights painters (not clear)
        Consumer<Graphics2D> redEdgesPainter = status.getStationMap().getEdges().values().stream()
                .filter(edge -> status.getSection(edge).isEmpty())
                .map(edge -> createLinePainter(edge, LIGHTS_STROKE, TRACK_RED_COLOR))
                .reduce(Consumer::andThen)
                .orElseThrow();
        // Generates the stream of green lights painters (clear)
        Consumer<Graphics2D> greenEdgesPainter = status.getStationMap().getEdges().values().stream()
                .filter(edge -> status.getSection(edge).isPresent())
                .map(edge -> createLinePainter(edge, LIGHTS_STROKE, TRACK_GREEN_COLOR))
                .reduce(Consumer::andThen)
                .orElseThrow();

        // Generate the stream of routes painters
        Consumer<Graphics2D> routesPainter = status.getRoutes().stream()
                .flatMap(route -> createPainter(route, status).stream())
                .reduce(Consumer::andThen)
                .orElseThrow();

        // Generate the stream of labels painters
        Consumer<Graphics2D> labelsPainter = createLabelsPainter(status);

        // Concatenates the streams
        return edgesPainter
                .andThen(redEdgesPainter)
                .andThen(greenEdgesPainter)
                .andThen(routesPainter)
                .andThen(trainsPainter)
                .andThen(labelsPainter);
    }

    /**
     * Returns the segment painter
     *
     * @param segment the segment
     */
    static Consumer<Graphics2D> createSegmentPainter(EdgeSegment segment) {
        Edge edge = segment.getEdge();
        if (edge instanceof Curve) {
            return createCurvePainter(segment, TRAIN_STROKE, GraphConstants.TRAIN_COLOR);
        } else if (edge instanceof Track) {
            return createTrackSegmentPainter(segment, TRAIN_STROKE, GraphConstants.TRAIN_COLOR);
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
    static Consumer<Graphics2D> createShapePainter(Shape shape, Color color, Stroke stroke) {
        return gr -> {
            gr.setColor(color);
            gr.setStroke(stroke);
            gr.draw(shape);
        };
    }

    /**
     * Returns the painter of track segment
     *
     * @param segment the segment
     * @param stroke  the stroke of line
     * @param color   the color
     */
    static Consumer<Graphics2D> createTrackSegmentPainter(EdgeSegment segment, Stroke stroke, Color color) {
        Line2D shape = new Line2D.Float(segment.getLocation0().getLocation(), segment.getLocation1().getLocation());
        return createShapePainter(shape, color, stroke);
    }

    /**
     * Returns the train painters
     *
     * @param coaches the train composition
     */
    static Stream<Consumer<Graphics2D>> createTrainPainter(TrainComposition coaches) {
        Stream<Consumer<Graphics2D>> head = coaches.getHead().map(coach -> createCoachPainter(HEAD_IMAGE_PAINTER, coach)).stream();
        Stream<Consumer<Graphics2D>> coachPainters = coaches.getCoaches().stream().map(coach -> createCoachPainter(COACH_IMAGE_PAINTER, coach));
        Stream<Consumer<Graphics2D>> tail = coaches.getTail().map(coach -> createCoachPainter(TAIL_IMAGE_PAINTER, coach)).stream();
        return Stream.concat(head, Stream.concat(coachPainters, tail));
    }

    /**
     * Returns the painter that transforms and paints
     * The painter restores the original graphics transformation
     *
     * @param tr      the transformation
     * @param painter the painter
     */
    static Consumer<Graphics2D> transformAndPaint(AffineTransform tr, Consumer<Graphics2D> painter) {
        return gr -> {
            AffineTransform back = gr.getTransform();
            gr.transform(tr);
            painter.accept(gr);
            gr.setTransform(back);
        };
    }

    /**
     * Returns the painter that transforms and paints
     * The painter restores the original graphics transformation
     *
     * @param translate the translation point
     * @param rotate    the rotation point (RAD)
     * @param painter   the painter
     */
    static Consumer<Graphics2D> transformAndPaint(Point2D translate, double rotate, Consumer<Graphics2D> painter) {
        AffineTransform tr = AffineTransform.getTranslateInstance(translate.getX(), translate.getY());
        tr.rotate(rotate);
        return transformAndPaint(tr, painter);
    }
}
