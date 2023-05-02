package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: PointRoute.java,v 1.5 2012/02/08 22:03:18 marco Exp $
 */
public class PointRoute implements NodeIncome, NodeOutcome {
	private TrackOutcome previous;
	private TrackIncome next;
	private StationElement element;

	/**
	 * @param element
	 */
	public PointRoute(StationElement element) {
		this.element = element;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#attach(org.mmarini.railways.model.routes.TrackIncome)
	 */
	@Override
	public void attach(TrackIncome income) {
		setNext(income);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeIncome#attach(org.mmarini.railways.model.routes.TrackOutcome)
	 */
	@Override
	public void attach(TrackOutcome outcome) {
		setPrevious(outcome);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
		getNext().calculateMovement(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeOutcome#calculateNextLocation(double)
	 */
	@Override
	public RoutePoint calculateNextLocation(double distance) {
		return getPrevious().calculateNextLocation(distance);
	}

	/**
	 * @return Returns the element.
	 */
	private StationElement getElement() {
		return element;
	}

	/**
	 * @return Returns the next.
	 */
	public TrackIncome getNext() {
		return next;
	}

	/**
	 * @return Returns the previous.
	 */
	public TrackOutcome getPrevious() {
		return previous;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return getPrevious().getTrainLocation();
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		return getNext().hasToStop(ctx);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#isBusy()
	 */
	@Override
	public boolean isBusy() {
		return false;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitableHandler#isTransitable()
	 */
	@Override
	public boolean isTransitable() {
		return getNext().isTransitable();
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		getNext().moveHead(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#moveTail(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveTail(MovementContext context) {
		getPrevious().moveTail(context);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainTail#reverse(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void reverse(Train train) {
		getPrevious().reverse(train);
	}

	/**
	 * @param next
	 *            The next to set.
	 */
	private void setNext(TrackIncome next) {
		this.next = next;
	}

	/**
	 * @param previous
	 *            The previous to set.
	 */
	private void setPrevious(TrackOutcome previous) {
		this.previous = previous;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		getNext().setTransit(transit);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getElement().toString();
	}
}