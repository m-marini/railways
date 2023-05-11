package org.mmarini.railways.model.routes;

import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractSingleNodeRoute.java,v 1.5 2012/02/08 22:03:18 marco Exp $
 */
public abstract class AbstractRoute extends AbstractPointRoute {
	private double transited;

	/**
	 * @param element
	 */
	public AbstractRoute(StationElement element) {
		super(element);
	}

	/**
	 * @return Returns the transited.
	 */
	public double getTransited() {
		return transited;
	}

	/**
	 * @param context
	 */
	@Override
	protected void handleTrainEntry(MovementContext context) {
		super.handleTrainEntry(context);
		setTransited(0);
	}

	/**
	 * @param train
	 * @param transited
	 */
	public void setTrainLocation(Train train, double transited) {
		setTrain(train);
		setTransited(transited);
	}

	/**
	 * @param transited
	 *            The transited to set.
	 */
	protected void setTransited(double transited) {
		this.transited = transited;
	}

	/**
	 * @param context
	 */
	protected void updateTransited(MovementContext context) {
		setTransited(context.getTransited());
	}
}