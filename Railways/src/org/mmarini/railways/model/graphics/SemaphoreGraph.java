package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.Semaphore;

/**
 * @author $$Author: marco $$
 * @version $Id: SemaphoreGraph.java,v 1.8 2012/02/08 22:03:30 marco Exp $
 */
public class SemaphoreGraph extends ActiveNodeGraph {
	/**
	 * @param node
	 */
	public SemaphoreGraph(Semaphore node) {
		super(node);
		createTransformedBounds(SemaphorePainter.getInstance().getBounds());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedElement(java.awt.geom.Point2D)
	 */
	@Override
	public StationGraphEvent findSelectedElement(Point2D point) {
		point = getInvTransform().transform(point, null);
		int index = SemaphorePainter.getInstance().findIndexOver(point);
		if (index >= 0) {
			StationGraphEvent ev = getEvent();
			ev.setElement(getNode());
			ev.setIndex(index);
			return ev;
		}
		return null;
	}
}