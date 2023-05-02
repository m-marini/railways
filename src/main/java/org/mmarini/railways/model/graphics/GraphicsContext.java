package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @author $Author: marco $
 * @version $Id: GraphicsContext.java,v 1.5 2012/02/08 22:03:30 marco Exp $
 */
public class GraphicsContext {
	private Graphics2D graphics;
	private PainterFactory painterFactory;
	private Rectangle2D bounds;

	/**
	 * 
	 */
	public GraphicsContext() {
	}

	/**
	 * @param ctx
	 */
	public GraphicsContext(GraphicsContext ctx) {
		this.graphics = ctx.graphics;
		this.painterFactory = ctx.painterFactory;
		this.bounds = ctx.bounds;
	}

	/**
	 * @return Returns the bounds.
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * @return Returns the graphics.
	 */
	public Graphics2D getGraphics() {
		return graphics;
	}

	/**
	 * @return Returns the painterFactory.
	 */
	public PainterFactory getPainterFactory() {
		return painterFactory;
	}

	/**
	 * @param bounds
	 *            The bounds to set.
	 */
	public void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}

	/**
	 * @param graphics
	 *            The graphics to set.
	 */
	public void setGraphics(Graphics2D graphics) {
		this.graphics = graphics;
	}

	/**
	 * @param painterFactory
	 *            The painterFactory to set.
	 */
	public void setPainterFactory(PainterFactory painterFactory) {
		this.painterFactory = painterFactory;
	}
}
