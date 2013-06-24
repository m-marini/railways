package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.routes.PointRoute;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Point.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class Point extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @param reference
	 */
	public Point(String reference) {
		super(reference, 2);
		PointRoute route0 = new PointRoute(this);
		PointRoute route1 = new PointRoute(this);
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(route0, route1);
		junction[1] = new NodeJunctionImpl(route1, route0);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitPoint(this);
	}
}