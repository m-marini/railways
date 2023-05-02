package org.mmarini.railways.model.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class BackgroundPainter implements Painter, GraphicContants {
	private Color color;

	/**
	 * @param color
	 */
	public BackgroundPainter(Color color) {
		this.color = color;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		Graphics2D gr = ctx.getGraphics();
		gr.setColor(color);
		Rectangle2D bounds = ctx.getBounds();
		gr.fill(bounds);
	}
}
