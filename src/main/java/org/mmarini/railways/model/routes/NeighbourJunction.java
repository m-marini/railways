package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NeighbourJunction.java,v 1.5.16.1 2012/02/04 19:22:57 marco Exp
 *          $
 */
public interface NeighbourJunction {

	/**
	 * Attachs a LineJunction.
	 * 
	 * @params junction the junction.
	 */
	public abstract void attach(LineJunction junction);

	/**
	 * Gets the income route.
	 * 
	 * @return the income route.
	 */
	public abstract NeighbourIncome getIncome();

	/**
	 * Gets the outcome route.
	 * 
	 * @return the outcome route.
	 */
	public abstract NeighbourOutcome getOutcome();
}