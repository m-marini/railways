package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NeighbourIncome.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface NeighbourIncome extends TrainHead {

	/**
	 * @param outcome
	 */
	public abstract void attach(LineOutcome outcome);

	/**
	 * Return true if is busy.
	 * 
	 * @return true if is busy.
	 */
	@Override
	public abstract boolean isBusy();
}