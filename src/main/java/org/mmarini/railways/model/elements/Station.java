package org.mmarini.railways.model.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mmarini.railways.model.ManagerInfos;
import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.StationEvent;
import org.mmarini.railways.model.StationListener;
import org.mmarini.railways.model.ValidationException;
import org.mmarini.railways.model.routes.MovementContext;
import org.mmarini.railways.model.train.Train;
import org.mmarini.railways.model.visitor.AutoLockSetter;
import org.mmarini.railways.model.visitor.ElementWalker;
import org.mmarini.railways.model.visitor.FinderVisitor;
import org.mmarini.railways.model.visitor.InOutVisitor;
import org.mmarini.railways.model.visitor.LineListVisitor;
import org.mmarini.railways.model.visitor.LockAllVisitor;
import org.mmarini.railways.sounds.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $$Author: marco $$
 * @version $Id: Station.java,v 1.11 2012/02/08 22:03:21 marco Exp $
 */
public class Station implements RailwayConstants, Serializable {
	public static final double INCOME_CANCEL_FREQUENCE = 10. * FREQUENCE_TRAIN_SCALE;
	public static final double OUTCOME_BUILD_FREQUENCE = 0;
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(Station.class);

	private StationNode reference;
	private int direction;
	private String name;
	private List<Train> trainList;
	private List<Neighbour> neighbourList;
	private List<StationListener> listeners;
	private StationEvent event;
	private ManagerInfos managerInfos;
	private SoundPlayer soundPlayer;
	private int nextTrainId;
	private Comparator<Train> trainComparator;

	/**
	 * 
	 * 
	 */
	public Station() {
		trainList = new ArrayList<Train>();
		neighbourList = new ArrayList<Neighbour>();
		event = new StationEvent(this);
		managerInfos = new ManagerInfos();
		trainComparator = new Comparator<Train>() {

			/**
			 * 
			 * @param train0
			 * @param train1
			 * @return
			 */
			@Override
			public int compare(Train train0, Train train1) {
				boolean t0b = train0.isStop();
				boolean t1b = train1.isStop();
				if (t0b && !t1b)
					return -1;
				if (!t0b && t1b)
					return 1;
				t0b = train0.isArrived();
				t1b = train1.isArrived();
				if (t0b && !t1b)
					return -1;
				if (!t0b && t1b)
					return 1;
				t0b = train0.isBrakeing();
				t1b = train1.isBrakeing();
				if (t0b && !t1b)
					return -1;
				if (!t0b && t1b)
					return 1;
				t0b = train0.isEntering();
				t1b = train1.isEntering();
				if (t0b && !t1b)
					return 1;
				if (!t0b && t1b)
					return -1;
				double tt0 = train0.getTotalTime();
				double tt1 = train1.getTotalTime();
				if (tt0 > tt1)
					return -1;
				if (tt0 < tt1)
					return 1;
				return train0.getId() - train1.getId();
			}
		};
	}

