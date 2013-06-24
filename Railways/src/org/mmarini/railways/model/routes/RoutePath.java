package org.mmarini.railways.model.routes;

import java.awt.geom.Point2D;

/**
 * @author $Author: marco $
 * @version $Id: RoutePath.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface RoutePath {

	/**
	 * @param location
	 * @return
	 */
	public abstract Point2D calculateLocation(double location);

	/**
	 * @param offset
	 * @param distance
	 * @return
	 */
	public abstract RoutePoint calculateNextLocation(RoutePoint offset,
			double distance);

}