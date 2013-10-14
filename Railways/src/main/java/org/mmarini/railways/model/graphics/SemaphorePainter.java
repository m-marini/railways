package org.mmarini.railways.model.graphics;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.elements.Semaphore;

/**
 * @author $$Author: marco $$
 * @version $Id: SemaphorePainter.java,v 1.5.16.1 2012/02/04 19:22:58 marco Exp
 *          $
 */
public class SemaphorePainter implements GraphicContants {
	private static final int IMAGE_WIDTH = 46;
	private static final int IMAGE_HEIGHT = 40;
	private static final int READY_INDEX = 0;
	private static final int HELD_INDEX = 1;
	private static final int BUSY_INDEX = 2;

	private static SemaphorePainter instance = new SemaphorePainter();

	/**
	 * @return Returns the instance.
	 */
	public static SemaphorePainter getInstance() {
		return instance;
	}

	private Rectangle2D bounds;
	private Rectangle2D[] activeBounds;
	private ImagePainter painter[][];

	/**
	 * 
	 */
	private SemaphorePainter() {
		bounds = new Rectangle2D.Double(-IMAGE_WIDTH / 2 / DEFAULT_SCALE,
				-IMAGE_HEIGHT / 2 / DEFAULT_SCALE, IMAGE_WIDTH / DEFAULT_SCALE,
				IMAGE_HEIGHT / DEFAULT_SCALE);
		activeBounds = new Rectangle2D[] {
				new Rectangle2D.Double(-IMAGE_WIDTH / 2 / DEFAULT_SCALE,
						-IMAGE_HEIGHT / 2 / DEFAULT_SCALE, IMAGE_WIDTH / 2
								/ DEFAULT_SCALE, IMAGE_HEIGHT / DEFAULT_SCALE),
				new Rectangle2D.Double(0, -IMAGE_HEIGHT / 2 / DEFAULT_SCALE,
						IMAGE_WIDTH / 2 / DEFAULT_SCALE, IMAGE_HEIGHT
								/ DEFAULT_SCALE) };

		int n = 3;
		painter = new ImagePainter[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				painter[i][j] = new ImagePainter("/img/sem" + i + "" + j
						+ ".png", -IMAGE_WIDTH / 2, -IMAGE_HEIGHT / 2,
						DEFAULT_SCALE);
	}

	/**
	 * Finds the index of component containing the point.
	 * 
	 * @param point
	 *            the point
	 * @return the index of component containing the point ( <0 if none)
	 */
	public int findIndexOver(Point2D point) {
		Shape[] shape = activeBounds;
		for (int i = 0; i < shape.length; ++i) {
			if (shape[i].contains(point)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * @param line
	 * @return
	 */
	private Painter getPainter(Semaphore line) {
		return painter[getPainterIndex(line, 0)][getPainterIndex(line, 1)];
	}

	/**
	 * @param line
	 * @param i
	 * @return
	 */
	private int getPainterIndex(Semaphore line, int i) {
		if (!line.isBusy(i))
			return READY_INDEX;
		if (line.isLocked(i))
			return HELD_INDEX;
		return BUSY_INDEX;
	}

	/**
	 * @param gr
	 * @param line
	 */
	public void paintNodes(GraphicsContext ctx, Semaphore line) {
		Painter painter = getPainter(line);
		painter.paint(ctx);
	}
}