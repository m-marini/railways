package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Deviator;

/**
 * 
 * @author US00852
 * @version $Id: LockAllVisitor.java,v 1.4 2012/02/08 22:03:25 marco Exp $
 */
public class LockAllVisitor extends ElementVisitorAdapter {

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		deviator.lock();
		super.visitDeviator(deviator);
	}

}
