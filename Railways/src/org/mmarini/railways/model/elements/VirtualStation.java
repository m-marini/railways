package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.PoissonRandomizer;
import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.routes.MovementContext;
import org.mmarini.railways.model.routes.NeighbourJunctionImpl;
import org.mmarini.railways.model.routes.TrainBuilder;
import org.mmarini.railways.model.routes.TrainCanceller;
import org.mmarini.railways.model.routes.TrainListener;
import org.mmarini.railways.model.train.Train;
import org.mmarini.railways.model.visitor.NeighbourVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: VirtualStation.java,v 1.7 2012/02/08 22:03:21 marco Exp $
 */
public class VirtualStation extends AbstractNeighbour implements
		RailwayConstants, TrainListener, Serializable {
	private static final long serialVersionUID = 1L;
	private TrainCanceller canceller;
	private TrainBuilder builder;
	private Station station;
	private PoissonRandomizer buildRandomizer = new PoissonRandomizer();
	private PoissonRandomizer cancelRandomizer = new PoissonRandomizer();
	private Line line;
	private boolean trainWaiting;
	private boolean started;

	/**
	 * 
	 */
	public VirtualStation() {
		canceller = new TrainCanceller();
		canceller.addTrainListener(this);
		builder = new TrainBuilder();
		builder.setOpposite(canceller);
		canceller.setOpposite(builder);
		NeighbourJunctionImpl junction = new NeighbourJunctionImpl(canceller,
				builder);
		setJunction(junction);
	}

	/**
	 * @see org.mmarini.railways.model.elements.Neighbour#accept(org.mmarini.railways.model.visitor.NeighbourVisitor)
	 */
	@Override
	public void accept(NeighbourVisitor visitor) {
		visitor.visitVirtualStation(this);
	}

	/**
	 * @see org.mmarini.railways.model.elements.AbstractNeighbour#attach(org.mmarini.railways.model.elements.Station)
	 */
	@Override
	public void attach(Station station) {
		setStation(station);
	}

	/**
	 * @see org.mmarini.railways.model.elements.Neighbour#attachLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void attachLine(Line line) {
		setLine(line);
	}

	/**
	 * @see org.mmarini.railways.model.elements.AbstractNeighbour#dispatch(double)
	 */
	@Override
	public void dispatch(double time) {
		boolean isTrainReady;
		if (!started && !isOutcome()) {
			isTrainReady = true;
			started = true;
		} else {
			isTrainReady = isTrainReady(time);
		}
		if (isTrainReady) {
			Station station = getStation();
			Train train = station.createTrain(this);
			train.setBuilder(getBuilder());
			train.setSpeed(MAX_SPEED);
		}
	}

	/**
	 * @return Returns the builder.
	 */
	private TrainBuilder getBuilder() {
		return builder;
	}

	/**
	 * @return
	 */
	public double getBuildFrequence() {
		return getBuildRandomizer().getFrequence();
	}

	/**
	 * @return Returns the builderRandomizer.
	 */
	private PoissonRandomizer getBuildRandomizer() {
		return buildRandomizer;
	}

	/**
	 * @return
	 */
	public double getCancelFrequence() {
		return getCancelRandomizer().getFrequence();
	}

	/**
	 * @return Returns the canceller.
	 */
	private TrainCanceller getCanceller() {
		return canceller;
	}

	/**
	 * @return Returns the cancellRandomizer.
	 */
	private PoissonRandomizer getCancelRandomizer() {
		return cancelRandomizer;
	}

	/**
	 * @return Returns the line.
	 */
	public Line getLine() {
		return line;
	}

	/**
	 * @return Returns the station.
	 */
	private Station getStation() {
		return station;
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainListener#handleTrainEntry(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void handleTrainEntry(MovementContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.routes.TrainListener#handleTrainExit(org.mmarini.railways.model.routes.MovementContext)
	 */
	@Override
	public void handleTrainExit(MovementContext context) {
		if (!isTrainWaiting()) {
			setTrainWaiting(true);
		}
		// PoissonRandomizer rnd = getCancelRandomizer();
		// if (rnd.getNextEvent(context.getTime())) {
		getCanceller().cancellTrain();
		getStation().handleTrainExit(context);
		setTrainWaiting(false);
		// }

	}

	/**
	 * Return true if a train is ready.
	 * 
	 * @param time
	 *            the elapsed time.
	 * @return true if a train is ready.
	 */
	private boolean isTrainReady(double time) {
		return getBuildRandomizer().getNextEvent(time);
	}

	/**
	 * @return Returns the waitingTrain.
	 */
	private boolean isTrainWaiting() {
		return trainWaiting;
	}

	/**
	 * @param frequence
	 */
	public void setBuildFrequence(double frequence) {
		getBuildRandomizer().setFrequence(frequence);
	}

	/**
	 * @param frequence
	 */
	public void setCancelFrequence(double frequence) {
		getCancelRandomizer().setFrequence(frequence);
	}

	/**
	 * @param line
	 *            The line to set.
	 */
	private void setLine(Line line) {
		this.line = line;
	}

	/**
	 * @param station
	 *            The station to set.
	 */
	private void setStation(Station station) {
		this.station = station;
	}

	/**
	 * @param trainWaiting
	 *            The trainWaiting to set.
	 */
	private void setTrainWaiting(boolean trainWaiting) {
		this.trainWaiting = trainWaiting;
	}
}