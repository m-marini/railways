package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.Cross;

/**
 * @author $$Author: marco $$
 * @version $Id: CrossNodeRoute.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class CrossRoute extends PointRoute {
	private CrossRoute lateral;
	private boolean transit;

	/**
	 * @param cross
	 */
	public CrossRoute(Cross cross) {
		super(cross);
	}

	/**
	 * @return Returns the lateral.
	 */
	public CrossRoute getLateral() {
		return lateral;
	}

	/**
	 * @param lateral
	 *            The lateral to set.
	 */
	public void setLateral(CrossRoute lateral) {
		this.lateral = lateral;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		getNext().setTransit(transit);
		if (transit != this.transit) {
			this.transit = transit;
			getLateral().setTransit(transit);
		}
	}
}