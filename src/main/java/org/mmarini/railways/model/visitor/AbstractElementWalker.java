package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.StationElement;

/**
 * This visitor scan a subset of the element of a graph.
 * 
 * @author $$Author: marco $$
 * @version $Id: AbstractElementWalker.java,v 1.2.6.1 2005/09/09 19:11:39 marco
 *          Exp $
 */
public abstract class AbstractElementWalker extends TraverseSet<StationElement>
		implements ElementVisitor {
	private ElementVisitor processVisitor;

	/**
	 * @param processVisitor
	 */
	public AbstractElementWalker(ElementVisitor processVisitor) {
		this.processVisitor = processVisitor;
	}

	/**
	 * @return Returns the processVisitor.
	 */
	private ElementVisitor getProcessVisitor() {
		return processVisitor;
	}

	/**
	 * @param elem
	 */
	protected void processNode(StationElement elem) {
		addTraversed(elem);
		elem.accept(getProcessVisitor());
	}
}