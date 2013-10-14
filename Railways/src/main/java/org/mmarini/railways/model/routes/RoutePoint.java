package org.mmarini.railways.model.routes;

import java.awt.geom.Point2D;

/**
 * @author $Author: marco $
 * @version $Id: RoutePoint.java,v 1.5 2012/02/08 22:03:19 marco Exp $
 */
public class RoutePoint {
	private RoutePath routePath;
	private double location;
	private boolean invisible;

	/**
	 * 
	 */
	public RoutePoint() {
	}

	/**
	 * @param routePath
	 * @param location
	 */
	public RoutePoint(RoutePath routePath, double location) {
		this.routePath = routePath;
		this.location = location;
	}

	/**
	 * @param path
	 * @param location
	 * @param invisible
	 */
	public RoutePoint(RoutePath path, double location, boolean invisible) {
		routePath = path;
		this.location = location;
		this.invisible = invisible;
	}

	/**
	 * @param distance
	 * @return
	 */
	public RoutePoint calculateNextLocation(double distance) {
		return getRoutePath().calculateNextLocation(this, distance);
	}

	/**
	 * @return Returns the location.
	 */
	public double getLocation() {
		return location;
	}

	/**
	 * @return
	 */
	public Point2D getLocation2D() {
		return getRoutePath().calculateLocation(getLocation());
	}

	/**
	 * @return Returns the routePath.
	 */
	public RoutePath getRoutePath() {
		return routePath;
	}

	/**
	 * @return Returns the visible.
	 */
	public boolean isInvisible() {
		return invisible;
	}

	/**
	 * @param visible
	 *            The visible to set.
	 */
	public void setInvisible(boolean visible) {
		this.invisible = visible;
	}

	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(double location) {
		this.location = location;
	}

	/**
	 * @param routePath
	 *            The routePath to set.
	 */
	public void setRoutePath(RoutePath routePath) {
		this.routePath = routePath;
	}
}