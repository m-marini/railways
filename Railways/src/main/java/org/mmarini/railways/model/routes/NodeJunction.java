package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NodeJunction.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public interface NodeJunction {

	/**
	 * Attachs a track junction.
	 * 
	 * @param junction
	 *            the track junction
	 */
	public abstract void attach(TrackJunction junction);

	/**
	 * Gets the income route.
	 * 
	 * @return the income route.
	 */
	public abstract NodeIncome getIncome();

	/**
	 * Gets the outcome route.
	 * 
	 * @return the outcome route.
	 */
	public abstract NodeOutcome getOutcome();
}