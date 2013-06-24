package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Deviator;

/**
 * 
 * @author US00852
 * @version $Id: AutoLockSetter.java,v 1.2 2006/09/21 18:15:57 marco Exp $
 */
public class AutoLockSetter extends ElementVisitorAdapter {
	private boolean autoLock;

	/**
	 * 
	 * @param autoLock
	 */
	public AutoLockSetter(boolean autoLock) {
		this.autoLock = autoLock;
	}

	/**
	 * @return the autoLock
	 */
	private boolean isAutoLock() {
		return autoLock;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		deviator.setAutoLock(isAutoLock());
	}

}
