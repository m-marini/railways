/*
 * Created on 15-set-2005
 */
package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.routes.NeighbourJunction;
import org.mmarini.railways.model.visitor.NeighbourVisitor;

/**
 * @author $Author: marco $
 * @version $Id: Neighbour.java,v 1.3 2012/02/08 22:03:21 marco Exp $
 */
public interface Neighbour {
	/**
	 * @param visitor
	 */
	public abstract void accept(NeighbourVisitor visitor);

	/**
	 * @param station
	 */
	public abstract void attach(Station station);

	/**
	 * @param line
	 */
	public abstract void attachLine(Line line);

	/**
	 * @param time
	 */
	public abstract void dispatch(double time);

	/**
	 * @return Returns the junction.
	 */
	public abstract NeighbourJunction getJunction();

	/**
	 * @return Returns the reference.
	 */
	public abstract String getName();

	/**
	 * @return
	 */
	public abstract boolean isOutcome();
}