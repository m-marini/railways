package org.mmarini.railways.model.train;

/**
 * @author $Author: marco $
 * @version $Id: EnteringState.java,v 1.6 2012/02/08 22:03:33 marco Exp $
 */
public class EnteringState extends TrainStateAdapter {

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double time = train.getTime();
		double entryTimer = train.getEntryTimer();
		if (time < entryTimer) {
			train.setEntryTimer(entryTimer - time);
			return;
		}
		train.setHead(train.getBuilder());
		train.setBuilder(null);
		train.setEntryTimer(0);
		changeState(train, RUNNING_FAST_STATE);
	}
}
