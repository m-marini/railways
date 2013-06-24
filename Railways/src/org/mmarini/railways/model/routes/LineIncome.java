package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: LineIncome.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class LineIncome extends AbstractPointRoute implements NodeOutcome,
		Serializable {
	private static final long serialVersionUID = 1L;
	private NeighbourOutcome previous;
	private TrackIncome next;
	private boolean transit;
	private boolean held;
	private LineOutcome opposite;

	/**
	 * @param line
	 */
	public LineIncome(Line line) {
		super(line);
	}

	/**
	 * @param outcome
	 */
	public void attach(NeighbourOutcome outcome) {
		previous = outcome;
	}

	/**
	 * @param income
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
		if (isBusy()) {
			context.setMovement(0);
		} else {
			next.calculateMovement(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#calculateNextLocation(double)
	 */
	@Override
	public RoutePoint calculateNextLocation(double distance) {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateNextSemaphoreDistance()
	 */
	public double calculateNextSemaphoreDistance() {
		return 0;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return isBusy();
	}

	/**
	 * Returns true if the LineIncome is busy.
	 * 
	 * @return true if the LineIncome is busy.
	 */
	@Override
	public boolean isBusy() {
		return held || transit || !isTransitable();
	}

	/**
	 * @return the held
	 */
	public boolean isHeld() {
		return held;
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
		Train train = context.getTrain();
		if (getTrain() == null) {
			handleTrainEntry(context);
			if (isBusy()) {
				train.handleSemaphoreBusy();
				context.setTransited(context.getMovement());
				previous.moveTail(context);
			} else {
				setTransit(true);
				next.moveHead(context);
			}
		} else if (!isBusy()) {
			setTransit(true);
			train.run();
			next.moveHead(context);
		}
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
	 * @param held
	 *            The held to set.
	 */
	public void setHeld(boolean held) {
		this.held = held;
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(LineOutcome opposite) {
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