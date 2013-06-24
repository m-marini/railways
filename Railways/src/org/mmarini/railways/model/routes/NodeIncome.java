package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NodeIncome.java,v 1.2 2006/09/11 11:30:20 marco Exp $
 */
public interface NodeIncome extends TrainHead, Route {

	/**
	 * @param outcome
	 */
	public abstract void attach(TrackOutcome outcome);

}