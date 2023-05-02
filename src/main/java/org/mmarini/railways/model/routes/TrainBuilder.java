package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: TrainBuilder.java,v 1.6 2012/02/08 22:03:19 marco Exp $
 */
public class TrainBuilder extends AbstractRoute implements NeighbourOutcome,
		Serializable {
	private static final long serialVersionUID = 1L;
	private LineIncome next;
	private TrainCanceller opposite;

	/**
	 *
	 */
	public TrainBuilder() {
		super(null);
	}

	/**
	 * @param train
	 */
	public void add(Train train) {
		train.setHead(this);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NeighbourOutcome#attach(org.mmarini.railways.model.routes.LineIncome)
	 */
	@Override
	public void attach(LineIncome income) {
		this.next = income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
		if (getTrain() == null) {
			getNext().calculateMovement(context);
		} else {
			context.setMovement(0);
		}
	}

	/**
	 * @return Returns the income.
	 */
	public LineIncome getNext() {
		return next;
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
		return false;
	}

	/**
	 * @see org.mmarini.railways.model.routes.AbstractPointRoute#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return getTrain() != null;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		Train currentTrain = getTrain();
		Train ctxTrain = context.getTrain();
		if (currentTrain == null) {
			handleTrainEntry(context);
			ctxTrain.run();
			getNext().moveHead(context);
		} else {
			ctxTrain.handleSemaphoreBusy();
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
		updateTransited(context);
		if (getTransited() >= train.getLength()) {
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
			train.setHead(opposite);
		}
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(TrainCanceller opposite) {
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