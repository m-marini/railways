package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.CurveRoute;
import org.mmarini.railways.model.routes.TrackJunction;
import org.mmarini.railways.model.routes.TrackRoute;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Curve.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class Curve extends AbstractTrack implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean left;
	private double radius;

	/**
	 * @param length
	 * @param left
	 */
	public Curve(double length, boolean left) {
		this(length, RADIUS, left);
	}

	/**
	 * @param length
	 * @param left
	 */
	public Curve(double length, double radius, boolean left) {
		super(length);
		this.radius = radius;
		this.left = left;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitCurve(this);
	}

	/**
	 * Gets the angle in degrees.
	 * 
	 * @return the angle in degrees.
	 */
	public double getAngle() {
		double angle = Math.toDegrees(getLength() / radius);
		return left ? -angle : angle;
	}

	/**
	 * @return Returns the radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @see org.mmarini.railways.model.elements.AbstractTrack#init()
	 */
	@Override
	protected void init() {
		TrackRoute[] route = new TrackRoute[2];
		route[0] = new CurveRoute(this);
		route[1] = new CurveRoute(this, true);
		route[1].setOpposite(route[0]);
		route[0].setOpposite(route[1]);
		setRoute(route);
		TrackJunction[] junction = new TrackJunction[2];
		junction[0] = new TrackJunction(route[0], route[1]);
		junction[1] = new TrackJunction(route[1], route[0]);
		setJunction(junction);
	}

	/**
	 * @param radius
	 *            The radius to set.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}
}