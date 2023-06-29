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

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.geometry.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.function.Consumer;

import static java.lang.Math.min;
import static org.mmarini.railways2.swing.GraphConstants.NONE_PAINTER;
import static org.mmarini.railways2.swing.Painters.TRACK_STROKE;
import static org.mmarini.railways2.swing.Painters.createLinePainter;

/**
 * Displays the station map
 */
public class MapPanel extends JComponent {
    public static final Color BACKGROUND_COLOR = Color.getHSBColor(0.278f, 0.10f,
            0.671f);
    private static final Logger logger = LoggerFactory.getLogger(MapPanel.class);
    private static final int BORDER = 10;
    private static final double MIN_WIDTH = 100;
    private static final double MIN_HEIGHT = 100;
    private final Flowable<Point2D> mouseClick;
    private Rectangle2D mapBounds;
    private Consumer<Graphics2D> painter;


    /**
     * Creates the map panel
     */
    public MapPanel() {
        painter = NONE_PAINTER;
        setBackground(BACKGROUND_COLOR);
        mapBounds = new Rectangle2D.Double(0, 0, MIN_WIDTH, MIN_HEIGHT);
        mouseClick = SwingObservable.mouse(this, SwingObservable.MOUSE_CLICK)
                .toFlowable(BackpressureStrategy.LATEST)
                .filter(ev -> ev.getID() == MouseEvent.MOUSE_CLICKED)
                .map(this::mapMouseEvent);
        logger.atDebug().log("Created");
    }

    /**
     * Returns the transformation from map coordinates to graphics coordinates
     */
    private AffineTransform createTransform() {
        Dimension size = getSize();
        Rectangle2D.Float panelRect = new Rectangle2D.Float(
                BORDER,
                BORDER,
                size.width - BORDER - BORDER,
                size.height - BORDER - BORDER);
        AffineTransform tr = AffineTransform.getTranslateInstance(panelRect.getCenterX(), panelRect.getCenterY());
        double sx = panelRect.getWidth() / mapBounds.getWidth();
        double sy = panelRect.getHeight() / mapBounds.getHeight();
        double scale = min(sx, sy);
        tr.scale(scale, -scale);
        tr.translate(-mapBounds.getCenterX(), -mapBounds.getCenterY());
        return tr;
    }

    /**
     * Returns the point of mouse click in map coordinates
     *
     * @param mouseEvent the mouse event
     */
    private Point2D mapMouseEvent(MouseEvent mouseEvent) {
        AffineTransform tr = createTransform();
        try {
            tr.invert();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        Point point = mouseEvent.getPoint();
        return tr.transform(point, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g.create();
        Dimension size = getSize();
        gr.setColor(getBackground());
        gr.fillRect(0, 0, size.width, size.height);
        gr.transform(createTransform());
        painter.accept(gr);
    }

    /**
     * Paints the station status
     * Paints in order the tracks not in section, tracks in section, trains
     *
     * @param status status
     */
    public void paintStation(StationStatus status) {
        mapBounds = status.getBounds();
        if (mapBounds.getHeight() < MIN_HEIGHT) {
            mapBounds = new Rectangle2D.Double(mapBounds.getX(), mapBounds.getCenterY() - MIN_HEIGHT / 2, mapBounds.getWidth(), MIN_HEIGHT);
        }
        if (mapBounds.getWidth() < MIN_WIDTH) {
            mapBounds = new Rectangle2D.Double(mapBounds.getCenterX() - MIN_WIDTH / 2, mapBounds.getY(), MIN_WIDTH, mapBounds.getHeight());
        }
        Collection<? extends Edge> edges = status.getStationMap().getEdges().values();
        Consumer<Graphics2D> redPainters = edges.stream()
                .filter(e -> status.getSection(e).isEmpty())
                .map(edge -> createLinePainter(edge, TRACK_STROKE, false))
                .reduce(Consumer::andThen)
                .orElse(NONE_PAINTER);
        Consumer<Graphics2D> greenPainters = edges.stream()
                .filter(e -> status.getSection(e).isPresent())
                .map(edge -> createLinePainter(edge, TRACK_STROKE, true))
                .reduce(Consumer::andThen)
                .orElse(NONE_PAINTER);
        Consumer<Graphics2D> trainPainters = Painters.createLineTrainPainters(status);

        painter = redPainters
                .andThen(greenPainters)
                .andThen(trainPainters);
        repaint();
    }

    /**
     * Returns the mouse click flowable
     */
    public Flowable<Point2D> readMouseClick() {
        return mouseClick;
    }
}
