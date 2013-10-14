package org.mmarini.railways.model.routes;

import java.awt.geom.Point2D;
import java.io.Serializable;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.elements.Curve;

/**
 * @author $$Author: marco $$
 * @version $Id: CurveRoute.java,v 1.6 2012/02/08 22:03:19 marco Exp $
 */
public class CurveRoute extends TrackRoute implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @param length
	 */
	public CurveRoute(Curve curve) {
		super(curve);
	}

	/**
	 * @param curve
	 * @param reverse
	 */
	public CurveRoute(Curve curve, boolean reverse) {
		super(curve, reverse);
	}

	/**
	 * @see org.mmarini.railways.model.routes.RoutePath#calculateLocation(double)
	 */
	@Override
	public Point2D calculateLocation(double location) {
		Topology topology = getTrack().getTopology();
		Point2D p0 = topology.getLocation();
		double x0 = p0.getX();
		double y0 = p0.getY();
		double alpha = Math.toRadians(topology.getDirection());
		Curve curve = (Curve) getTrack();
		double v = Math.signum(curve.getAngle());
		double r = curve.getRadius();
		if (isReverse())
			location = getLength() - location;
		double beta = alpha + v * location / r;
		double x = x0 - v * r * (Math.cos(beta) - Math.cos(alpha));
		double y = y0 - v * r * (Math.sin(beta) - Math.sin(alpha));
		return new Point2D.Double(x, y);
	}
}