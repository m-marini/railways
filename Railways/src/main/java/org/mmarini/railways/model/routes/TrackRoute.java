package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.Track;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: TrackRoute.java,v 1.5 2012/02/08 22:03:18 marco Exp $
 */
public abstract class TrackRoute extends AbstractRoute implements TrackOutcome,
		TrackIncome, RoutePath {
	private NodeOutcome previous;
	private NodeIncome next;
	private TrackRoute opposite;
	private boolean reverse;

	/**
	 * @param length
	 */
	public TrackRoute(Track track) {
		super(track);
	}

	/**
	 * @param track
	 * @param reverse
	 */
	public TrackRoute(Track track, boolean reverse) {
		super(track);
		this.reverse = reverse;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrackOutcome#attach(org.mmarini.railways.model.routes.NodeIncome)
	 */
	@Override
	public void attach(NodeIncome income) {
		this.next = income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrackIncome#attach(org.mmarini.railways.model.routes.NodeOutcome)
	 */
	@Override
	public void attach(NodeOutcome outcome) {
		this.previous = outcome;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#calculateMovement(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void calculateMovement(MovementContext context) {
		double max = context.getMovement();
		Train train = context.getTrain();
		/*
		 * Calculates the segment movement
		 */
		double movement = getLength();
		if (train == getTrain()) {
			/*
			 * Train is transiting: subtract the transited movement
			 */
			movement -= getTransited();
		}
		if (max >= movement) {
			/*
			 * Train is passing over: add the next route movement
			 */
			MovementContext nextCtx = new MovementContext(context);
			nextCtx.setMovement(nextCtx.getMovement() - movement);
			next.calculateMovement(nextCtx);
			movement += nextCtx.getMovement();
		} else
			movement = max;
		context.setMovement(movement);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrackOutcome#calculateNextLocation(double)
	 */
	@Override
	public RoutePoint calculateNextLocation(double distance) {
		return calculateNextLocation(new RoutePoint(this, getLength()),
				distance);
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
			return new RoutePoint(this, newLoc);
		distance -= loc;
		return getPrevious().calculateNextLocation(distance);
	}

	/**
	 * @return Returns the length.
	 */
	public double getLength() {
		return getTrack().getLength();
	}

	/**
	 * Gets the next node.
	 * 
	 * @return the next node.
	 */
	public NodeIncome getNext() {
		return next;
	}

	/**
	 * @return Returns the opposite.
	 */
	public TrackRoute getOpposite() {
		return opposite;
	}

	/**
	 * @return Returns the prev.
	 */
	public NodeOutcome getPrevious() {
		return previous;
	}

	/**
	 * @return Returns the track.
	 */
	public Track getTrack() {
		return (Track) getElement();
	}

	/**
	 * Gets the train head position.
	 * 
	 * @return the train head position.
	 */
	public double getTrainHead() {
		if (getTrain() == null) {
			return -1;
		}
		double head;
		head = Math.min(getTransited(), getLength());
		return head;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#getTrainLocation()
	 */
	@Override
	public RoutePoint getTrainLocation() {
		return new RoutePoint(this, getTransited());
	}

	/**
	 * Gets the train tail position.
	 * 
	 * @return the train tail position.
	 */
	public double getTrainTail() {
		if (getTrain() == null) {
			return -1;
		}
		double tail;
		tail = Math.max(0,
				Math.min(getTransited() - getTrain().getLength(), getLength()));
		return tail;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		double movement = ctx.getMovement();
		Train train = ctx.getTrain();
		if (train == getTrain()) {
			movement += getTransited();
		}
		if (movement < getLength())
			return false;
		ctx.setMovement(movement - getLength());
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
	 * @return Returns the reverse.
	 */
	protected boolean isReverse() {
		return reverse;
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
		Train currentTrain = getTrain();
		if (currentTrain == null) {
			/*
			 * Register the incoming train
			 */
			handleTrainEntry(context);
		}
		double movement = context.getMovement();
		double newTransited = movement + getTransited();
		double nextMovement = newTransited - getLength();
		if (nextMovement >= 0) {
			context.setMovement(nextMovement);
			next.moveHead(context);
		} else {
			setTransited(newTransited);
			context.setTransited(newTransited);
			getPrevious().moveTail(context);
		}
	}

	/**
	 * @param context
	 */
	@Override
	public void moveTail(MovementContext context) {
		Train train = context.getTrain();
		if (getTrain() != train)
			return;
		double transited = context.getTransited() + getLength();
		context.setTransited(transited);
		setTransited(transited);
		getPrevious().moveTail(context);
		if (transited >= getLength() + train.getLength()) {
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
		double transited = train.getLength() + getLength() - getTransited();
		if (transited >= 0) {
			TrackRoute opposite = getOpposite();
			opposite.setTrainLocation(train, transited);
			if (transited <= getLength())
				train.setHead(opposite);
			if (getTransited() < train.getLength())
				getPrevious().reverse(train);
		}
	}

	/**
	 * @param opposite
	 *            The opposite to set.
	 */
	public void setOpposite(TrackRoute opposite) {
		this.opposite = opposite;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TransitHandler#setTransit(boolean)
	 */
	@Override
	public void setTransit(boolean transit) {
		next.setTransit(transit);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getTrack().toString();
	}
}