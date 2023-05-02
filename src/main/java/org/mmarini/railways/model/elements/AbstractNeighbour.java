package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.routes.NeighbourJunction;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractNeighbour.java,v 1.4.16.1 2012/02/04 19:22:55 marco Exp
 *          $
 */
public abstract class AbstractNeighbour implements Neighbour {

	private NeighbourJunction junction;
	private String name;
	private boolean outcome;

	/**
	 * 
	 */
	protected AbstractNeighbour() {
	}

	/**
	 * @return Returns the junction.
	 */
	@Override
	public NeighbourJunction getJunction() {
		return junction;
	}

	/**
	 * @return Returns the reference.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see org.mmarini.railways.model.elements.Neighbour#isOutcome()
	 */
	@Override
	public boolean isOutcome() {
		return outcome;
	}

	/**
	 * @param junction
	 *            The junction to set.
	 */
	protected void setJunction(NeighbourJunction junction) {
		this.junction = junction;
	}

	/**
	 * @param reference
	 *            The reference to set.
	 */
	public void setName(String reference) {
		this.name = reference;
	}

	/**
	 * @param outcome
	 *            The outcome to set.
	 */
	public void setOutcome(boolean outcome) {
		this.outcome = outcome;
	}
}