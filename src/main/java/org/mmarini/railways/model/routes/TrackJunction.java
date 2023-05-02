package org.mmarini.railways.model.routes;

import java.io.Serializable;

/**
 * @author $$Author: marco $$
 * @version $Id: TrackJunction.java,v 1.6 2012/02/08 22:03:19 marco Exp $
 */
public class TrackJunction implements Serializable {
	private static final long serialVersionUID = 1L;
	private TrackIncome income;
	private TrackOutcome outcome;

	/**
	 * @param income
	 * @param outcome
	 */
	public TrackJunction(TrackIncome income, TrackOutcome outcome) {
		this.income = income;
		this.outcome = outcome;
	}

	/**
	 * @param junction
	 */
	public void attach(NodeJunction junction) {
		getIncome().attach(junction.getOutcome());
		getOutcome().attach(junction.getIncome());
	}

	/**
	 * @return Returns the income.
	 */
	public TrackIncome getIncome() {
		return income;
	}

	/**
	 * @return Returns the outcome.
	 */
	public TrackOutcome getOutcome() {
		return outcome;
	}
}