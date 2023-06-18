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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import static java.lang.Math.round;
import static org.mmarini.railways2.swing.GraphConstants.NONE_PAINTER;
import static org.mmarini.railways2.swing.Painters.Builder;

/**
 * Displays the station
 */
public class StationPanel extends JComponent {
    public static final Color BACKGROUND_COLOR = Color.getHSBColor(95f / 360f,
            0.08f, 0.40f);
    private static final int BORDER = 40;
    /**
     * Scale 10 pix/m
     */
    private static final double SCALE = 10;
    private static final Logger logger = LoggerFactory.getLogger(StationPanel.class);
    private final Flowable<MapEvent> mouseClick;
    private Point2D center;
    private Consumer<Graphics2D> painter;
    private StationStatus status;

    /**
     * Creates the station panel
     */
    public StationPanel() {
        setBackground(BACKGROUND_COLOR);
        this.center = new Point2D.Double();
        this.painter = NONE_PAINTER;
        this.mouseClick = SwingObservable.mouse(this, SwingObservable.MOUSE_CLICK)
                .toFlowable(BackpressureStrategy.BUFFER)
                .filter(ev -> ev.getID() == MouseEvent.MOUSE_PRESSED)
                .map(this::mapMouseEvent);
    }

    /**
     * Returns the transformation from map coordinates to view coordinates
     */
    AffineTransform createTransform() {
        Dimension size = getSize();
        Rectangle2D.Float panelRect = new Rectangle2D.Float(
                BORDER,
                BORDER,
                size.width - BORDER - BORDER,
                size.height - BORDER - BORDER);
        AffineTransform tr = AffineTransform.getTranslateInstance(panelRect.getCenterX(), panelRect.getCenterY());
        tr.scale(SCALE, -SCALE);
        tr.translate(-center.getX(), -center.getY());
        return tr;
    }

    /**
     * Returns the center of map view
     */
    public Point2D getCenter() {
        return center;
    }

    /**
     * Set the center of map view
     *
     * @param center the center
     */
    public void setCenter(Point2D center) {
        this.center = center;
        repaint();
    }

    /**
     * Returns the map point of the view point
     *
     * @param point the map point
     */
    public Point2D getMapPoint(Point point) {
        AffineTransform tr = createTransform();
        try {
            tr.invert();
        } catch (NoninvertibleTransformException e) {
            logger.atError().setCause(e).log();
            throw new RuntimeException(e);
        }
        return tr.transform(point, null);
    }

    /**
     * Returns the view point of the map point
     *
     * @param point the map point
     */
    public Point getViewPoint(Point2D point) {
        AffineTransform tr = createTransform();
        Point2D viewPoint = tr.transform(point, null);
        int x = (int) Math.round(viewPoint.getX());
        int y = (int) Math.round(viewPoint.getY());
        return new Point(x, y);
    }

    /**
     * Returns the map event from the mouse event
     *
     * @param mouseEvent the mouse event
     */
    private MapEvent mapMouseEvent(MouseEvent mouseEvent) {
        Point2D mapLocation = getMapPoint(mouseEvent.getPoint());
        return new MapEvent(mouseEvent,
                mapLocation,
                status);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Dimension size = getSize();
        Graphics2D gr = (Graphics2D) g.create();
        gr.setColor(getBackground());
        gr.fillRect(0, 0, size.width, size.height);
        gr.transform(createTransform());
        painter.accept(gr);
    }

    /**
     * Paints the station.
     * <p>
     * Paints in order:
     * <ul>
     *     <li>the edges background</li>
     *     <li>the edges line</li>
     *     <li>the nodes</li>
     *     <li>the trains evidence</li>
     * </ul>
     *
     * </p>
     *
     * @param status the station
     */
    public void paintStation(StationStatus status) {
        this.status = status;
        Rectangle2D bounds = status.getBounds();
        int w = (int) round(bounds.getWidth() * SCALE);
        int h = (int) round(bounds.getHeight() * SCALE);
        center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        setPreferredSize(new Dimension(w + BORDER * 2, h + BORDER * 2));
        painter = new Builder(status).build();
        repaint();
    }

    public Flowable<MapEvent> readMouseClick() {
        return mouseClick;
    }
}
