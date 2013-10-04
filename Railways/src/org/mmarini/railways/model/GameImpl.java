package org.mmarini.railways.model;

import org.mmarini.railways.model.elements.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $Author: marco $
 * @version $Id: GameImpl.java,v 1.4 2012/02/08 22:03:18 marco Exp $
 */
public class GameImpl implements RailwayConstants, Game {
	private static final double DEFAULT_TIME_SPEED = 1e-3;

	private static Logger log = LoggerFactory.getLogger(GameImpl.class);

	private Station station;
	private ManagerInfos managerInfos;
	private long lastTick;
	private double timeSpeed;

	/**
	 * 
	 */
	public GameImpl() {
		managerInfos = new ManagerInfos();
		timeSpeed = DEFAULT_TIME_SPEED;
		lastTick = System.currentTimeMillis();
	}

	/**
	 * @return
	 */
	private long getElapsedTime() {
		long now = System.currentTimeMillis();
		long time = now - lastTick;
		this.lastTick = now;
		return time;
	}

	/**
	 * @see org.mmarini.railways.model.Game#getManagerInfos()
	 */
	@Override
	public ManagerInfos getManagerInfos() {
		return managerInfos;
	}

	/**
	 * @see org.mmarini.railways.model.Game#getStation()
	 */
	@Override
	public Station getStation() {
		return station;
	}

	/**
	 * @see org.mmarini.railways.model.Game#handleTimer()
	 */
	@Override
	public void handleTimer() {
		if (isGameEnded())
			return;
		long time = getElapsedTime();
		if (timeSpeed > 0) {
			station.dispatch(time * timeSpeed);
		}
	}

	/**
	 * @see org.mmarini.railways.model.Game#isGameEnded()
	 */
	@Override
	public boolean isGameEnded() {
		return managerInfos.isGameEnded();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.Game#lockAllSemaphores()
	 */
	@Override
	public void lockAllSemaphores() {
		station.lockAllSemaphores();
	}

	/**
	 * @see org.mmarini.railways.model.Game#reset()
	 */
	@Override
	public void reset() {
		log.debug("reset");
		long now = System.currentTimeMillis();
		this.lastTick = now;
		managerInfos.reset();
	}

	/**
	 * @param managerInfos
	 *            The managerInfos to set.
	 */
	public void setManagerInfos(ManagerInfos managerInfos) {
		this.managerInfos = managerInfos;
		if (station != null)
			station.setManagerInfos(managerInfos);
	}

	/**
	 * @param station
	 *            The station to set.
	 */
	public void setStation(Station station) {
		this.station = station;
		if (station != null)
			station.setManagerInfos(managerInfos);
	}

	/**
	 * @see org.mmarini.railways.model.Game#setTimeSpeed(double)
	 */
	@Override
	public void setTimeSpeed(double timeSpeed) {
		this.timeSpeed = timeSpeed;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.Game#stopAllTrains()
	 */
	@Override
	public void stopAllTrains() {
		station.stopAllTrains();
	}
}