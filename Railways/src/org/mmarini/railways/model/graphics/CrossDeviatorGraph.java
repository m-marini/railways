package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.CrossDeviator;

/**
 * @author $$Author: marco $$
 * @version $Id: CrossDeviatorGraph.java,v 1.2.10.1 2012/02/04 19:22:59 marco
 *          Exp $
 */
public class CrossDeviatorGraph extends ActiveNodeGraph {
	/**
	 * @param node
	 */
	public CrossDeviatorGraph(CrossDeviator node) {
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