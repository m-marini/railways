package org.mmarini.railways.model.train;

import org.mmarini.railways.model.routes.MovementContext;
import org.mmarini.railways.model.routes.TrainHead;

/**
 * @author $Author: marco $
 * @version $Id: RunningFastState.java,v 1.5.16.1 2012/02/04 19:22:55 marco Exp
 *          $
 */
public class RunningFastState extends RunningState {
	/**
	 * 
	 */
	protected RunningFastState() {
		super(MAX_SPEED);
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double stopDistance = train.computeStopDistance();
		TrainHead head = train.getHead();
		if (head != null && stopDistance > 0) {
			MovementContext context = new MovementContext(train, stopDistance,
					0);
			if (head.hasToStop(context)) {
				TrainState newState = RUNNIN_MIN_STATE;
				changeState(train, newState);
				newState.handleDispatch(train);
				return;
			}
		}
		super.handleDispatch(train);
	}
}