package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NeighbourOutcome.java,v 1.2 2006/09/11 11:30:21 marco Exp $
 */
public interface NeighbourOutcome extends TrainTail {

	/**
	 * @param income
	 */
	public abstract void attach(LineIncome income);
}