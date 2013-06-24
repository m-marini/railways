package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.VirtualStation;

/**
 * @author $Author: marco $
 * @version $Id: NeighbourVisitor.java,v 1.2 2006/09/11 11:30:21 marco Exp $
 */
public interface NeighbourVisitor {

	/**
	 * @param neighbour
	 */
	public abstract void visitVirtualStation(VirtualStation neighbour);
}