package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: NodeOutcome.java,v 1.2 2006/09/11 11:30:20 marco Exp $
 */
public interface NodeOutcome extends TrainTail, Route {

	/**
	 * @param income
	 */
	public abstract void attach(TrackIncome income);

	/**
	 * @param offset
	 * @param distance
	 * @return
	 */
	public abstract RoutePoint calculateNextLocation(double distance);
}