package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.DeadTrack;

/**
 * @author $$Author: marco $$
 * @version $Id: DeadTrackGraph.java,v 1.2 2005/11/15 21:43:26 marco Exp $
 */
public class DeadTrackGraph extends AbstractNodeGraph {
	/**
	 * @param node
	 */
	public DeadTrackGraph(DeadTrack node) {
		super(node);
		createTransformedBounds(DeadTrackImgPainter.getInstance().getBounds());
	}
}