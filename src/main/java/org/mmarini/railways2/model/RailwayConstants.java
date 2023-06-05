package org.mmarini.railways2.model;

import org.mmarini.railways.model.xml.XmlConstants;

import static java.lang.Math.*;

/**
 * @author $Author: marco $
 * @version $Id: RailwayConstants.java,v 1.1.4.1.2.1 2005/08/24 17:07:10 marco
 * Exp $
 */
public interface RailwayConstants extends XmlConstants {

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
    double RADIUS = 400;

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
     * Angle of standard switch element (DEG)
     */
    int SWITCH_ANGLE_DEG = 3;

    /**
     * Angle of standard switch element (RAD)
     */
    double SWITCH_ANGLE_RAD = toRadians(SWITCH_ANGLE_DEG);

    /**
     * Radius of standard switch element (m)
     */
    double SWITCH_RADIUS = 1200;

    /**
     * Horizontal length of diverged switch track (m)
     */
    double DIVERGED_LENGTH = SWITCH_RADIUS * sin(toRadians(SWITCH_ANGLE_DEG));

    /**
     * Vertical distange of platform switch (m)
     */
    double PLATFORM_SWITCH_Y = SWITCH_RADIUS * (1 - cos(toRadians(SWITCH_ANGLE_DEG)));

    /**
     * Horizontal length of switch gap (m)
     * <p>
     * <pre>
     * sg tan(alpha) + r (1-cos(alpha)) = tg / 2
     * sg = [tg / 2 - r (1-cos(alpha))] / tan(alpha)
     * </pre>
     */
    double SWITCH_GAP = (TRACK_GAP / 2 - PLATFORM_SWITCH_Y) / tan(toRadians(SWITCH_ANGLE_DEG));

    /**
     * Length of standard switch element (m)
     * <p>
     * <pre>
     * sl = 2 sg + dl
     * </pre>
     * </p>
     */
    double SWITCH_LENGTH = DIVERGED_LENGTH + 2 * SWITCH_GAP;

    /**
     * Length of standard segment element (m)
     */
    double SEGMENT_LENGTH = SWITCH_LENGTH;


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
    /**
     * Default train frequency (#/s)
     */
    double DEFAULT_TRAIN_FREQUENCY = 30d / 3600;
}