package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.routes.TrackJunction;

/**
 * @author $$Author: marco $$
 * @version $Id: Track.java,v 1.3 2012/02/08 22:03:21 marco Exp $
 */
public interface Track extends StationElement {

	/**
	 * Gets a track junction.
	 * 
	 * @param index
	 *            the index of the junction.
	 * @return the track junction.
	 */
	public abstract TrackJunction getJunction(int index);

	/**
	 * Gets the length of the track.
	 * 
	 * @return the length of the track.
	 */
	public abstract double getLength();

	/**
	 * Gets the nodes.
	 * 
	 * @return the nodes.
	 */
	public abstract StationNode[] getNode();

	/**
	 * Gets a node.
	 * 
	 * @param index
	 *            the index of node.
	 * @return the node.
	 */
	public abstract StationNode getNode(int index);

	/**
	 * Returns true if the track is busy.
	 * 
	 * @return true if the track is busy.
	 */
	public abstract boolean isBusy();

	/**
	 * @param index
	 * @param node
	 */
	public abstract void setNode(int index, StationNode node);
}