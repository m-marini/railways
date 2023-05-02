package org.mmarini.railways.model.train;

import org.mmarini.railways.model.ManagerInfos;
import org.mmarini.railways.model.routes.TrainHead;

/**
 * @author $Author: marco $
 * @version $Id: WaitForSemState.java,v 1.6 2012/02/08 22:03:33 marco Exp $
 */
public class WaitForSemState extends TrainStateAdapter {
	/**
	 * 
	 */
	protected WaitForSemState() {
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double time = train.getTime();
		ManagerInfos infos = train.getStation().getManagerInfos();
		TrainHead head = train.getHead();
		if (head.isBusy()) {
			infos.addTrainsLifeTime(time);
			infos.addTrainsWaitTime(time);
			return;
		}
		changeState(train, RUNNING_FAST_STATE);
		return;
	}
}
