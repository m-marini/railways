package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.train.Train;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $$Author: marco $$
 * @version $Id: DeadTrackIncome.java,v 1.5 2012/02/08 22:03:18 marco Exp $
 */
public class DeadTrackIncome extends AbstractPointRoute implements NodeIncome {
	private TrackOutcome previous;
	private DeadTrackOutcome opposite;

	/**
	 * @param node
	 */
	public DeadTrackIncome(DeadTrack track) {
		super(track);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeIncome#attach(org.mmarini.railways.model.routes.TrackOutcome)
	 */
	@Override
	public void attach(TrackOutcome outcome) {
		previous = outcome;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
		context.setMovement(0);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return previous.getTrainLocation();
	}

	/**
	 * @see org.mmarini.railways.model.routes.AbstractRoute#handleTrainExit(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	protected void handleTrainExit(MovementContext train) {
		super.handleTrainExit(train);
		setTransit(false);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return true;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitableHandler#isTransitable()
	 */
	@Override
	public boolean isTransitable() {
		return true;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		checkForError(context);
		Train train = context.getTrain();
		if (getTrain() == null) {
			handleTrainEntry(context);
			train.stop();
			context.setTransited(context.getMovement());
			previous.moveTail(context);
		}
	}

	/**
	 * @param context
	 */
	public void moveTail(MovementContext context) {
		Train currentTrain = getTrain();
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
		log.error("Train route   = " + currentTrain);
		log.error("Length        = " + currentTrain.getLength());
		log.error("Speed         = " + currentTrain.getSpeed());
		log.error("In            = " + currentTrain.getElementLocation());
		throw new AssertionError(assertion);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#reverse(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void reverse(Train train) {
		if (train != getTrain())
			throw new AssertionError(train + " != " + getTrain());
		setTrain(null);
		opposite.setTrain(train);
		previous.reverse(train);
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(DeadTrackOutcome opposite) {
		this.opposite = opposite;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		opposite.setTransit(transit);
	}
}