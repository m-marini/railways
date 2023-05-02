package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: DeadTrackOutcome.java,v 1.5.16.1 2012/02/04 19:22:57 marco Exp
 *          $
 */
public class DeadTrackOutcome extends AbstractPointRoute implements
		NodeOutcome, Serializable {
	private static final long serialVersionUID = 1L;
	private TrackIncome next;
	private boolean transit;
	private DeadTrackIncome opposite;

	/**
	 * 
	 * @param track
	 */
	public DeadTrackOutcome(DeadTrack track) {
		super(track);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#attach(org.mmarini.railways.model.routes.TrackIncome)
	 */
	@Override
	public void attach(TrackIncome income) {
		next = income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
		next.calculateMovement(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#calculateNextLocation(double)
	 */
	@Override
	public RoutePoint calculateNextLocation(double distance) {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.AbstractRoute#handleTrainEntry(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	protected void handleTrainEntry(MovementContext context) {
		super.handleTrainEntry(context);
		setTransit(true);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return next.hasToStop(ctx);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitableHandler#isTransitable()
	 */
	@Override
	public boolean isTransitable() {
		return next.isTransitable();
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		checkForError(context);
		if (getTrain() == null) {
			handleTrainEntry(context);
		}
		next.moveHead(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#moveTail(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveTail(MovementContext context) {
		Train train = context.getTrain();
		if (getTrain() != train)
			return;
		double transited = context.getTransited();
		if (transited > 0)
			handleTrainExit(context);
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
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(DeadTrackIncome opposite) {
		this.opposite = opposite;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		boolean oldTransit = this.transit;
		this.transit = transit;
		if (oldTransit != transit) {
			next.setTransit(transit);
		}
	}
}