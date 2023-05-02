package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: TrackIncome.java,v 1.2 2006/09/11 11:30:21 marco Exp $
 */
public interface TrackIncome extends TrainHead, Route {

	/**
	 * @param outcome
	 */
	public abstract void attach(NodeOutcome outcome);
}