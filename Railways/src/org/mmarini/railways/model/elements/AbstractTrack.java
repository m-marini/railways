package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.routes.TrackJunction;
import org.mmarini.railways.model.routes.TrackRoute;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractTrack.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public abstract class AbstractTrack implements Track {
	private Topology topology;
	private TrackJunction[] junction;
	private StationNode[] node;
	private double length;
	private TrackRoute[] route;

	/**
	 * @param length
	 */
	protected AbstractTrack(double length) {
		node = new StationNode[2];
		this.length = length;
		init();
	}

	/**
	 * @return Returns the junction.
	 */
	public TrackJunction[] getJunction() {
		return junction;
	}

	/**
	 * @see org.mmarini.railways.model.elements.Track#getJunction(int)
	 */
	@Override
	public TrackJunction getJunction(int index) {
		return getJunction()[index];
	}

	/**
	 * @see org.mmarini.railways.model.elements.Track#getLength()
	 */
	@Override
	public double getLength() {
		return length;
	}

	/**
	 * @see org.mmarini.railways.model.elements.Track#getNode()
	 */
	@Override
	public StationNode[] getNode() {
		return node;
	}

	/**
	 * @see org.mmarini.railways.model.elements.Track#getNode(int)
	 */
	@Override
	public StationNode getNode(int index) {
		return node[index];
	}

	/**
	 * @return Returns the route.
	 */
	private TrackRoute[] getRoute() {
		return route;
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#getTopology()
	 */
	@Override
	public Topology getTopology() {
		return topology;
	}

	/**
	 * @return
	 */
	public Train getTrain() {
		TrackRoute[] route = getRoute();
		Train train = route[0].getTrain();
		if (train != null) {
			return train;
		}
		return route[1].getTrain();
	}

	/**
	 * Get the train head position.
	 * 
	 * @return the train head position.
	 */
	public double getTrainHead() {
		TrackRoute[] route = getRoute();
		double head = route[0].getTrainHead();
		if (head < 0) {
			head = getLength() - route[1].getTrainHead();
		}
		return head;
	}

	/**
	 * Gets the train tail position.
	 * 
	 * @return the train tail position.
	 */
	public double getTrainTail() {
		TrackRoute[] route = getRoute();
		double tail = route[0].getTrainTail();
		if (tail < 0) {
			tail = getLength() - route[1].getTrainTail();
		}
		return tail;
	}

	/**
	 * 
	 * 
	 */
	protected void init() {
	}

	/**
	 * @see org.mmarini.railways.model.elements.Track#isBusy()
	 */
	@Override
	public boolean isBusy() {
		TrackRoute[] route = getRoute();
		return !route[0].isTransitable() || !route[1].isTransitable();
	}

	/**
	 * @param junction
	 *            The junction to set.
	 */
	protected void setJunction(TrackJunction[] junction) {
		this.junction = junction;
	}

	/**
	 * @param index
	 * @param node
	 */
	@Override
	public void setNode(int index, StationNode node) {
		getNode()[index] = node;
	}

	/**
	 * @param route
	 *            The route to set.
	 */
	protected void setRoute(TrackRoute[] route) {
		this.route = route;
	}

	/**
	 * @param topology
	 *            The topology to set.
	 */
	public void setTopology(Topology topology) {
		this.topology = topology;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer bfr = new StringBuffer();
		bfr.append(getNode(0));
		bfr.append("-");
		bfr.append(getNode(1));
		return bfr.toString();
	}
}