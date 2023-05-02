package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: TrainTail.java,v 1.2 2006/09/11 11:30:20 marco Exp $
 */
public interface TrainTail {

	/**
	 * @param context
	 */
	public abstract void moveTail(MovementContext context);

	/**
	 * @param train
	 */
	public abstract void reverse(Train train);
}