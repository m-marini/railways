package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;
import java.util.EventObject;

import org.mmarini.railways.model.train.Train;

/**
 * @author $Author: marco $
 * @version $Id: ElementSelectionEvent.java,v 1.1.2.1 2005/08/30 18:01:18 marco
 *          Exp $
 */
public class TrainGraphEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private Point2D location;
	private Train train;

	/**
	 * @param source
	 */
	public TrainGraphEvent(Object source) {
		super(source);
	}

	/**
	 * @return Returns the location.
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 * @return Returns the train.
	 */
	public Train getTrain() {
		return train;
	}

	/**
	 * @param location
	 */
	public void setLocation(Point2D location) {
		this.location = location;
	}

	/**
	 * @param train
	 *            The train to set.
	 */
	public void setTrain(Train train) {
		this.train = train;
	}
}