package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: CrossDeviatorOutcome.java,v 1.2.10.1 2012/02/04 19:22:57 marco
 *          Exp $
 */
public class CrossDeviatorOutcome implements NodeOutcome, TrainHead,
		Serializable {
	private static final long serialVersionUID = 1L;
	private TrackIncome next;
	private CrossDeviatorIncome previous;
	private boolean transit;
	private CrossDeviator deviator;

	/**
	 * @param deviator
	 */
	public CrossDeviatorOutcome(CrossDeviator deviator) {
		this.deviator = deviator;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#attach(org.mmarini.railways.model.routes.TrackIncome)
	 */
	@Override
	public void attach(TrackIncome income) {
		setNext(income);
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
		previous.moveTail(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#reverse(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void reverse(Train train) {
		previous.reverse(train);
	}

	/**
	 * @param previous
	 *            The previous to set.
	 */
	private void setNext(TrackIncome previous) {
		this.next = previous;
	}

	/**
	 * @param next
	 *            The next to set.
	 */
	public void setPrevious(CrossDeviatorIncome next) {
		this.previous = next;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		this.transit = transit;
		next.setTransit(transit);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return deviator.toString();
	}
}