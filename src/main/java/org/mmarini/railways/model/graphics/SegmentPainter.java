package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $$Author: marco $$
 * @version $Id: SegmentPainter.java,v 1.7 2012/02/08 22:03:30 marco Exp $
 */
public class SegmentPainter implements GraphicContants {
	private static SegmentPainter instance = new SegmentPainter();

	/**
	 * @return Returns the instance.
	 */
	public static SegmentPainter getInstance() {
		return instance;
	}

	private Map<Segment, Shape[]> cache;
	private Map<Segment, Rectangle2D> boundCache;

	/**
	 * 
	 */
	private SegmentPainter() {
		cache = new HashMap<Segment, Shape[]>();
		boundCache = new HashMap<Segment, Rectangle2D>();
	}

	/**
	 * @param element
	 * @return
	 */
	private Rectangle2D createBounds(Segment element) {
		double length = element.getLength();
		return new Rectangle2D.Double(-WIDTH / 2, -length, WIDTH, length);
	}

	/**
	 * @param element
	 * @return
	 */
	private Shape[] createShapes(Segment element) {
		Shape[] shapes = new Shape[3];
		double length = element.getLength();
		shapes[0] = new Line2D.Double(-WIDTH / 2, -length, -WIDTH / 2, 0);
		shapes[1] = new Line2D.Double(WIDTH / 2, -length, WIDTH / 2, 0);
		shapes[2] = new Line2D.Double(0, -length, 0, 0);
		return shapes;
	}

	/**
	 * @param from
	 * @param to
	 * @return
	 */
	private Shape createTrainShape(double from, double to) {
		if (to < from) {
			double t = to;
			to = from;
			from = t;
		}
		Shape shape = new Rectangle2D.Double(-WIDTH / 2, -to, WIDTH, to - from);
		return shape;
	}

	/**
	 * @return
	 */
	public Rectangle2D getBounds(Segment element) {
		Map<Segment, Rectangle2D> cache = boundCache;
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
	private Shape[] getShapes(Segment element) {
		Shape[] shapes = cache.get(element);
		if (shapes == null) {
			shapes = createShapes(element);
			cache.put(element, shapes);
		}
		return shapes;
	}

	/**
	 * @param location
	 * @param from
	 * @param to
	 * @return
	 */
	public boolean isOverTrain(Point2D location, double from, double to) {
		Shape shape = createTrainShape(from, to);
		return shape.contains(location);
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(Graphics2D gr, Segment element) {
		Shape[] shape = getShapes(element);
		gr.setStroke(BASIC_STROKE);
		gr.setColor(TRACK_BLACK_COLOR);
		gr.draw(shape[0]);
		gr.draw(shape[1]);
		if (element.isBusy())
			gr.setColor(TRACK_RED_COLOR);
		else
			gr.setColor(TRACK_GREEN_COLOR);
		gr.setStroke(LIGHTS_STROKE);
		gr.draw(shape[2]);
	}

	/**
	 * @param gr
	 * @param from
	 * @param to
	 */
	public void paintTrain(Graphics2D gr, double from, double to) {
		Shape shape = createTrainShape(from, to);
		gr.setStroke(BASIC_STROKE);
		gr.setColor(TRAIN_COLOR);
		gr.fill(shape);
	}
}