package org.mmarini.railways2.model;

import org.mmarini.railways.model.xml.XmlConstants;

import static java.lang.Math.sin;

/**
 * @author $Author: marco $
 * @version $Id: RailwayConstants.java,v 1.1.4.1.2.1 2005/08/24 17:07:10 marco
 * Exp $
 */
public interface RailwayConstants extends XmlConstants {

    /**
     * Length of standard segment element (m)
     */
    double SEGMENT_LENGTH = 35;

    /**
     * Numbers of curve elements per circle
     */
    int CURVES_PER_CIRCLE = 24;

    /**
     * Rads per curve unit (rads) = 15 DEG
     */
    double CURVE_RADS = 2 * Math.PI / CURVES_PER_CIRCLE;
    /**
     * Radius of standard curve element (meters)
     */
    double RADIUS = SEGMENT_LENGTH * 0.5 / sin(CURVE_RADS);

    /**
     * Minimun number of coach per train.
     */
    int MIN_COACH_COUNT = 3;

    /**
     * Maximum number od coach per train.
     */
    int MAX_COACH_COUNT = 14;

    /**
     * Track gauge (m)
     */
    double TRACK_GAUGE = 1.435;
    /**
     * Gap between adjacent track (m)
     */
    double TRACK_GAP = 3.22 + TRACK_GAUGE;
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
     * Approach speed 1.8 Kmh
     */
    double APPROACH_SPEED = 0.5;
    /**
     * The acceleration (meters / sec ^ 2) 400kN / 642t = 0.623
     */
    double ACCELERATION = 400. / 642.;
    /**
     * The deceleration (meters / sec ^ 2) = 100./2/3.6/3.6/2 = 1.929
     */
    double DEACCELERATION = -100. / 2 / 3.6 / 3.6 / 2;
    /**
     * Coach length
     */
    double COACH_LENGTH = 25;
    /**
     * Distance between rail and coach end (m)
     */
    double COACH_RAIL_DISTANCE = 2;
}