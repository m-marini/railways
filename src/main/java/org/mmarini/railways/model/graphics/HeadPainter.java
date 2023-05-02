package org.mmarini.railways.model.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.RailwayConstants;

/**
 * @author $Author: marco $
 * @version $Id: HeadPainter.java,v 1.6 2012/02/08 22:03:31 marco Exp $
 */
public class HeadPainter implements RailwayConstants, GraphicContants, Painter {
	private static HeadPainter instance = new HeadPainter();

	/**
	 * @return Returns the instance.
	 */
	public static HeadPainter getInstance() {
		return instance;
	}

	private Shape shape;
	private Shape leftLight;
	private Shape rightLight;

	/**
	 * @param topology
	 */
	private HeadPainter() {
		shape = new Rectangle2D.Double(-COACH_WIDTH / 2, .3, COACH_WIDTH,
				COACH_LENGTH - .6);
		leftLight = new Ellipse2D.Double(-COACH_WIDTH / 4 - 0.3 / 2, .5, 0.30,
				0.30);
		rightLight = new Ellipse2D.Double(COACH_WIDTH / 4 - 0.3 / 2, .5, 0.30,
				0.30);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		Graphics2D gr = ctx.getGraphics();
		gr.setColor(TRAIN_COLOR);
		gr.fill(shape);
		gr.setColor(BORDER_COLOR);
		gr.draw(shape);
		gr.setColor(Color.WHITE);
		gr.fill(leftLight);
		gr.fill(rightLight);
	}
}