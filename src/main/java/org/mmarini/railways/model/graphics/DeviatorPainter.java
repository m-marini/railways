package org.mmarini.railways.model.graphics;

import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Deviator;

/**
 * @author $$Author: marco $$
 * @version $Id: DeviatorPainter.java,v 1.1.4.1.2.1 2005/08/22 18:47:44 marco
 *          Exp $
 */
public class DeviatorPainter implements GraphicContants {
	private static final int X_IMAGE = -23;
	private static final int Y_IMAGE = -10;
	private static final int IMAGE_WIDTH = 9;
	private static final int IMAGE_HEIGHT = 20;
	private static DeviatorPainter instance = new DeviatorPainter();

	/**
	 * @return Returns the instance.
	 */
	public static DeviatorPainter getInstance() {
		return instance;
	}

	private Rectangle2D bounds;
	private Rectangle2D activeBounds;
	private ImagePainter painter[][][];

	/**
	 * 
	 */
	private DeviatorPainter() {
		bounds = new Rectangle2D.Double(X_IMAGE / DEFAULT_SCALE, Y_IMAGE
				/ DEFAULT_SCALE, (-X_IMAGE / DEFAULT_SCALE) + COACH_WIDTH,
				IMAGE_HEIGHT / DEFAULT_SCALE);
		activeBounds = new Rectangle2D.Double(X_IMAGE / DEFAULT_SCALE, Y_IMAGE
				/ DEFAULT_SCALE, IMAGE_WIDTH / DEFAULT_SCALE, IMAGE_HEIGHT
				/ DEFAULT_SCALE);
		painter = new ImagePainter[2][2][2];
		for (int i = 0; i < 2; ++i)
			for (int j = 0; j < 2; ++j)
				for (int k = 0; k < 2; ++k)
					painter[i][j][k] = new ImagePainter("/img/dev" + i + "" + j
							+ "" + k + ".png", X_IMAGE, Y_IMAGE, DEFAULT_SCALE);
	}

	/**
	 * @return Returns the activeBounds.
	 */
	public Rectangle2D getActiveBounds() {
		return activeBounds;
	}

	/**
	 * @return
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * @param element
	 * @return
	 */
	private Painter getPainter(CrossDeviator element) {
		int i = element.isDeviated() ? 1 : 0;
		int j = element.isLocked() ? 1 : 0;
		int k = element.isBusy() ? 1 : 0;
		return painter[i][j][k];
	}

	/**
	 * @param element
	 * @return
	 */
	private Painter getPainter(Deviator element) {
		int i = element.isDeviated() ? 1 : 0;
		int j = element.isLocked() ? 1 : 0;
		int k = element.isBusy() ? 1 : 0;
		return painter[i][j][k];
	}

	/**
	 * @param gr
	 * @param line
	 */
	public void paintNodes(GraphicsContext ctx, CrossDeviator line) {
		Painter painter = getPainter(line);
		painter.paint(ctx);
	}

	/**
	 * @param gr
	 * @param line
	 */
	public void paintNodes(GraphicsContext ctx, Deviator line) {
		Painter painter = getPainter(line);
		painter.paint(ctx);
	}
}