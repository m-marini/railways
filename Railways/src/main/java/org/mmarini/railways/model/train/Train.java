package org.mmarini.railways.model.train;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.elements.Neighbour;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.routes.NeighbourIncome;
import org.mmarini.railways.model.routes.RoutePoint;
import org.mmarini.railways.model.routes.TrainBuilder;
import org.mmarini.railways.model.routes.TrainHead;
import org.mmarini.railways.model.visitor.ElementFinderByRoute;
import org.mmarini.railways.model.visitor.ElementWalker;
import org.mmarini.railways.sounds.SoundPlayer;

/**
 * @author $Author: marco $
 * @version $Id: Train.java,v 1.9 2012/02/08 22:03:33 marco Exp $
 */
public class Train implements RailwayConstants, Serializable {
	public static final double LOADING_TIME = 10.;
	private static final long serialVersionUID = 1L;

	private String name;
	private double length;
	private TrainHead head;
	private double speed;
	private Station station;
	private boolean rightExited;
	private List<Neighbour> route;
	private TrainHead builder;
	private double entryTimer;
	private boolean arrived;
	private TrainState state;
	private double time;
	private SoundPlayer soundPlayer;
	private double loadTimeout;
	private int id;
	private double totalTime;

	/**
	 * 
	 * 
	 */
	public Train() {
		route = new ArrayList<Neighbour>();
		state = TrainState.ENTERING_STATE;
	}

	/**
	 * Add a neighbour to the route list.
	 * 
	 * @param neighbour
	 *            the neighbour.
	 */
	public void add(Neighbour neighbour) {
		route.add(neighbour);
	}

	/**
	 * Attaches the station to the train.
	 * 
	 * @param station
	 *            the station.
	 */
	public void attach(Station station) {
		this.station = station;
	}

	/**
	 * 
	 * 
	 */
	public void brake() {
		soundPlayer.playBraking();
		changeState(TrainState.BRAKEING_STATE);
	}

	/**
	 * @param state2
	 */
	public void changeState(TrainState state2) {
		state = state2;
	}

	/**
	 * Calculate the train movement.
	 * 
	 * @param time
	 *            the elapsed time.
	 * @return the train movement.
	 */
	public double computeMovement(double time) {
		/*
		 * Calculates the speed
		 */
		return speed * time;
	}

	/**
	 * @return
	 */
	public double computeStopDistance() {
		/*
		 * a = v/t => t = v/a s = 1/2 a t^2 = 1/2 a v^2/a^2 = 1/2 v^2/a
		 */
		return speed * speed / DEACCELERATION * -0.5;
	}

	/**
	 * @param time
	 */
	public void dispatch(double time) {
		this.time = time;
		totalTime += time;
		state.handleDispatch(this);
	}

	/**
	 * @return Returns the income.
	 */
	public TrainHead getBuilder() {
		return builder;
	}

	/**
	 * @return
	 */
	public Neighbour getDestination() {
		Neighbour neigh;
		List<Neighbour> list = route;
		if (list.isEmpty())
			neigh = null;
		else
			neigh = list.get(0);
		return neigh;
	}

	/**
	 * @return
	 */
	public StationElement getElementLocation() {
		ElementFinderByRoute finder = new ElementFinderByRoute();
		ElementWalker walker = new ElementWalker(finder);
		Station station = this.station;
		TrainHead head = this.head;
		if (head == null) {
			head = builder;
			if (head != null)
				head = ((TrainBuilder) head).getNext();
		}
		if (head == null)
			return null;
		finder.setHead(head);
		station.getReference().accept(walker);
		StationElement element = finder.getFound();
		return element;
	}

	/**
	 * @return Returns the entryTimer.
	 */
	double getEntryTimer() {
		return entryTimer;
	}

	/**
	 * @return Returns the head.
	 */
	public TrainHead getHead() {
		return head;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Returns the length.
	 */
	public double getLength() {
		return length;
	}

	/**
	 * 
	 * @return
	 */
	public double getLoadTimeout() {
		return loadTimeout;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public RoutePoint getRouteLocation() {
		TrainHead head = this.head;
		if (head == null)
			return null;
		return head.getTrainLocation();
	}

	/**
	 * @return Returns the speed.
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @return
	 */
	public String getStateId() {
		return state.getStateId();
	}

	/**
	 * @return Returns the station.
	 */
	public Station getStation() {
		return station;
	}

	/**
	 * @return Returns the time.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return the totalTime
	 */
	public double getTotalTime() {
		return totalTime;
	}

	/**
	 * 
	 * 
	 */
	public void handleSemaphoreBusy() {
		state.handleSemaphoreBusy(this);
	}

	/**
	 * @return Returns the arrived.
	 */
	public boolean isArrived() {
		return arrived;
	}

	/**
	 * @return
	 */
	public boolean isBrakeing() {
		return state.equals(TrainState.BRAKEING_STATE)
				|| state.equals(TrainState.WAIT_FOR_RUN_STATE);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEntering() {
		return entryTimer > 0;
	}

	/**
	 * @return
	 */
	public boolean isRightExited() {
		return rightExited;
	}

	/**
	 * @return
	 */
	public boolean isStop() {
		return speed == 0;
	}

	/**
	 * 
	 * 
	 */
	public void load() {
		loadTimeout = 0.;
		changeState(TrainState.WAIT_FOR_LOADED_STATE);
	}

	/**
	 * 
	 */
	public void playBraking() {
		soundPlayer.playBraking();
	}

	/**
	 * 
	 */
	public void playStopped() {
		soundPlayer.playStopped();
	}

	/**
	 * 
	 * 
	 */
	public void reverse() {
		head.reverse(this);
		soundPlayer.playLeaving();
	}

	/**
	 * 
	 */
	public void run() {
		changeState(TrainState.RUNNING_FAST_STATE);
		if (speed > 0)
			return;
		station.handleTrainRun(this);
		soundPlayer.playLeaving();
	}

	/**
	 * @param arrived
	 *            The arrived to set.
	 */
	public void setArrived(boolean arrived) {
		this.arrived = arrived;
	}

	/**
	 * @param builder
	 *            The income to set.
	 */
	public void setBuilder(TrainHead builder) {
		this.builder = builder;
		entryTimer = ENTRY_TIMEOUT;
	}

	/**
	 * @param entryTimer
	 *            The entryTimer to set.
	 */
	public void setEntryTimer(double entryTimer) {
		this.entryTimer = entryTimer;
	}

	/**
	 * @param head
	 *            The head to set.
	 */
	public void setHead(TrainHead head) {
		TrainHead oldHead = this.head;
		this.head = head;
		if (head != null)
			return;
		Neighbour neigh = getDestination();
		if (neigh == null)
			return;
		NeighbourIncome in = neigh.getJunction().getIncome();
		this.rightExited = isArrived() && in.equals(oldHead);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param length
	 */
	public void setLength(double length) {
		this.length = length;
	}

	/**
	 * 
	 * @param loadTimeout
	 */
	public void setLoadTimeout(double loadTimeout) {
		this.loadTimeout = loadTimeout;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param soundPlayer
	 *            the soundPlayer to set
	 */
	public void setSoundPlayer(SoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	/**
	 * @param speed
	 *            The speed to set.
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * 
	 */
	public void stop() {
		changeState(TrainState.WAIT_FOR_RUN_STATE);
		if (speed > 0)
			station.handleTrainStop(this);
		speed = 0;
		soundPlayer.playArrived();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(name);
	}
}