package org.mmarini.railways.model.train;

import org.mmarini.railways.model.RailwayConstants;

/**
 * @author $Author: marco $
 * @version $Id: AbstractTrainState.java,v 1.5.16.1 2012/02/04 19:22:55 marco
 *          Exp $
 */
public abstract class AbstractTrainState implements TrainState,
		RailwayConstants {
	/**
	 * @param ctx
	 * @param state
	 */
	@Override
	public void changeState(Train ctx, TrainState state) {
		ctx.changeState(state);
	}

	/**
	 * @return
	 */
	@Override
	public String getStateId() {
		return getClass().getName();
	}

	/**
	 * @param ctx
	 */
	@Override
	public abstract void handleBrake(Train ctx);

	/**
	 * @param ctx
	 */
	@Override
	public abstract void handleDispatch(Train ctx);

	/**
	 * @param ctx
	 */
	@Override
	public abstract void handleRun(Train ctx);

	/**
	 * @param ctx
	 */
	@Override
	public abstract void handleSemaphoreBusy(Train ctx);
}
