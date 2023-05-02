package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: PlatformRoute.java,v 1.7 2012/02/08 22:03:19 marco Exp $
 */
public class PlatformRoute extends SegmentRoute {
	private static final long serialVersionUID = 1L;

	/**
	 * @param platform
	 */
	public PlatformRoute(Platform platform) {
		super(platform);
	}

	/**
	 * @param platform
	 * @param reverse
	 */
	public PlatformRoute(Platform platform, boolean reverse) {
		super(platform, reverse);
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
		if (max < movement) {
			movement = max;
		} else if (train.isArrived()) {
			/*
			 * Train is passing over: add the next route movement
			 */
			MovementContext nextCtx = new MovementContext(context);
			nextCtx.setMovement(nextCtx.getMovement() - movement);
			getNext().calculateMovement(nextCtx);
			movement += nextCtx.getMovement();
		}
		context.setMovement(movement);
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#hasToStop(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public boolean hasToStop(MovementContext ctx) {
		double movement = ctx.getMovement();
		Train train = ctx.getTrain();
		if (train == getTrain()) {
			movement += getTransited();
		}
		if (movement >= getLength()) {
			if (!ctx.getTrain().isArrived())
				return true;
			ctx.setMovement(movement - getLength());
			return getNext().hasToStop(ctx);
		}
		return false;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainHead#moveHead(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void moveHead(MovementContext context) {
		checkForError(context);
		Train train = getTrain();
		if (train == null) {
			/*
			 * Register the incoming train
			 */
			handleTrainEntry(context);
			train = getTrain();
		}
		double movement = context.getMovement();
		double newTransited = movement + getTransited();
		double nextMovement = newTransited - getLength();
		if (nextMovement < 0) {
			/*
			 * Transito su binario locale
			 */
			setTransited(newTransited);
			context.setTransited(newTransited);
			getPrevious().moveTail(context);
		} else if (train.isArrived()) {
			context.setMovement(nextMovement);
			getNext().moveHead(context);
		} else {
			setTransited(newTransited);
			context.setTransited(newTransited);
			train.stop();
			getPrevious().moveTail(context);
		}
		if (train.getSpeed() == 0 && getTransited() >= train.getLength()
				&& !train.isArrived()) {
			train.load();
		}
	}
}