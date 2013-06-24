package org.mmarini.railways.model.routes;

import java.io.Serializable;

import org.mmarini.railways.model.train.Train;

/**
 * @author marco
 */
public class MovementContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private Train train;
	private double movement;
	private double time;
	private double transited;

	/**
	 * @param context
	 */
	public MovementContext(MovementContext context) {
		this(context.getTrain(), context.getMovement(), context.getTime());
	}

	/**
	 * @param time
	 * @param movement
	 * @param train
	 */
	public MovementContext(Train train, double movement, double time) {
		this.train = train;
		this.movement = movement;
		this.time = time;
	}

	/**
	 * @return Returns the movement.
	 */
	public double getMovement() {
		return movement;
	}

	/**
	 * @return Returns the time.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return Returns the train.
	 */
	public Train getTrain() {
		return train;
	}

	/**
	 * @return Returns the transited.
	 */
	public double getTransited() {
		return transited;
	}

	/**
	 * @param movement
	 *            The movement to set.
	 */
	public void setMovement(double movement) {
		this.movement = movement;
	}

	/**
	 * @param train
	 *            The train to set.
	 */
	public void setTrain(Train train) {
		this.train = train;
	}

	/**
	 * @param transited
	 *            The transited to set.
	 */
	public void setTransited(double transited) {
		this.transited = transited;
	}
}