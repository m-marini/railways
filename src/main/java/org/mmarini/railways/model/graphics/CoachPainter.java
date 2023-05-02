package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.RailwayConstants;

/**
 * @author $Author: marco $
 * @version $Id: CoachPainter.java,v 1.6 2012/02/08 22:03:31 marco Exp $
 */
public class CoachPainter implements RailwayConstants, GraphicContants, Painter {
	private static Painter instance = new CoachPainter();

	/**
	 * @return Returns the instance.
	 */
	public static Painter getInstance() {
		return instance;
	}

	private Shape shape;

	/**
	 * @param topology
	 */
	private CoachPainter() {
		shape = new Rectangle2D.Double(-COACH_WIDTH / 2, .3, COACH_WIDTH,
				COACH_LENGTH - .6);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(java.awt.Graphics2D)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		Graphics2D gr = ctx.getGraphics();
		gr.setColor(TRAIN_COLOR);
		gr.fill(shape);
		gr.setColor(BORDER_COLOR);
		gr.draw(shape);
	}
}