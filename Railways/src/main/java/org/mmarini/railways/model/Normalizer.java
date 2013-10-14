package org.mmarini.railways.model;

/**
 * @author $$Author: marco $$
 * @version $Id: Normalizer.java,v 1.2 2005/10/17 22:37:17 marco Exp $
 */
public class Normalizer {

	/**
	 * @param direction
	 * @return
	 */
	public static double normalize(double direction) {
		while (direction > 360)
			direction -= 360;
		while (direction <= 0)
			direction += 360;
		return direction;
	}

}