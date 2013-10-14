package org.mmarini.railways.model.graphics;

import java.awt.geom.Rectangle2D;

public class DeadTrackImgPainter implements Painter, GraphicContants {
	private static DeadTrackImgPainter instance = new DeadTrackImgPainter();

	/**
	 * @return Returns the instance.
	 */
	public static DeadTrackImgPainter getInstance() {
		return instance;
	}

	private ImagePainter painter;
	private Rectangle2D bounds;

	/**
	 * 
	 *
	 */
	private DeadTrackImgPainter() {
		painter = new ImagePainter("/img/end.png", -23, 0, DEFAULT_SCALE);
		bounds = new Rectangle2D.Double(-2.3, 0, 4.6, 2.0);
	}

	/**
	 * 
	 * @return
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		painter.paint(ctx);
	}
}
