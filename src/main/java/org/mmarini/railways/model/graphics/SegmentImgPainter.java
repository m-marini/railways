package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $Author: marco $
 * @version $Id: SegmentImgPainter.java,v 1.4.16.1 2012/02/04 19:22:58 marco Exp
 *          $
 */
public class SegmentImgPainter implements GraphicContants {
	private static SegmentImgPainter instance = new SegmentImgPainter();

	/**
	 * @return Returns the instance.
	 */
	public static SegmentImgPainter getInstance() {
		return instance;
	}

	private Map<Double, ImagePainter> greenCache;
	private Map<Double, ImagePainter> redCache;

	/**
	 * 
	 */
	private SegmentImgPainter() {
		greenCache = new HashMap<Double, ImagePainter>();
		redCache = new HashMap<Double, ImagePainter>();
		createCache();
	}

	/**
	 * 
	 *
	 */
	private void createCache() {
		loadPainter(9.6, -15, -96);
		loadPainter(8.8, -15, -88);
		loadPainter(17.5, -15, -175);
		loadPainter(25.4, -15, -254);
		loadPainter(35, -15, -350);
	}

	/**
	 * @param red
	 * @return
	 */
	private Map<Double, ImagePainter> getCache(boolean red) {
		if (red)
			return redCache;
		else
			return greenCache;
	}

	/**
	 * @param red
	 * @param length
	 * @return
	 */
	private String getImageName(boolean red, double length) {
		StringBuffer bfr = new StringBuffer();
		bfr.append("/img/seg");
		if (red)
			bfr.append("r");
		else
			bfr.append("g");
		bfr.append(Math.round(length * DEFAULT_SCALE));
		bfr.append(".png");
		return bfr.toString();
	}

	/**
	 * @param length
	 * @return
	 */
	private Painter getPainter(double length, boolean busy) {
		Number key = getPainterKey(length);
		return getCache(busy).get(key);
	}

	/**
	 * @param length
	 * @return
	 */
	private Number getPainterKey(double length) {
		Number found = null;
		double distance = Double.MAX_VALUE;
		for (Double key : greenCache.keySet()) {
			double l = key.doubleValue();
			double d = Math.abs(l - length);
			if (d < distance) {
				found = key;
				distance = d;
			}
		}
		return found;
	}

	/**
	 * @param length
	 * @param x
	 * @param y
	 */
	private void loadPainter(boolean red, double length, double x, double y) {
		String imgName = getImageName(red, length);
		ImagePainter painter = new ImagePainter(imgName, x, y, DEFAULT_SCALE);
		getCache(red).put(new Double(length), painter);
	}

	/**
	 * @param length
	 * @param x
	 * @param y
	 */
	private void loadPainter(double length, double x, double y) {
		loadPainter(true, length, x, y);
		loadPainter(false, length, x, y);
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(GraphicsContext ctx, Segment segment) {
		double length = segment.getLength();
		Graphics2D gr = ctx.getGraphics();
		boolean busy = segment.isBusy();
		while (length > SEGMENT_LENGTH) {
			Painter painter = getPainter(SEGMENT_LENGTH, busy);
			if (painter != null)
				painter.paint(ctx);
			length -= SEGMENT_LENGTH;
			/*
			 * Translate up the context by SEGMENT_LENGTH
			 */
			gr.translate(0, -SEGMENT_LENGTH);
		}
		Painter painter = getPainter(length, busy);
		if (painter != null)
			painter.paint(ctx);
	}
}