package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $$Author: marco $$
 * @version $Id: SegmentLinePainter.java,v 1.5.20.1 2012/02/04 19:22:59 marco
 *          Exp $
 */
public class SegmentLinePainter implements GraphicContants {
	private static SegmentLinePainter instance = new SegmentLinePainter();

	/**
	 * @return Returns the instance.
	 */
	public static SegmentLinePainter getInstance() {
		return instance;
	}

	private Map<Segment, Line2D> cache;

	/**
	 * 
	 */
	private SegmentLinePainter() {
		cache = new HashMap<Segment, Line2D>();
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
		Shape shape = new Line2D.Double(0, -to, 0, -from);
		return shape;
	}

	/**
	 * @param element
	 * @return
	 */
	private Line2D getLine(Segment element) {
		Line2D line = cache.get(element);
		if (line == null) {
			double length = element.getLength();
			line = new Line2D.Double(0, -length, 0, 0);
			cache.put(element, line);
		}
		return line;
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(Graphics2D gr, Segment element) {
		Shape shape = getLine(element);
		gr.setStroke(BASIC_STROKE);
		if (element.isBusy())
			gr.setColor(TRACK_RED_COLOR);
		else
			gr.setColor(TRACK_GREEN_COLOR);
		gr.draw(shape);
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
		gr.draw(shape);
	}

}