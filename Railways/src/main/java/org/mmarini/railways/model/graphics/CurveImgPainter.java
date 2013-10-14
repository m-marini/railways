package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mmarini.railways.model.elements.Curve;

/**
 * @author $Author: marco $
 * @version $Id: CurveImgPainter.java,v 1.5 2012/02/08 22:03:31 marco Exp $
 */
public class CurveImgPainter implements GraphicContants {
	class CacheKey {
		private boolean right;
		private boolean red;
		private double radius;

		public CacheKey(boolean red, double radius, double angle) {
			super();
			this.red = red;
			this.radius = radius;
			this.right = (angle >= 0);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CacheKey other = (CacheKey) obj;
			if (Double.doubleToLongBits(radius) != Double
					.doubleToLongBits(other.radius))
				return false;
			if (red != other.red)
				return false;
			if (right != other.right)
				return false;
			return true;
		}

		/**
		 * @return the radius
		 */
		double getRadius() {
			return radius;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(radius);
			result = PRIME * result + (int) (temp ^ (temp >>> 32));
			result = PRIME * result + (red ? 1231 : 1237);
			result = PRIME * result + (right ? 1231 : 1237);
			return result;
		}

		/**
		 * @return the red
		 */
		boolean isRed() {
			return red;
		}

		/**
		 * @return the right
		 */
		boolean isRight() {
			return right;
		}

	}

	private static CurveImgPainter instance = new CurveImgPainter();

	/**
	 * @return Returns the instance.
	 */
	public static CurveImgPainter getInstance() {
		return instance;
	}

	private Map<CacheKey, Painter> cache;

	/**
	 * 
	 */
	private CurveImgPainter() {
		cache = new HashMap<CacheKey, Painter>();
		createCache();
	}

	/**
	 * 
	 * 
	 */
	private void createCache() {
		loadPainter(RADIUS, CURVE_DEGS, -7, -176);
		loadPainter(RADIUS + TRACK_GAP, CURVE_DEGS, -7, -188);
		loadPainter(RADIUS + TRACK_GAP, 6, -7, -66);
		loadPainter(RADIUS + TRACK_GAP, 9, -7, -119);

		loadPainter(RADIUS, -CURVE_DEGS, -29, -176);
		loadPainter(RADIUS + TRACK_GAP, -CURVE_DEGS, -32, -188);
		loadPainter(RADIUS + TRACK_GAP, -6, -11, -66);
		loadPainter(RADIUS + TRACK_GAP, -9, -17, -119);
	}

	/**
	 * @param radius
	 * @param angle
	 * @return
	 */
	private AffineTransform createTransform(double radius, double angle) {
		double y = -radius * Math.sin(Math.abs(angle));
		double x = radius * (1 - Math.cos(angle));
		if (angle < 0)
			x = -x;
		AffineTransform tran = AffineTransform.getTranslateInstance(x, y);
		tran.rotate(angle);
		return tran;
	}

	/**
	 * @param busy
	 * @param radius
	 * @param angle
	 * @return
	 */
	private String getImageName(boolean busy, double radius, double angle) {
		StringBuffer bfr = new StringBuffer();
		bfr.append("/img/curve");
		if (angle < 0)
			bfr.append("l");
		else
			bfr.append("r");
		if (busy)
			bfr.append("r-");
		else
			bfr.append("g-");
		bfr.append(Math.round(radius));
		bfr.append("-");
		bfr.append(Math.round(Math.abs(angle)));
		bfr.append(".png");
		return bfr.toString();
	}

	/**
	 * @param busy
	 * @param radius
	 * @param angle
	 * @return
	 */
	private Painter getPainter(boolean busy, double radius, double angle) {
		CacheKey ref = new CacheKey(busy, radius, angle);
		Painter painter = cache.get(ref);
		if (painter != null)
			return painter;
		double distance = Double.MAX_VALUE;
		for (Entry<CacheKey, Painter> entry : cache.entrySet()) {
			CacheKey key = entry.getKey();
			if (ref.isRed() == key.isRed() && ref.isRight() == key.isRight()) {
				double l = key.getRadius();
				double d = Math.abs(l - radius);
				if (d < distance) {
					painter = entry.getValue();
					distance = d;
				}
			}
		}
		return painter;
	}

	/**
	 * @param busy
	 * @param radius
	 * @param angle
	 * @param x
	 * @param y
	 */
	private void loadPainter(boolean busy, double radius, double angle,
			double x, double y) {
		String name = getImageName(busy, radius, angle);
		Painter painter = new ImagePainter(name, x, y, DEFAULT_SCALE);
		CacheKey key = new CacheKey(busy, radius, angle);
		cache.put(key, painter);
	}

	/**
	 * 
	 * @param radius
	 * @param angle
	 * @param x
	 * @param y
	 */
	private void loadPainter(double radius, double angle, double x, double y) {
		loadPainter(false, radius, angle, x, y);
		loadPainter(true, radius, angle, x, y);
	}

	/**
	 * @param gr
	 * @param element
	 */
	public void paint(GraphicsContext ctx, Curve curve) {
		double angle = curve.getAngle();
		double radius = curve.getRadius();
		boolean busy = curve.isBusy();
		Graphics2D gr = ctx.getGraphics();
		double angleCount = Math.abs(angle);
		Painter painter = getPainter(busy, radius, angle < 0 ? -CURVE_DEGS
				: CURVE_DEGS);
		AffineTransform tran = createTransform(radius, angle < 0 ? -CURVE_RADS
				: CURVE_RADS);
		while (angleCount > CURVE_DEGS) {
			if (painter != null)
				painter.paint(ctx);
			angleCount -= CURVE_DEGS;
			gr.transform(tran);
		}
		painter = getPainter(busy, radius, angle < 0 ? -angleCount : angleCount);
		if (painter != null)
			painter.paint(ctx);
	}
}