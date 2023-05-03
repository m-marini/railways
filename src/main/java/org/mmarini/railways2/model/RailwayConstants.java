package org.mmarini.railways2.model;

import org.mmarini.railways.model.xml.XmlConstants;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * @author $Author: marco $
 * @version $Id: RailwayConstants.java,v 1.1.4.1.2.1 2005/08/24 17:07:10 marco
 * Exp $
 */
public interface RailwayConstants extends XmlConstants {
    /**
     * Seconds per minute
     */
    double SPM = 60.;

    /**
     * Seconds per hours
     */
    double SPH = 3600.;

    /**
     * Constant to covert the MIN_WAIT_TRAIN time in SEC
     */
    double MIN_WAIT_TRAIN_SCALE = SPM;

    /**
     * Constant to covert the FREQUENCE_TRAIN time in train/sec.
     */
    double FREQUENCE_TRAIN_SCALE = 1 / SPH;

    /**
     * Exit distance (m)
     */
    double EXIT_DISTANCE = 3000;
    /**
     * Constant to convert the KMH (Km/hour) to MS (meters/sec)
     */
    double KMH = 1 / 3.6;

    /**
     * Time to wait before enter in station
     */
    double ENTRY_TIMEOUT = 40.;

    /**
     * Time to wait for load 60 s
     */
    double LOADING_TIME = 60.;

    /**
     * Maximum speed 140 Kmh
     */
    double MAX_SPEED = 140. * KMH;

    /**
     * The acceleration (meters / sec ^ 2) 400kN / 642t = 0.623
     */
    double ACCELERATION = 400. / 642.;

    /**
     * The deacceleration (meters / sec ^ 2) = 100./2/3.6/3.6/2 = 1.929
     */
    double DEACCELERATION = -100. / 2 / 3.6 / 3.6 / 2;

    /**
     * Coach length
     */
    double COACH_LENGTH = 25;

    /**
     * Coach width
     */
    double COACH_WIDTH = 3;

    /**
     * Minimun number of coach per train.
     */
    int MIN_COACH_COUNT = 3;

    /**
     * Maximum number od coach per train.
     */
    int MAX_COACH_COUNT = 14;

    /**
     * Length units of standars segment element (meters)
     */
    double SEGMENT_LENGTH = 35;

    /**
     * Numbers of curve elements per circle angle 360 / 24 = 15 DEG
     */
    int CURVES_PER_CIRCLE = 24;

    /**
     * Rads per curve unit (rads)
     */
    double CURVE_RADS = 2 * Math.PI / CURVES_PER_CIRCLE;
    /**
     * Default angle for cross element (degrees)
     */
    double DEFAULT_CROSS_ANGLE = 2 * Math.toDegrees(CURVE_RADS);

    /**
     * Radius of standard curve element (meters)
     */
    double RADIUS = SEGMENT_LENGTH * 0.5 / sin(CURVE_RADS);

    /**
     * Length of standard curve element (meters)
     */
    double CURVE_LENGTH = RADIUS * CURVE_RADS;

    /**
     * Gap between tracks (meters)
     */
    double TRACK_GAP = SEGMENT_LENGTH
            * (1 - cos(CURVE_RADS)) / sin(CURVE_RADS);

    /**
     * Rads per curve unit (rads)
     */
    double CURVE_DEGS = 360. / CURVES_PER_CIRCLE;
}