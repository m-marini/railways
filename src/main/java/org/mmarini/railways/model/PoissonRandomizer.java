package org.mmarini.railways.model;

import java.io.Serializable;

/**
 * @author $Author: marco $
 * @version $Id: PoissonRandomizer.java,v 1.4.20.1 2012/02/04 19:22:56 marco Exp
 *          $
 */
public class PoissonRandomizer implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final double MIN_PROBABILITY = 10e-15;

	private double frequence;
	private double elapsed;

	/**
	 * 
	 */
	public PoissonRandomizer() {
	}

	/**
	 * @param avgTime
	 */
	public PoissonRandomizer(double avgTime) {
		this.frequence = avgTime;
	}

	/**
	 * @return Returns the avgTime.
	 */
	public double getFrequence() {
		return frequence;
	}

	/**
	 * Generate a random event.
	 * 
	 * @param time
	 *            the elapsed time since the last call
	 * @return true if an event is generated.
	 */
	public boolean getNextEvent(double time) {
		double elapsed = this.elapsed + time;
		double f = getFrequence();
		if (f <= 0)
			return false;
		double prob = 1 - Math.exp(-elapsed * getFrequence());
		if (prob <= MIN_PROBABILITY) {
			this.elapsed = elapsed;
			return false;
		}
		this.elapsed = 0;
		return Math.random() <= prob;
	}

	/**
	 * @param avgTime
	 *            The avgTime to set.
	 */
	public void setFrequence(double avgTime) {
		this.frequence = avgTime;
	}
}