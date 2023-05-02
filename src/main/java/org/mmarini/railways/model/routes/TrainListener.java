package org.mmarini.railways.model.routes;

/**
 * @author $Author: marco $
 * @version $Id: TrainListener.java,v 1.2 2006/09/11 11:30:21 marco Exp $
 */
public interface TrainListener {

	/**
	 * @param context
	 */
	public abstract void handleTrainEntry(MovementContext context);

	/**
	 * @param context
	 */
	public abstract void handleTrainExit(MovementContext context);
}