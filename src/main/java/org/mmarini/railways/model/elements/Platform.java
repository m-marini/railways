package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.PlatformRoute;
import org.mmarini.railways.model.routes.TrackJunction;
import org.mmarini.railways.model.routes.TrackRoute;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Platform.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class Platform extends Segment implements Serializable {
	private static final String EMPTY_STRING = "";
	private static final long serialVersionUID = 1L;
	private String name;

	/**
	 * @param length
	 */
	public Platform(double length) {
		super(length);
		name = EMPTY_STRING;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitPlatform(this);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.mmarini.railways.model.elements.AbstractTrack#init()
	 */
	@Override
	protected void init() {
		TrackRoute[] route = new TrackRoute[2];
		route[0] = new PlatformRoute(this);
		route[1] = new PlatformRoute(this, true);
		route[1].setOpposite(route[0]);
		route[0].setOpposite(route[1]);
		setRoute(route);
		TrackJunction[] junction = new TrackJunction[2];
		junction[0] = new TrackJunction(route[0], route[1]);
		junction[1] = new TrackJunction(route[1], route[0]);
		setJunction(junction);
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
}