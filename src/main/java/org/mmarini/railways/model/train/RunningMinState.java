package org.mmarini.railways.model.train;

import org.mmarini.railways.model.routes.MovementContext;
import org.mmarini.railways.model.routes.TrainHead;

/**
 * @author $Author: marco $
 * @version $Id: RunningMinState.java,v 1.6 2012/02/08 22:03:33 marco Exp $
 */
public class RunningMinState extends RunningState {
	/**
	 * 
	 */
	protected RunningMinState() {
		super(0.5);
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
			if (!head.hasToStop(context)) {
				TrainState newState = RUNNING_FAST_STATE;
				changeState(train, newState);
				newState.handleDispatch(train);
				return;
			}
		}
		super.handleDispatch(train);
	}
}
