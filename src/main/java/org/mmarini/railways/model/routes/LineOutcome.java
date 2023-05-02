package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: LineOutcome.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class LineOutcome extends AbstractPointRoute implements NodeIncome,
		Serializable {
	private static final long serialVersionUID = 1L;
	private NeighbourIncome next;
	private TrackOutcome previous;
	private LineIncome opposite;

	/**
	 *
	 */
	public LineOutcome(Line line) {
		super(line);
	}

	/**
	 * @param income
	 */
	public void attach(NeighbourIncome income) {
		next = income;
	}

	/**
	 * @param outcome
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
		if (isBusy()) {
			context.setMovement(0);
		} else {
			next.calculateMovement(context);
		}
	}

	/**
	 * @param distance
	 * @return
	 */
	public RoutePoint calculateNextLocation(double distance) {
		return previous.calculateNextLocation(distance);
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
		return isBusy();
	}

	/**
	 * Returns true if the LineOutcome is busy.
	 * 
	 * @return true if the LineOutcome is busy.
	 */
	@Override
	public boolean isBusy() {
		if (next == null)
			return true;
		return next.isBusy();
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
			if (isBusy()) {
				train.handleSemaphoreBusy();
				context.setTransited(context.getMovement());
				previous.moveTail(context);
			} else {
				next.moveHead(context);
			}
		} else if (!isBusy()) {
			train.run();
			next.moveHead(context);
		}
	}

	/**
	 * @param context
	 */
	public void moveTail(MovementContext context) {
		Train train = context.getTrain();
		if (getTrain() != train)
			return;
		double transited = context.getTransited();
		previous.moveTail(context);
		if (transited >= train.getLength()) {
			handleTrainExit(context);
		}
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
	public void setOpposite(LineIncome opposite) {
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