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

import io.reactivex.rxjava3.core.Flowable;
import org.mmarini.railways2.model.StationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

import static java.lang.Math.max;

/**
 * Shows the visible viewport of the station and handle the scrolling of view
 */
public class StationScrollPanel extends JScrollPane {
    private static final Logger logger = LoggerFactory.getLogger(StationScrollPanel.class);
    private final StationPanel stationPanel;

    /**
     * Creates the station view panel
     */
    public StationScrollPanel() {
        this.stationPanel = new StationPanel();
        setViewportView(stationPanel);
        logger.atDebug().log("Created");
    }

    /**
     * Paints the station
     *
     * @param stationStatus the station status
     */
    public void paintStation(StationStatus stationStatus) {
        stationPanel.paintStation(stationStatus);
    }

    /**
     * Returns the mouse click flowable events
     */
    public Flowable<MapEvent> readMouseClick() {
        return stationPanel.readMouseClick();
    }

    /**
     * Scrolls the view to the location
     *
     * @param location the location
     */
    public void scrollTo(Point2D location) {
        logger.atDebug().log("scroll to {}", location);
        Point viewPoint = stationPanel.getViewPoint(location);
        JViewport view = getViewport();
        Dimension size = view.getExtentSize();
        int x = max(0, viewPoint.x - size.width / 2);
        int y = max(0, viewPoint.y - size.height / 2);
        stationPanel.scrollRectToVisible(new Rectangle(x, y, size.width, size.height));
    }

    /**
     * Shows the popup menu
     *
     * @param popupMenu the popup menu
     * @param location  the location in map coordinate
     */
    public void showPopUp(JPopupMenu popupMenu, Point2D location) {
        Point loc = stationPanel.getViewPoint(location);
        popupMenu.show(stationPanel, loc.x, loc.y);
    }
}
