package org.mmarini.railways.model.train;

/**
 * 
 * @author us00852
 * @version $Id: TrainState.java,v 1.2 2012/02/08 22:03:33 marco Exp $
 */
public interface TrainState {
	public static final EnteringState ENTERING_STATE = new EnteringState();
	public static final BrakeingState BRAKEING_STATE = new BrakeingState();
	public static final RunningMinState RUNNIN_MIN_STATE = new RunningMinState();
	public static final RunningFastState RUNNING_FAST_STATE = new RunningFastState();
	public static final WaitForSemState WAIT_FOR_SEM_STATE = new WaitForSemState();
	public static final WaitForRunState WAIT_FOR_RUN_STATE = new WaitForRunState();
	public static final WaitForLoadedState WAIT_FOR_LOADED_STATE = new WaitForLoadedState();

	/**
	 * @param ctx
	 * @param state
	 */
	public abstract void changeState(Train ctx, TrainState state);

	/**
	 * @return
	 */
	public abstract String getStateId();

	/**
	 * @param ctx
	 */
	public abstract void handleBrake(Train ctx);

	/**
	 * @param ctx
	 */
	public abstract void handleDispatch(Train ctx);

	/**
	 * @param ctx
	 */
	public abstract void handleRun(Train ctx);

	/**
	 * @param ctx
	 */
	public abstract void handleSemaphoreBusy(Train ctx);
}
