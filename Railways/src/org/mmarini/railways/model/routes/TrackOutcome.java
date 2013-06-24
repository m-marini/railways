package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: TrackOutcome.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface TrackOutcome extends TrainTail, Route {

	/**
	 * @param income
	 */
	public abstract void attach(NodeIncome income);

	/**
	 * @param distance
	 * @return
	 */
	public abstract RoutePoint calculateNextLocation(double distance);

	/**
	 * @return
	 */
	public abstract RoutePoint getTrainLocation();
}