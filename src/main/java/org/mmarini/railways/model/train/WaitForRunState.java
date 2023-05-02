package org.mmarini.railways.model.train;

import org.mmarini.railways.model.ManagerInfos;

/**
 * @author $Author: marco $
 * @version $Id: WaitForRunState.java,v 1.5 2012/02/08 22:03:33 marco Exp $
 */
public class WaitForRunState extends TrainStateAdapter {
	/**
	 * 
	 */
	protected WaitForRunState() {
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double time = train.getTime();
		ManagerInfos infos = train.getStation().getManagerInfos();
		infos.addTrainsLifeTime(time);
		infos.addTrainsWaitTime(time);
	}
}
