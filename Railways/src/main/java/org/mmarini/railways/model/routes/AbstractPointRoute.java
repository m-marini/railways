package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractPointRoute.java,v 1.4.16.1 2012/02/04 19:22:57 marco
 *          Exp $
 */
public abstract class AbstractPointRoute implements TrainHead {
	private Train train;
	private StationElement element;

	/**
	 * @param element
	 */
	public AbstractPointRoute(StationElement element) {
		this.element = element;
	}

	/**
	 * 
	 * @param context
	 */
	protected void checkForError(MovementContext context) {
		Train currentTrain = train;
		Train ctxTrain = context.getTrain();
		if (currentTrain == null || currentTrain == ctxTrain)
			return;
		String assertion = currentTrain + " != " + ctxTrain;
		Logger log = LoggerFactory.getLogger(getClass());
		log.error(assertion);
		log.error("Train context = " + ctxTrain);
		log.error("Length        = " + ctxTrain.getLength());
		log.error("Speed         = " + ctxTrain.getSpeed());
		log.error("In            = " + ctxTrain.getElementLocation());
		log.error("Route         = " + this);
		if (this instanceof TrackRoute)
			log.error("Track length  = " + ((TrackRoute) this).getLength());
		log.error("Train route   = " + currentTrain);
		log.error("Length        = " + currentTrain.getLength());
		log.error("Speed         = " + currentTrain.getSpeed());
		log.error("In            = " + currentTrain.getElementLocation());
		if (this instanceof AbstractRoute)
			log.error("Transited     = "
					+ ((AbstractRoute) this).getTransited());
		throw new AssertionError(assertion);
	}

	/**
	 * @return Returns the node.
	 */
	protected StationElement getElement() {
		return element;
	}

	/**
	 * @return Returns the train.
	 */
	public Train getTrain() {
		return train;
	}

	/**
	 * @param context
	 */
	protected void handleTrainEntry(MovementContext context) {
		Train train = context.getTrain();
		setTrain(train);
		train.setHead(this);
	}

	/**
	 * @param train
	 */
	protected void handleTrainExit(MovementContext train) {
		setTrain(null);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return false;
	}

	/**
	 * @param train
	 *            The train to set.
	 */
	protected void setTrain(Train train) {
		this.train = train;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StationElement elem = getElement();
		if (elem != null)
			return elem.toString();
		return super.toString();
	}
}