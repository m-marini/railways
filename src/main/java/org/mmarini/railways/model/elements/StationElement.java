package org.mmarini.railways.model.elements;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: StationElement.java,v 1.3 2012/02/08 22:03:21 marco Exp $
 */
public interface StationElement extends RailwayConstants {

	/**
	 * @param visitor
	 */
	public abstract void accept(ElementVisitor visitor);

	/**
	 * @return
	 */
	public abstract Topology getTopology();
}