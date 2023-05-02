package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.SegmentRoute;
import org.mmarini.railways.model.routes.TrackJunction;
import org.mmarini.railways.model.routes.TrackRoute;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Segment.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class Segment extends AbstractTrack implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @param length
	 */
	public Segment(double length) {
		super(length);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitSegment(this);
	}

	/**
	 * @see org.mmarini.railways.model.elements.AbstractTrack#init()
	 */
	@Override
	protected void init() {
		TrackRoute[] route = new TrackRoute[2];
		route[0] = new SegmentRoute(this);
		route[1] = new SegmentRoute(this, true);
		route[1].setOpposite(route[0]);
		route[0].setOpposite(route[1]);
		setRoute(route);
		TrackJunction[] junction = new TrackJunction[2];
		junction[0] = new TrackJunction(route[0], route[1]);
		junction[1] = new TrackJunction(route[1], route[0]);
		setJunction(junction);
	}
}