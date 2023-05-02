package org.mmarini.railways.model.graphics;

import java.awt.BasicStroke;
import java.awt.Color;

import org.mmarini.railways.model.RailwayConstants;

/**
 * @author $$Author: marco $$
 * @version $Id: GraphicContants.java,v 1.1.4.1.2.1 2005/08/23 06:03:59 marco
 *          Exp $
 */
public interface GraphicContants extends RailwayConstants {
	public static final double COACH_RAIL_DISTANCE = 2.;

	public static final double DEFAULT_SCALE = 10.;

	public static final BasicStroke BASIC_STROKE = new BasicStroke(0);
	public static final BasicStroke LIGHTS_STROKE = new BasicStroke(0,
			BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10f, new float[] {
					0.1f, 0.7f }, 0f);

	public final static double WIDTH = 1.435;

	public static final Color BACKGROUND_COLOR = Color.getHSBColor(95f / 360f,
			0.08f, 0.40f);
	public static final Color PANEL_COLOR = Color.getHSBColor(0.278f, 0.10f,
			0.671f);
	public static final Color BOUNDS_COLOR = Color.LIGHT_GRAY;
	public static final Color BORDER_COLOR = Color.BLACK;

	public static final Color TRACK_GREEN_COLOR = Color.getHSBColor(
			162f / 360f, 0.9f, 1f);
	public static final Color TRACK_RED_COLOR = Color.getHSBColor(343f / 360,
			0.93f, 1f);
	public static final Color TRACK_BLACK_COLOR = Color.getHSBColor(26f / 360,
			0.23f, 0.27f);

	public static final Color SEM_OPEN_COLOR = Color.getHSBColor(162f / 360f,
			0.9f, 1f);
	public static final Color SEM_UNDEFINED_COLOR = Color.YELLOW;
	public static final Color SEM_BUSY_COLOR = Color.getHSBColor(343f / 360,
			0.93f, 1f);
	public static final Color SEM_HELD_COLOR = SEM_BUSY_COLOR;
	public static final Color SEM_NOT_HELD_COLOR = Color.DARK_GRAY;

	public static final Color DEV_UNLOCKED_COLOR = Color.DARK_GRAY;
	public static final Color DEV_LOCKED_COLOR = Color.getHSBColor(0f, 1f, 1f);
	public static final Color DEV_BUSY_COLOR = SEM_BUSY_COLOR;
	public static final Color DEV_OPEN_COLOR = SEM_OPEN_COLOR;

	public static final Color TRAIN_COLOR = Color.getHSBColor(53f / 360f, .80f,
			1f);
}