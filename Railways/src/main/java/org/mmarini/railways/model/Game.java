package org.mmarini.railways.model;

import org.mmarini.railways.model.elements.Station;

/**
 * 
 * @author US00852
 * @version $Id: Game.java,v 1.4 2012/02/08 22:03:18 marco Exp $
 */
public interface Game {

	/**
	 * @see org.mmarini.railways.model.GameHandler#getManagerInfos()
	 */
	public abstract ManagerInfos getManagerInfos();

	/**
	 * @see org.mmarini.railways.model.GameHandler#getStation()
	 */
	public abstract Station getStation();

	/**
	 * @see org.mmarini.railways.model.GameHandler#handleTimer()
	 */
	public abstract void handleTimer();

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#isGameEnded()
	 */
	public abstract boolean isGameEnded();

	/**
	 * 
	 * 
	 */
	public abstract void lockAllSemaphores();

	/**
	 * 
	 * 
	 */
	public abstract void reset();

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setTimeSpeed(double)
	 */
	public abstract void setTimeSpeed(double timeSpeed);

	/**
	 * 
	 * 
	 */
	public abstract void stopAllTrains();

}