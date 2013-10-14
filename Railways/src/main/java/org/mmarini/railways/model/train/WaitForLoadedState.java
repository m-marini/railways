package org.mmarini.railways.model.train;

/**
 * @author $Author: marco $
 * @version $Id: WaitForLoadedState.java,v 1.2.20.1 2012/02/04 19:22:55 marco
 *          Exp $
 */
public class WaitForLoadedState extends TrainStateAdapter {

	public static final double LOADING_TIME = 60.;

	/**
	 * 
	 */
	protected WaitForLoadedState() {
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double time = train.getTime() + train.getLoadTimeout();
		train.setLoadTimeout(time);
		if (time > LOADING_TIME) {
			train.playStopped();
			train.setArrived(true);
			changeState(train, WAIT_FOR_RUN_STATE);
		}
	}
}
