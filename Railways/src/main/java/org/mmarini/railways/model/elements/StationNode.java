package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.ValidationException;
import org.mmarini.railways.model.routes.NodeJunction;

/**
 * @author $$Author: marco $$
 * @version $Id: StationNode.java,v 1.4 2012/02/08 22:03:21 marco Exp $
 */
public interface StationNode extends StationElement {

	/**
	 * @param index
	 * @param track
	 * @param atTrackIndex
	 * @throws ValidationException
	 */
	public abstract void attach(int index, Track track, int atTrackIndex)
			throws ValidationException;

	/**
	 * Gets a junction of the node.
	 * 
	 * @param index
	 *            the index of the track.
	 * @return the junction of the node.
	 */
	public abstract NodeJunction getJunction(int index);

	/**
	 * Gets the reference string of the node.
	 * 
	 * @return the reference string of the node.
	 */
	public abstract String getReference();

	/**
	 * Gets the tracks of the node.
	 * 
	 * @return the tracks of the node.
	 */
	public abstract Track[] getTrack();

	/**
	 * Gets a track of the node.
	 * 
	 * @param index
	 *            the index track.
	 * @return the track of the node.
	 */
	public abstract Track getTrack(int index);

	/**
	 * @param index
	 * @param track
	 */
	public abstract void setTrack(int index, Track track);
}