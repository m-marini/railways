package org.mmarini.railways.model.routes;

import java.io.Serializable;

/**
 * @author $$Author: marco $$
 * @version $Id: LineJunction.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class LineJunction implements Serializable {
	private static final long serialVersionUID = 1L;
	private LineIncome income;
	private LineOutcome outcome;

	/**
	 * @param income
	 * @param outcome
	 */
	public LineJunction(LineIncome income, LineOutcome outcome) {
		this.income = income;
		this.outcome = outcome;
	}

	/**
	 * @param junction
	 */
	public void attach(NeighbourJunction junction) {
		income.attach(junction.getOutcome());
		outcome.attach(junction.getIncome());
		junction.attach(this);
	}

	/**
	 * @return Returns the income.
	 */
	public LineIncome getIncome() {
		return income;
	}

	/**
	 * @return Returns the outcome.
	 */
	public LineOutcome getOutcome() {
		return outcome;
	}
}