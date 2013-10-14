package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Deviator;

public class ChangeStateVisitor extends ElementVisitorAdapter {
	private static ChangeStateVisitor instance = new ChangeStateVisitor();

	/**
	 * @return Returns the instance.
	 */
	public static ChangeStateVisitor getInstance() {
		return instance;
	}

	/**
	 * 
	 */
	private ChangeStateVisitor() {
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator deviator) {
		if (deviator.isLocked())
			deviator.unlock();
		else
			deviator.lock();
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		if (deviator.isLocked())
			deviator.unlock();
		else
			deviator.lock();
	}
}
