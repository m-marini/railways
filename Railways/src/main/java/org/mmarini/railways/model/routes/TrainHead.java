package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: TrainHead.java,v 1.3 2012/02/08 22:03:19 marco Exp $
 */
public interface TrainHead extends TransitHandler {

	/**
	 * @param context
	 */
	public abstract void calculateMovement(MovementContext context);

	/**
	 * @return
	 */
	public abstract RoutePoint getTrainLocation();

	/**
	 * @param ctx
	 *            movement context
	 * @return
	 */
	public abstract boolean hasToStop(MovementContext ctx);

	/**
	 * @return
	 */
	public abstract boolean isBusy();

	/**
	 * @param context
	 */
	public abstract void moveHead(MovementContext context);

	/**
	 * @param train
	 */
	public abstract void reverse(Train train);
}