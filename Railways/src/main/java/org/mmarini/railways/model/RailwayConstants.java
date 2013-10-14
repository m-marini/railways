package org.mmarini.railways.model;

import org.mmarini.railways.model.xml.XmlConstants;

/**
 * @author $Author: marco $
 * @version $Id: RailwayConstants.java,v 1.1.4.1.2.1 2005/08/24 17:07:10 marco
 *          Exp $
 */
public interface RailwayConstants extends XmlConstants {
	/**
	 * Seconds per minute
	 */
	public static final double SPM = 60.;

	/**
	 * Seconds per hours
	 */
	public static final double SPH = 3600.;

	/**
	 * Constant to covert the MIN_WAIT_TRAIN time in SEC
	 */
	public static final double MIN_WAIT_TRAIN_SCALE = SPM;

	/**
	 * Constant to covert the FREQUENCE_TRAIN time in train/sec.
	 */
	public static final double FREQUENCE_TRAIN_SCALE = 1 / SPH;

	/**
	 * Constant to convert the KMH (Km/hour) to MS (meters/sec)
	 */
	public static final double KMH = 1 / 3.6;

	/**
	 * Time to wait before enter in station
	 */
	public static final double ENTRY_TIMEOUT = 40.;

	/**
	 * Maximum speed 140 Kmh
	 */
	public static final double MAX_SPEED = 140. * KMH;

	/**
	 * The acceleration (meters / sec ^ 2) 400kN / 642t = 0.623
	 */
	public static final double ACCELERATION = 400. / 642.;

	/**
	 * The deacceleration (meters / sec ^ 2) = 100./2/3.6/3.6/2 = 1.929
	 */
	public static final double DEACCELERATION = -100. / 2 / 3.6 / 3.6 / 2;

	/**
	 * Coach length
	 */
	public static final double COACH_LENGTH = 25;

	/**
	 * Coach width
	 */
	public static final double COACH_WIDTH = 3;

	/**
	 * Minimun number of coach per train.
	 */
	public static final int MIN_COACH_COUNT = 3;

	/**
	 * Maximum number od coach per train.
	 */
	public static final int MAX_COACH_COUNT = 14;

	/**
	 * Length units of standars segment element (meters)
	 */
	public static final double SEGMENT_LENGTH = 35;

	/**
	 * Numbers of curve elements per circle angle 360 / 24 = 15 DEG
	 */
	public static final int CURVES_PER_CIRCLE = 24;

	/**
	 * Rads per curve unit (rads)
	 */
	public static final double CURVE_RADS = 2 * Math.PI / CURVES_PER_CIRCLE;

	/**
	 * Rads per curve unit (rads)
	 */
	public static final double CURVE_DEGS = 360. / CURVES_PER_CIRCLE;

	/**
	 * Default angle for cross element (degrees)
	 */
	public static final double DEFAULT_CROSS_ANGLE = 2 * Math
			.toDegrees(CURVE_RADS);

	/**
	 * Radius of standard curve element (meters)
	 */
	public static final double RADIUS = SEGMENT_LENGTH * 0.5
			/ Math.sin(CURVE_RADS);

	/**
	 * Gap between tracks (meters)
	 */
	public static final double TRACK_GAP = SEGMENT_LENGTH
			* (1 - Math.cos(CURVE_RADS)) / Math.sin(CURVE_RADS);

	/**
	 * Length of standard curve element (meters)
	 */
	public static final double CURVE_LENGTH = RADIUS * CURVE_RADS;
}