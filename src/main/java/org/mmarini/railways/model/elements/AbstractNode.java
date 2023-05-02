package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.ValidationException;
import org.mmarini.railways.model.routes.NodeJunction;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractNode.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public abstract class AbstractNode implements StationNode {
	private Topology topology;
	private String reference;
	private Track[] track;
	private NodeJunction[] junction;

	/**
	 * @param reference
	 * @param trackCount
	 */
	protected AbstractNode(String reference, int trackCount) {
		this.reference = reference;
		track = new Track[trackCount];
		junction = new NodeJunction[trackCount];
	}

	/**
	 * @throws ValidationException
	 * @see org.mmarini.railways.model.elements.StationNode#attach(int,
	 *      org.mmarini.railways.model.elements.Track, int)
	 */
	@Override
	public void attach(int index, Track track, int atTrackIndex)
			throws ValidationException {
		Track oldTrack;
		try {
			oldTrack = getTrack(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ValidationException("Track " + index + " on node "
					+ getReference() + " does not exist", e);
		}
		if (oldTrack != null)
			throw new ValidationException("Track " + index + " on node "
					+ getReference() + " already connect to " + oldTrack);
		getJunction(index).attach(track.getJunction(atTrackIndex));
		setTrack(index, track);
		track.setNode(atTrackIndex, this);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StationNode))
			return false;
		return this.getReference().equals(((StationNode) obj).getReference());
	}

	/**
	 * @return Returns the junction.
	 */
	public NodeJunction[] getJunction() {
		return junction;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationNode#getJunction(int)
	 */
	@Override
	public NodeJunction getJunction(int index) {
		return getJunction()[index];
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationNode#getReference()
	 */
	@Override
	public String getReference() {
		return reference;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#getTopology()
	 */
	@Override
	public Topology getTopology() {
		return topology;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationNode#getTrack()
	 */
	@Override
	public Track[] getTrack() {
		return track;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationNode#getTrack(int)
	 */
	@Override
	public Track getTrack(int index) {
		return getTrack()[index];
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getReference().hashCode();
	}

	/**
	 * @param topology
	 *            The topology to set.
	 */
	public void setTopology(Topology topology) {
		this.topology = topology;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationNode#setTrack(int,
	 *      org.mmarini.railways.model.elements.Track)
	 */
	@Override
	public void setTrack(int index, Track track) {
		getTrack()[index] = track;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getReference();
	}
}