	/**
	 * Add a neighbour to the station.
	 * 
	 * @param lineId
	 *            the line identificator where attach neighbour.
	 * @param neighbour
	 *            the neighbour.
	 * @throws ValidationException
	 *             in case the line does not exists or the identifier is not a
	 *             line.
	 */
	public void addNeighbour(String lineId, Neighbour neighbour)
			throws ValidationException {
		StationElement line = findNode(lineId);
		if (line == null) {
			throw new ValidationException("Line " + lineId + " does not exist");
		}
		if (!(line instanceof Line)) {
			throw new ValidationException("Node " + lineId + " is not a line");
		}
		((Line) line).attach(neighbour);
		neighbour.attach(this);
		neighbourList.add(neighbour);
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public synchronized void addStationListener(StationListener listener) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null) {
			listeners = new ArrayList<StationListener>(1);
		} else if (!listeners.contains(listener)) {
			listeners = new ArrayList<StationListener>(listeners);
		} else {
			return;
		}
		listeners.add(listener);
		this.listeners = listeners;
	}

	/**
	 * @param from
	 * @return
	 */
	private Neighbour createDestination(Neighbour from) {
		/*
		 * Create the outcome list
		 */
		List<Neighbour> outcome = new ArrayList<Neighbour>();
		double tot = 0;
		for (Neighbour neigh : neighbourList) {
			if (neigh.isOutcome() && neigh != from) {
				outcome.add(neigh);
			}
		}
		int n = outcome.size();
		if (n > 0) {
			double p = Math.random() * n;
			tot = 0;
			for (Neighbour dest : outcome) {
				tot += 1;
				if (p < tot)
					return dest;
			}
		}
		return null;
	}

	/**
	 * Create a train.
	 * 
	 * @param neighbour
	 *            the deposit
	 * @return the train.
	 */
	public Train createTrain(Neighbour neighbour) {
		/*
		 * Creates the train name
		 */
		String name = "TTT-" + String.valueOf(Math.round(Math.random() * 1000));
		int coachCount = (int) Math.floor(Math.random()
				* (MAX_COACH_COUNT - MIN_COACH_COUNT))
				+ MIN_COACH_COUNT;
		log.debug("Creating train ...");
		Train train = createTrain(name, coachCount);
		log.debug("Train crated " + train);

		/*
		 * Attachs the station to the train
		 */
		train.attach(this);

		/*
		 * Adds the train to the list
		 */
		trainList.add(train);

		/*
		 * Creates the destination for the train
		 */
		Neighbour dest = createDestination(neighbour);
		if (dest != null)
			train.add(dest);

		/*
		 * Records the creation on the manager infos
		 */
		managerInfos.addIncomeTrainCount();

		/*
		 * Fires train created event
		 */
		fireTrainCreated(train);
		soundPlayer.playLeopold();
		return train;
	}

	/**
	 * @param name
	 * @param coachCount
	 * @return
	 */
	private Train createTrain(String name, int coachCount) {
		Train train = new Train();
		int id = createTrainId();
		train.setId(id);
		train.setName(name);
		train.setSoundPlayer(soundPlayer);
		train.setLength(COACH_LENGTH * coachCount);

		return train;
	}

	/**
	 * 
	 * @return
	 */
	private synchronized int createTrainId() {
		return nextTrainId++;
	}

	/**
	 * Dispatches the process.
	 * 
	 * @param time
	 *            the elapsed time since the last dispatch.
	 */
	public void dispatch(double time) {
		if (isGameEnded()) {
			return;
		}
		List<Train> list = new ArrayList<Train>(trainList);
		for (Train train : list) {
			train.dispatch(time);
		}
		for (Neighbour neighbour : neighbourList) {
			neighbour.dispatch(time);
		}
		managerInfos.addTotalLifeTime(time);
	}

	/**
	 * Finds a node in the station.
	 * 
	 * @param reference
	 *            the node id.
	 * @return the node (null if not exist).
	 */
	public StationNode findNode(String reference) {
		FinderVisitor finder = new FinderVisitor(reference);
		ElementWalker visitor = new ElementWalker(finder);
		this.reference.accept(visitor);
		return finder.getFound();
	}

	/**
	 * Fires a train exited event.
	 * 
	 * @param train
	 *            the train exited.
	 */
	private void fireTrainCreated(Train train) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null)
			return;
		event.setTrain(train);
		for (StationListener iter : listeners) {
			iter.trainCreated(event);
		}
	}

	/**
	 * Fires a train exited event.
	 * 
	 * @param train
	 *            the train exited.
	 */
	private void fireTrainExited(Train train) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null)
			return;
		event.setTrain(train);
		for (StationListener iter : listeners) {
			iter.trainExited(event);
		}
	}

	/**
	 * Fires a train runned event.
	 * 
	 * @param train
	 *            the train exited.
	 */
	private void fireTrainRunned(Train train) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null)
			return;
		event.setTrain(train);
		for (StationListener iter : listeners) {
			iter.trainRunned(event);
		}
	}

	/**
	 * Fires a train stopped event.
	 * 
	 * @param train
	 *            the train exited.
	 */
	private void fireTrainStopped(Train train) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null)
			return;
		event.setTrain(train);
		for (StationListener iter : listeners) {
			iter.trainStopped(event);
		}
	}

	/**
	 * Gets the direction of the reference.
	 * 
	 * @return the direction of reference.
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * @return
	 */
	public List<Line> getLineList() {
		LineListVisitor visitor = new LineListVisitor();
		ElementWalker walker = new ElementWalker(visitor);
		reference.accept(walker);
		return visitor.getList();
	}

	/**
	 * @return Returns the managerInfo.
	 */
	public ManagerInfos getManagerInfos() {
		return managerInfos;
	}

	/**
	 * Gets the name of station.
	 * 
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the reference.
	 * 
	 * @return the reference.
	 */
	public StationNode getReference() {
		return reference;
	}

	/**
	 * 
	 * @return
	 */
	public List<Train> getSortedTrainList() {
		List<Train> list = new ArrayList<Train>(trainList);
		Collections.sort(list, trainComparator);
		return list;
	}

	/**
	 * Gets the list of trains.
	 * 
	 * @return the list of trains.
	 */
	public List<Train> getTrainList() {
		return trainList;
	}

	/**
	 * Handles the exit of a train from the station.
	 * 
	 * @param context
	 *            the movement context.
	 */
	public void handleTrainExit(MovementContext context) {
		Train train = context.getTrain();
		trainList.remove(train);
		if (train.isRightExited())
			managerInfos.addRightOutcomeTrainCount();
		else
			managerInfos.addWrongOutcomeTrainCount();
		fireTrainExited(train);
	}

	/**
	 * @param train
	 */
	public void handleTrainRun(Train train) {
		fireTrainRunned(train);
	}

	/**
	 * @param train
	 */
	public void handleTrainStop(Train train) {
		managerInfos.addTrainsStopCount();
		fireTrainStopped(train);
	}

	/**
	 * @return
	 */
	private boolean isGameEnded() {
		return managerInfos.isGameEnded();
	}

	/**
	 * 
	 * 
	 */
	public void lockAllSemaphores() {
		ElementWalker walker = new ElementWalker(new LockAllVisitor());
		reference.accept(walker);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener
	 *            the listener.
	 */
	public synchronized void removeStationListener(StationListener listener) {
		List<StationListener> listeners = this.listeners;
		if (listeners == null || !listeners.contains(listener))
			return;
		listeners = new ArrayList<StationListener>(listeners);
		listeners.remove(listener);
		this.listeners = listeners;
	}

	/**
	 * 
	 * @param autoLock
	 */
	public void setAutoLock(boolean autoLock) {
		ElementWalker walker = new ElementWalker(new AutoLockSetter(autoLock));
		reference.accept(walker);
	}

	/**
	 * Sets the direction of reference.
	 * 
	 * @param direction
	 *            the direction to set.
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * @param managerInfos
	 *            The managerInfos to set.
	 */
	public void setManagerInfos(ManagerInfos managerInfos) {
		this.managerInfos = managerInfos;
	}

	/**
	 * Sets the name of station.
	 * 
	 * @param name
	 *            the name of station.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the reference.
	 * 
	 * @param reference
	 *            the reference to set.
	 */
	public void setReference(StationNode reference) {
		this.reference = reference;
	}

	/**
	 * @param soundPlayer
	 *            the soundPlayer to set
	 */
	public void setSoundPlayer(SoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	/**
	 * 
	 * @param trainFrequence
	 */
	public void setTrainFrequence(double trainFrequence) {
		InOutVisitor visitor = new InOutVisitor();
		for (Neighbour iter : neighbourList) {
			iter.accept(visitor);
		}
		List<VirtualStation> list = visitor.getIncome();
		double freq = trainFrequence;
		int n = list.size();
		if (n > 0) {
			freq /= n;
		}
		log.debug("Setting train frequence " + trainFrequence + "tps for " + n
				+ " incomes");
		for (VirtualStation iter : list) {
			iter.setBuildFrequence(freq);
		}
	}

	/**
	 * 
	 * 
	 */
	public void stopAllTrains() {
		for (Train train : trainList) {
			if (!train.isStop() && !train.isEntering()) {
				train.brake();
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer bfr = new StringBuffer();
		bfr.append("Station ");
		bfr.append(this.name);
		bfr.append("(direction=");
		bfr.append(this.direction);
		bfr.append(",reference=");
		bfr.append(this.reference);
		bfr.append(")");
		return bfr.toString();
	}
}