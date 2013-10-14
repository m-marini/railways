package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.Deviator;

/**
 * @author $$Author: marco $$
 * @version $Id: DeviatorGraph.java,v 1.9 2012/02/08 22:03:31 marco Exp $
 */
public class DeviatorGraph extends ActiveNodeGraph {
	/**
	 * @param node
	 */
	public DeviatorGraph(Deviator node) {
		super(node);
		createTransformedBounds(DeviatorPainter.getInstance().getBounds());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedElement(java.awt.geom.Point2D)
	 */
	@Override
	public StationGraphEvent findSelectedElement(Point2D point) {
		point = getInvTransform().transform(point, null);
		if (DeviatorPainter.getInstance().getActiveBounds().contains(point)) {
			StationGraphEvent ev = getEvent();
			ev.setElement(getNode());
			return ev;
		}
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.AbstractNodeGraph#paintLabels(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paintLabels(GraphicsContext context) {
	}
}