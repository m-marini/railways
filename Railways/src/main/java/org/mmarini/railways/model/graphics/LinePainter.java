package org.mmarini.railways.model.graphics;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.elements.Line;

/**
 * @author $$Author: marco $$
 * @version $Id: LinePainter.java,v 1.6 2012/02/08 22:03:31 marco Exp $
 */
public class LinePainter implements GraphicContants {
	private static final int IMAGE_WIDTH = 46;
	private static final int IMAGE_HEIGHT = 40;
	private static final int READY_INDEX = 0;
	private static final int HELD_INDEX = 1;
	private static final int BUSY_INDEX = 2;

	private static LinePainter instance = new LinePainter();

	/**
	 * @return Returns the instance.
	 */
	public static LinePainter getInstance() {
		return instance;
	}

	private Rectangle2D bounds;
	private Rectangle2D[] activeBounds;
	private ImagePainter painter[][];

	/**
	 * 
	 */
	private LinePainter() {
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
		painter = new ImagePainter[3][2];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 2; j++)
				painter[i][j] = new ImagePainter("/img/line" + i + "" + j
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
	private Painter getPainter(Line line) {
		int i = BUSY_INDEX;
		if (!line.isBusy())
			i = READY_INDEX;
		if (line.isHeld(0))
			i = HELD_INDEX;
		int j = line.isNeighbourBusy() ? 1 : 0;
		return painter[i][j];
	}

	/**
	 * @param gr
	 * @param line
	 */
	public void paintNodes(GraphicsContext ctx, Line line) {
		Painter painter = getPainter(line);
		painter.paint(ctx);
	}
}