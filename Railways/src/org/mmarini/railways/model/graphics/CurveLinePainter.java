package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.util.HashMap;
import java.util.Map;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.elements.Curve;

/**
 * @author $$Author: marco $$
 * @version $Id: CurveLinePainter.java,v 1.4.20.1 2012/02/04 19:22:58 marco Exp
 *          $
 */
public class CurveLinePainter implements GraphicContants, RailwayConstants {
	private static CurveLinePainter instance = new CurveLinePainter();

	/**
	 * @return Returns the instance.
	 */
	public static CurveLinePainter getInstance() {
		return instance;
	}

	private Map<Curve, Shape> cache;

	/**
	 * 
	 */
	private CurveLinePainter() {
		cache = new HashMap<Curve, Shape>();
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape createShape(Curve element) {
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

		double radius = curveRadius;
		double diameter = radius * 2;
		Shape shape = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, angle, Arc2D.OPEN);
		return shape;
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape createTrainArea(Curve element) {
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

		double radius = curveRadius;
		double diameter = radius * 2;
		Shape shape = new Arc2D.Double(xc - radius, yc - radius, diameter,
				diameter, start, end - start, Arc2D.OPEN);
		return shape;
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape getShape(Curve element) {
		Shape area = cache.get(element);
		if (area == null) {
			area = createShape(element);
			cache.put(element, area);
		}
		return area;
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(Graphics2D gr, Curve element) {
		if (!element.isBusy()) {
			gr.setColor(TRACK_GREEN_COLOR);
		} else {
			gr.setColor(TRACK_RED_COLOR);
		}
		Shape area = getShape(element);
		gr.setStroke(BASIC_STROKE);
		gr.draw(area);
	}

	/**
	 * @param gr
	 * @param elem
	 */
	public void paintTrain(Graphics2D gr, Curve elem) {
		Shape shape = createTrainArea(elem);
		gr.setStroke(BASIC_STROKE);
		gr.setColor(TRAIN_COLOR);
		gr.draw(shape);
	}

}