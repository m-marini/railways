package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: SemaphoreOutcome.java,v 1.6.14.1 2012/02/04 19:22:57 marco Exp
 *          $
 */
public class SemaphoreOutcome extends AbstractPointRoute implements
		NodeOutcome, Serializable {
	private static final long serialVersionUID = 1L;
	private SemaphoreIncome previous;
	private TrackIncome next;
	private boolean transit;
	private SemaphoreIncome opposite;

	/**
	 * @param node
	 */
	public SemaphoreOutcome(Semaphore node) {
		super(node);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#attach(org.mmarini.railways.model.routes.TrackIncome)
	 */
	@Override
	public void attach(TrackIncome income) {
		this.next = income;
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
	 * @return Returns the transit.
	 */
	public boolean isTransit() {
		return transit;
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
			throw new AssertionError(train + " != " + getTrain()
					+ " in reverse of " + toString());
		setTrain(null);
		opposite.setTrain(train);
		previous.reverse(train);
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(SemaphoreIncome opposite) {
		this.opposite = opposite;
	}

	/**
	 * @param previous
	 *            The previous to set.
	 */
	public void setPrevious(SemaphoreIncome previous) {
		this.previous = previous;
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