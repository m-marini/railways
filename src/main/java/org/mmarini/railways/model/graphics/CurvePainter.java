package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.elements.Curve;

/**
 * @author $$Author: marco $$
 * @version $Id: CurvePainter.java,v 1.8 2012/02/08 22:03:31 marco Exp $
 */
public class CurvePainter implements GraphicContants, RailwayConstants {
	private static CurvePainter instance = new CurvePainter();

	/**
	 * @return Returns the instance.
	 */
	public static CurvePainter getInstance() {
		return instance;
	}

	private Map<Curve, Shape[]> cache;
	private Map<Curve, Rectangle2D> boundCache;

	/**
	 * 
	 */
	protected CurvePainter() {
		cache = new HashMap<Curve, Shape[]>();
		boundCache = new HashMap<Curve, Rectangle2D>();
	}

	/**
	 * @param element
	 * @return
	 */
	private Rectangle2D createBounds(Curve element) {
		double angle = element.getAngle();
		double xc = 0;
		double yc = 0;
		double start = 0;
		double curveRadius = element.getRadius();
		if (angle > 0) {
			xc = curveRadius;
			start = 180;
		} else {
			xc = -curveRadius;
		}
		angle = -angle;

		double radius = curveRadius + WIDTH / 2;
		double diameter = radius * 2;
		Shape shape = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, angle, Arc2D.PIE);
		Area area = new Area(shape);
		radius -= WIDTH;
		diameter = radius * 2;
		shape = new Ellipse2D.Double(xc - radius, yc - radius, diameter,
				diameter);
		area.subtract(new Area(shape));
		return area.getBounds2D();
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape[] createShapes(Curve element) {
		double angle = element.getAngle();
		double xc = 0;
		double yc = 0;
		double start = 0;
		double curveRadius = element.getRadius();
		if (angle > 0) {
			xc = curveRadius;
			start = 180;
		} else {
			xc = -curveRadius;
		}
		angle = -angle;

		double radius = curveRadius + WIDTH / 2;
		double diameter = radius * 2;
		Shape shapes[] = new Shape[3];
		shapes[0] = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, angle, Arc2D.OPEN);
		radius -= WIDTH;
		diameter = radius * 2;
		shapes[1] = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, angle, Arc2D.OPEN);
		radius = curveRadius;
		diameter = radius * 2;
		shapes[2] = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, angle, Arc2D.OPEN);
		return shapes;
	}

	/**
	 * @param element
	 * @return
	 */
	private Area createTrainArea(Curve element) {
		double angle = element.getAngle();
		double xc = 0;
		double yc = 0;
		double start = element.getTrainHead();
		double end = element.getTrainTail();
		if (start > end) {
			double t = start;
			start = end;
			end = t;
		}
		double curveRadius = element.getRadius();
		start = Math.toDegrees(start / curveRadius);
		end = Math.toDegrees(end / curveRadius);
		if (angle > 0) {
			xc = curveRadius;
			start = 180 - start;
			end = 180 - end;
		} else {
			xc = -curveRadius;
		}

		double radius = curveRadius + WIDTH / 2;
		double diameter = radius * 2;
		Shape shape = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, end - start, Arc2D.PIE);
		Area area = new Area(shape);
		radius -= WIDTH;
		diameter = radius * 2;
		shape = new Ellipse2D.Double(xc - radius, yc - radius, diameter,
				diameter);
		area.subtract(new Area(shape));
		return area;
	}

	/**
	 * @return
	 */
	public Rectangle2D getBounds(Curve element) {
		Map<Curve, Rectangle2D> cache = boundCache;
		Rectangle2D bounds = cache.get(element);
		if (bounds == null) {
			bounds = createBounds(element);
			cache.put(element, bounds);

		}
		return bounds;
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape[] getShapes(Curve element) {
		Shape[] area = cache.get(element);
		if (area == null) {
			area = createShapes(element);
			cache.put(element, area);
		}
		return area;
	}

	/**
	 * @param location
	 * @param element
	 * @return
	 */
	public boolean isOverTrain(Point2D location, Curve element) {
		Area area = createTrainArea(element);
		return area.contains(location);
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(Graphics2D gr, Curve element) {
		Shape[] shapes = getShapes(element);
		gr.setStroke(BASIC_STROKE);
		gr.setColor(TRACK_BLACK_COLOR);
		gr.draw(shapes[0]);
		gr.draw(shapes[1]);
		if (!element.isBusy()) {
			gr.setColor(TRACK_GREEN_COLOR);
		} else {
			gr.setColor(TRACK_RED_COLOR);
		}
		gr.setStroke(LIGHTS_STROKE);
		gr.draw(shapes[2]);
	}

}