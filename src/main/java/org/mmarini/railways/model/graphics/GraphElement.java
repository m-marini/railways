package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.Topology;

/**
 * @author $$Author: marco $$
 * @version $Id: GraphElement.java,v 1.3 2012/02/08 22:03:30 marco Exp $
 */
public interface GraphElement {

	/**
	 * Finds the selected element on a point.
	 * 
	 * @param location
	 *            the point selection (station coordinate).
	 * @return the selection event.
	 */
	public abstract StationGraphEvent findSelectedElement(Point2D location);

	/**
	 * Finds the selected train on a point.
	 * 
	 * @param location
	 * @return
	 */
	public abstract TrainGraphEvent findSelectedTrain(Point2D location);

	/**
	 * Gets the bounds of the graphic element.
	 * 
	 * @return the bounds
	 */
	public abstract Rectangle2D getBounds();

	/**
	 * @return
	 */
	public abstract Topology getTopology();

	/**
	 * @param context
	 */
	public abstract void paintBase(GraphicsContext context);

	/**
	 * @param context
	 */
	public abstract void paintLabels(GraphicsContext context);

	/**
	 * @param context
	 */
	public abstract void paintNodes(GraphicsContext context);

	/**
	 * @param context
	 */
	public abstract void paintTrain(GraphicsContext context);

	/**
	 * @param context
	 */
	public abstract void paintTransitable(GraphicsContext context);

	/**
	 * @param context
	 */
	public abstract void paintUntransitable(GraphicsContext context);
}