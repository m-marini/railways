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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

import static java.lang.Math.*;
import static org.mmarini.railways2.swing.GraphConstants.NONE_PAINTER;
import static org.mmarini.railways2.swing.Painters.*;

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
    private Point2D center;
    private Consumer<Graphics2D> painter;

    /**
     * Creates the station panel
     */
    public StationPanel() {
        setBackground(BACKGROUND_COLOR);
        this.center = new Point2D.Double();
        this.painter = NONE_PAINTER;
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

    @Override
    protected void paintComponent(Graphics g) {
        Dimension size = getSize();
        Graphics2D gr = (Graphics2D) g.create();
        gr.setColor(getBackground());
        gr.fillRect(0, 0, size.width, size.height);
        Rectangle2D.Float panelRect = new Rectangle2D.Float(
                BORDER,
                BORDER,
                size.width - BORDER - BORDER,
                size.height - BORDER - BORDER);

        gr.translate(panelRect.getCenterX(), panelRect.getCenterY());
        gr.scale(SCALE, -SCALE);
        gr.translate(-center.getX(), -center.getY());
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
        Rectangle2D bounds = status.getBounds();
        int w = (int) round(bounds.getWidth() * SCALE);
        int h = (int) round(bounds.getHeight() * SCALE);
        center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        setPreferredSize(new Dimension(w + BORDER * 2, h + BORDER * 2));
        painter = createPainters(status);
        /*
        painter = transformAndPaint(new Point2D.Double(), toRadians(45), CLEAR_LINE_IMAGE_PAINTER).andThen(
                gr -> {
                    gr.setColor(Color.GREEN);
                    gr.fill(new Ellipse2D.Double(-1, -1, 2, 2));
                }
        );

         */
        repaint();
    }
}
