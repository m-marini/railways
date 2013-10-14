package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.StationNode;

/**
 * @author $$Author: marco $$
 * @version $Id: ActiveNodeGraph.java,v 1.6 2006/09/11 11:30:22 marco Exp $
 */
public abstract class ActiveNodeGraph extends AbstractNodeGraph {
	private StationGraphEvent event = new StationGraphEvent(this);

	/**
	 * @param node
	 * @param location
	 * @param direction
	 */
	public ActiveNodeGraph(StationNode node) {
		super(node);
	}

	/**
	 * @return Returns the event.
	 */
	protected StationGraphEvent getEvent() {
		return event;
	}
}