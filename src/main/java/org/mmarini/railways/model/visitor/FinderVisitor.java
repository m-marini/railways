package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Cross;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Point;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.StationNode;

/**
 * @author $$Author: marco $$
 * @version $Id: FinderVisitor.java,v 1.6 2012/02/08 22:03:25 marco Exp $
 */
public class FinderVisitor extends ElementVisitorAdapter {
	private StationNode found;
	private String reference;

	/**
	 * @param reference
	 */
	public FinderVisitor(String reference) {
		this.reference = reference;
	}

	/**
	 * @return
	 */
	public StationNode getFound() {
		return found;
	}

	/**
	 * @return Returns the reference.
	 */
	private String getReference() {
		return reference;
	}

	/**
	 * @param found
	 *            The found to set.
	 */
	private void setFound(StationNode found) {
		this.found = found;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCross(org.mmarini.railways.model.elements.Cross)
	 */
	@Override
	public void visitCross(Cross element) {
		visitNode(element);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		visitNode(deviator);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line line) {
		visitNode(line);
	}

	/**
	 * @param node
	 */
	private void visitNode(StationNode node) {
		if (getReference().equals(node.getReference()))
			setFound(node);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPoint(org.mmarini.railways.model.elements.Point)
	 */
	@Override
	public void visitPoint(Point point) {
		visitNode(point);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore semaphore) {
		visitNode(semaphore);
	}
}