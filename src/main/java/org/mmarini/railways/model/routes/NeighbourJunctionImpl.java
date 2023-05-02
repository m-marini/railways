package org.mmarini.railways.model.routes;

import java.io.Serializable;

/**
 * @author $$Author: marco $$
 * @version $Id: NeighbourJunctionImpl.java,v 1.2 2006/09/11 11:30:21 marco Exp
 *          $
 */
public class NeighbourJunctionImpl implements NeighbourJunction, Serializable {
	private static final long serialVersionUID = 1L;
	private NeighbourOutcome outcome;
	private NeighbourIncome income;

	/**
	 * @param income
	 * @param outcome
	 */
	public NeighbourJunctionImpl(NeighbourIncome income,
			NeighbourOutcome outcome) {
		this.outcome = outcome;
		this.income = income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourJunction#attach(org.mmarini.railways.model.routes.LineJunction)
	 */
	@Override
	public void attach(LineJunction junction) {
		getOutcome().attach(junction.getIncome());
		getIncome().attach(junction.getOutcome());
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourJunction#getIncome()
	 */
	@Override
	public NeighbourIncome getIncome() {
		return income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourJunction#getOutcome()
	 */
	@Override
	public NeighbourOutcome getOutcome() {
		return outcome;
	}
}