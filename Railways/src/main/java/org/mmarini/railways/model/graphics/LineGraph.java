package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Neighbour;

/**
 * @author $$Author: marco $$
 * @version $Id: LineGraph.java,v 1.9 2012/02/08 22:03:31 marco Exp $
 */
public class LineGraph extends ActiveNodeGraph {
	/**
	 * @param node
	 */
	public LineGraph(Line node) {
		super(node);
		createTransformedBounds(LinePainter.getInstance().getBounds());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedElement(java.awt.geom.Point2D)
	 */
	@Override
	public StationGraphEvent findSelectedElement(Point2D point) {
		if (getBounds().contains(point)) {
			point = getInvTransform().transform(point, null);
			int index = LinePainter.getInstance().findIndexOver(point);
			StationGraphEvent ev = getEvent();
			ev.setElement(getNode());
			ev.setIndex(index);
			return ev;
		}
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.AbstractNodeGraph#getLabel()
	 */
	@Override
	public String getLabel() {
		Line line = (Line) getNode();
		Neighbour neighbour = line.getNeighbour();
		return neighbour != null ? neighbour.getName() : line.getReference();
	}
}