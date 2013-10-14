package org.mmarini.railways.model;

/**
 * 
 * @author us00852
 * @version $Id: GameParameters.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface GameParameters {

	/**
	 * 
	 * @return
	 */
	public abstract double getGameLength();

	/**
	 * 
	 * @return
	 */
	public abstract String getStationName();

	/**
	 * 
	 * @return
	 */
	public abstract double getTrainFrequence();
}
