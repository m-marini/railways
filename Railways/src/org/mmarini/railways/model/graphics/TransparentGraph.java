package org.mmarini.railways.model.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.Topology;

/**
 * @author $$Author: marco $$
 * @version $Id: TransparentGraph.java,v 1.1.4.1.6.1 2005/08/30 18:01:18 marco
 *          Exp $
 */
public abstract class TransparentGraph implements GraphElement {
	private AffineTransform transform;
	private AffineTransform invTransform;
	private Rectangle2D bounds = new Rectangle2D.Double();

	/**
	 * @param location
	 * @param direction
	 */
	public TransparentGraph(Topology topology) {
		Point2D location = topology.getLocation();
		transform = AffineTransform.getTranslateInstance(location.getX(),
				location.getY());
		double direction = topology.getDirection();
		transform.rotate(Math.toRadians(direction));
		try {
			invTransform = transform.createInverse();
		} catch (NoninvertibleTransformException e) {
		}
	}

	/**
	 * @param base
	 */
	protected void createTransformedBounds(Rectangle2D base) {
		AffineTransform trans = transform;
		Area area = new Area(base);
		area = area.createTransformedArea(trans);
		setBounds(area.getBounds());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedElement(java.awt.geom.Point2D)
	 */
	@Override
	public StationGraphEvent findSelectedElement(Point2D point) {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#getBounds()
	 */
	@Override
	public Rectangle2D getBounds() {
		return bounds;
	}

	/**
	 * @return Returns the invTransform.
	 */
	protected AffineTransform getInvTransform() {
		return invTransform;
	}

	/**
	 * @return Returns the transform.
	 */
	protected AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintBase(GraphicsContext)
	 */
	@Override
	public void paintBase(GraphicsContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintLabels(GraphicsContext)
	 */
	@Override
	public void paintLabels(GraphicsContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintNodes(GraphicsContext)
	 */
	@Override
	public void paintNodes(GraphicsContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintTrain(GraphicsContext)
	 */
	@Override
	public void paintTrain(GraphicsContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintTransitable(GraphicsContext)
	 */
	@Override
	public void paintTransitable(GraphicsContext context) {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintUntransitable(GraphicsContext)
	 */
	@Override
	public void paintUntransitable(GraphicsContext context) {
	}

	/**
	 * @param bounds
	 *            The bounds to set.
	 */
	protected void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}
}