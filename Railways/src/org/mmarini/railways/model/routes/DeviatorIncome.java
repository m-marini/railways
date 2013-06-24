package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: DeviatorIncome.java,v 1.7 2012/02/08 22:03:18 marco Exp $
 */
public class DeviatorIncome implements NodeIncome, TrainTail, Serializable {
	private static final long serialVersionUID = 1L;
	private TrackOutcome previous;
	private DeviatorOutcome next;
	private boolean transit;
	private Deviator deviator;
	private boolean autoLock;

	/**
	 * @param deviator
	 */
	public DeviatorIncome(Deviator deviator) {
		this.deviator = deviator;
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
		next.calculateMovement(context);
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
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return next.hasToStop(ctx);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return false;
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
		if (next == null)
			return false;
		return next.isTransitable();
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		next.moveHead(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#moveTail(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveTail(MovementContext context) {
		this.previous.moveTail(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#reverse(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void reverse(Train train) {
		previous.reverse(train);
	}

	/**
	 * @param autoLock
	 *            the autoLock to set
	 */
	public void setAutoLock(boolean autoLock) {
		this.autoLock = autoLock;
	}

	/**
	 * @param next
	 *            The next to set.
	 */
	public void setNext(DeviatorOutcome next) {
		this.next = next;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		this.transit = transit;
		if (next != null)
			next.setTransit(transit);
		if (transit && autoLock) {
			deviator.lock();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return deviator.toString();
	}
}