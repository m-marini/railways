package org.mmarini.railways.model.routes;

import java.awt.geom.Point2D;
import java.io.Serializable;

import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: TrainCanceller.java,v 1.7 2012/02/08 22:03:18 marco Exp $
 */
public class TrainCanceller extends AbstractRoute implements NeighbourIncome,
		Serializable, RoutePath {
	private static final long serialVersionUID = 1L;
	private LineOutcome previous;
	private boolean transit;
	private TrainListener listener;
	private TrainBuilder opposite;
	private double length = 3000;

	/**
	 * 
	 *
	 */
	public TrainCanceller() {
		super(null);
	}

	/**
	 * Adds a train listener
	 * 
	 * @param listener
	 *            a train listener
	 */
	public void addTrainListener(TrainListener listener) {
		this.listener = listener;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourIncome#attach(org.mmarini.railways.model.routes.LineOutcome)
	 */
	@Override
	public void attach(LineOutcome outcome) {
		this.previous = outcome;
	}

	/**
	 * @see org.mmarini.railways.model.routes.RoutePath#calculateLocation(double)
	 */
	@Override
	public Point2D calculateLocation(double location) {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.routes.RoutePath#calculateNextLocation(org.mmarini.railways.model.routes.RoutePoint,
	 *      double)
	 */
	@Override
	public RoutePoint calculateNextLocation(RoutePoint offset, double distance) {
		if (offset.getRoutePath() != this)
			throw new IllegalArgumentException("Illegal argument on " + this);
		double loc = offset.getLocation();
		double newLoc = loc - distance;
		if (newLoc >= 0)
			return new RoutePoint(this, newLoc, true);
		distance -= loc;
		return previous.calculateNextLocation(distance);
	}

	/**
	 * Cancel the train
	 */
	public void cancellTrain() {
		Train train = getTrain();
		if (train != null)
			train.setHead(null);
		setTrain(null);
		setTransit(false);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return new RoutePoint(this, getTransited(), true);
	}

	/**
	 * @see org.mmarini.railways.model.routes.AbstractRoute#handleTrainEntry(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	protected void handleTrainEntry(MovementContext context) {
		super.handleTrainEntry(context);
		setTransit(true);
		if (listener != null)
			listener.handleTrainEntry(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.AbstractRoute#handleTrainExit(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	protected void handleTrainExit(MovementContext context) {
		if (listener != null)
			listener.handleTrainExit(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return false;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourIncome#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return transit;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		checkForError(context);
		Train currentTrain = getTrain();
		if (currentTrain == null) {
			/*
			 * Register the incoming train
			 */
			handleTrainEntry(context);
		}

		context.setTransited(context.getMovement() + getTransited());
		moveTail(context);
	}

	/**
	 * @param context
	 */
	public void moveTail(MovementContext context) {
		checkForError(context);
		updateTransited(context);
		previous.moveTail(context);
		if (getTransited() >= getTrain().getLength() + length) {
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
		double transited = train.getLength() - getTransited();
		if (transited >= 0) {
			opposite.setTrainLocation(train, transited);
			previous.reverse(train);
		}
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(TrainBuilder opposite) {
		this.opposite = opposite;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		this.transit = transit;
	}
}