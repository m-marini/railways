package org.mmarini.railways.model.routes;

import java.awt.geom.Point2D;
import java.io.Serializable;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.elements.Segment;

/**
 * @author $$Author: marco $$
 * @version $Id: SegmentRoute.java,v 1.6 2012/02/08 22:03:19 marco Exp $
 */
public class SegmentRoute extends TrackRoute implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @param length
	 */
	public SegmentRoute(Segment track) {
		super(track);
	}

	/**
	 * @param track
	 * @param reverse
	 */
	public SegmentRoute(Segment track, boolean reverse) {
		super(track, reverse);
	}

	/**
	 * @see org.mmarini.railways.model.routes.RoutePath#calculateLocation(double)
	 */
	@Override
	public Point2D calculateLocation(double location) {
		Topology topology = getTrack().getTopology();
		Point2D offset = topology.getLocation();
		double dir = topology.getDirection();
		double angle = Math.toRadians(dir);
		if (isReverse())
			location = getLength() - location;
		double x = offset.getX() + location * Math.sin(angle);
		double y = offset.getY() - location * Math.cos(angle);
		return new Point2D.Double(x, y);
	}
}