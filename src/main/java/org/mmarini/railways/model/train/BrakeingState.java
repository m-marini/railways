package org.mmarini.railways.model.train;

/**
 * @author $Author: marco $
 * @version $Id: BrakeingState.java,v 1.7 2012/02/08 22:03:33 marco Exp $
 */
public class BrakeingState extends RunningState {

	/**
	 * 
	 */
	public BrakeingState() {
		super(0);
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		if (train.getSpeed() == 0) {
			changeState(train, WAIT_FOR_RUN_STATE);
			train.playBraking();
			return;
		}
		super.handleDispatch(train);
	}
}
