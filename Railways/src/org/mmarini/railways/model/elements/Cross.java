package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.CrossRoute;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Cross.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class Cross extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private double angle;

	/**
	 * @param reference
	 */
	public Cross(String reference) {
		this(reference, DEFAULT_CROSS_ANGLE);
	}

	/**
	 * 
	 * @param reference
	 * @param angle
	 */
	public Cross(String reference, double angle) {
		super(reference, 4);
		this.angle = angle;
		CrossRoute route0 = new CrossRoute(this);
		CrossRoute route1 = new CrossRoute(this);
		CrossRoute route2 = new CrossRoute(this);
		CrossRoute route3 = new CrossRoute(this);
		route0.setLateral(route1);
		route1.setLateral(route2);
		route2.setLateral(route3);
		route3.setLateral(route0);
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(route0, route2);
		junction[1] = new NodeJunctionImpl(route1, route3);
		junction[2] = new NodeJunctionImpl(route2, route0);
		junction[3] = new NodeJunctionImpl(route3, route1);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitCross(this);
	}

	/**
	 * @return Returns the angle.
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * @param angle
	 *            The angle to set.
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}
}