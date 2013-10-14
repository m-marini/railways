package org.mmarini.railways.model;

import java.util.EventObject;

import org.mmarini.railways.model.train.Train;

/**
 * @author $Author: marco $
 * @version $Id: StationEvent.java,v 1.5 2006/08/29 16:42:38 marco Exp $
 */
public class StationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private Train train;

	/**
	 * @param source
	 */
	public StationEvent(Object source) {
		super(source);
	}

	/**
	 * @return Returns the train.
	 */
	public Train getTrain() {
		return train;
	}

	/**
	 * @param train
	 *            The train to set.
	 */
	public void setTrain(Train train) {
		this.train = train;
	}
}