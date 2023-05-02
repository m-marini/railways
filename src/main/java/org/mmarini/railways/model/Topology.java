package org.mmarini.railways.model;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * @author $Author: marco $
 * @version $Id: Topology.java,v 1.4 2012/02/08 22:03:18 marco Exp $
 */
public class Topology implements Serializable {
	private static final long serialVersionUID = 1L;
	private Point2D location;
	private double direction;

	/**
	 * 
	 */
	public Topology() {
		location = new Point2D.Double();
	}

	/**
	 * @param location
	 * @param direction
	 */
	public Topology(Point2D location, double direction) {
		this();
		this.location.setLocation(location);
		this.direction = direction;
	}

	/**
	 * @param topology
	 */
	public Topology(Topology topology) {
		this(topology.location, topology.direction);
	}

	/**
	 * @return Returns the direction.
	 */
	public double getDirection() {
		return direction;
	}

	/**
	 * @return Returns the location.
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 * @param direction
	 *            The direction to set.
	 */
	public void setDirection(double direction) {
		this.direction = direction;
	}

	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(Point2D location) {
		this.location = location;
	}
}