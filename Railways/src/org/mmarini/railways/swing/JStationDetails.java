package org.mmarini.railways.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.graphics.GraphicContants;
import org.mmarini.railways.model.graphics.StationGraph;

/**
 * @author $Author: marco $
 * @version $Id: JStationDetails.java,v 1.7 2012/02/08 22:03:31 marco Exp $
 */
public class JStationDetails extends JScrollPane implements GraphicContants {
	private static final long serialVersionUID = 1L;

	private JElementSelector selector;

	/**
	 * 
	 */
	public JStationDetails() {
		selector = new JElementSelector();
		setViewportView(selector);
		getViewport().setBackground(BACKGROUND_COLOR);
	}

	/**
	 * @return
	 */
	public StationGraph getGraph() {
		return selector.getGraph();
	}

	/**
	 * @param location
	 */
	public void setDetailsLocation(Point2D location) {
		JStation stat = selector;
		location = stat.getPhisicalLocation(location);
		int x = (int) Math.round(location.getX());
		int y = (int) Math.round(location.getY());
		JViewport view = getViewport();
		Dimension size = view.getExtentSize();
		x -= size.width / 2;
		y -= size.height / 2;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		stat.scrollRectToVisible(new Rectangle(x, y, size.width, size.height));
	}

	/**
	 * @param gameHandler
	 * @see org.mmarini.railways.swing.JElementSelector#setGameHandler(org.mmarini.railways.model.GameHandler)
	 */
	public void setGameHandler(GameHandler gameHandler) {
		selector.setGameHandler(gameHandler);
	}

	/**
	 * @param station
	 */
	public void showStation(Station station) {
		selector.showStation(station);
	}
}