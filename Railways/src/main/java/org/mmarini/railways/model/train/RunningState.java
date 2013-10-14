package org.mmarini.railways.model.train;

import org.mmarini.railways.model.ManagerInfos;
import org.mmarini.railways.model.routes.MovementContext;
import org.mmarini.railways.model.routes.TrainHead;

/**
 * @author $Author: marco $
 * @version $Id: RunningState.java,v 1.8 2012/02/08 22:03:32 marco Exp $
 */
public class RunningState extends TrainStateAdapter {
	private double speed;

	/**
	 * @param speed
	 */
	protected RunningState(double speed) {
		this.speed = speed;
	}

	/**
	 * @return Returns the speed.
	 */
	protected double getSpeed() {
		return speed;
	}

	/**
	 * @see org.mmarini.railways.model.train.TrainStateAdapter#handleBrake(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void handleBrake(Train ctx) {
		changeState(ctx, BRAKEING_STATE);
	}

	/**
	 * @see org.mmarini.railways.model.train.AbstractTrainState#handleDispatch(org.mmarini.railways.model.train.TrainStateContext)
	 */
	@Override
	public void handleDispatch(Train train) {
		double time = train.getTime();
		ManagerInfos infos = train.getStation().getManagerInfos();
		infos.addTrainsLifeTime(time);
		double speed = train.getSpeed();
		double speedValue = getSpeed();
		double acceleration = (speedValue - speed) / time;
		if (acceleration > ACCELERATION)
			acceleration = ACCELERATION;
		else if (acceleration < DEACCELERATION)
			acceleration = DEACCELERATION;
		speed += acceleration * time;
		if (speed > MAX_SPEED)
			speed = MAX_SPEED;
		train.setSpeed(speed);

		double movement = train.computeMovement(time);
		MovementContext context = new MovementContext(train, movement, time);
		TrainHead head = train.getHead();
		head.calculateMovement(context);
		double movementDiff = movement - context.getMovement();
		movement = context.getMovement();
		head.moveHead(context);
		infos.addTrainsDistance(movement);
		speed = train.getSpeed();
		if (speed != 0 && movementDiff > 0) {
			infos.addTrainsWaitTime(movementDiff / speed);
		}
	}

	/**
	 * @see org.mmarini.railways.model.train.TrainStateAdapter#handleRun(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void handleRun(Train ctx) {
		changeState(ctx, RUNNING_FAST_STATE);
	}

	/**
	 * @see org.mmarini.railways.model.train.TrainStateAdapter#handleSemaphoreBusy(org.mmarini.railways.model.train.Train)
	 */
	@Override
	public void handleSemaphoreBusy(Train ctx) {
		ctx.setSpeed(0);
		changeState(ctx, WAIT_FOR_SEM_STATE);
		ctx.playStopped();
	}
}
