package org.mmarini.railways.model;

import java.util.EventListener;

/**
 * @author $Author: marco $
 * @version $Id: StationListener.java,v 1.5 2012/02/08 22:03:18 marco Exp $
 */
public interface StationListener extends EventListener {

	/**
	 * 
	 * @param event
	 */
	public abstract void gameEnded(StationEvent event);

	/**
	 * @param event
	 */
	public abstract void trainCreated(StationEvent event);

	/**
	 * @param event
	 */
	public abstract void trainExited(StationEvent event);

	/**
	 * @param event
	 */
	public abstract void trainRunned(StationEvent event);

	/**
	 * @param event
	 */
	public abstract void trainStopped(StationEvent event);
}