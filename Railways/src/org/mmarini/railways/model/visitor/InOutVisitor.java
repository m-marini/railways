package org.mmarini.railways.model.visitor;

import java.util.ArrayList;
import java.util.List;

import org.mmarini.railways.model.elements.VirtualStation;

/**
 * 
 * @author us00852
 * @version $Id: FrequenceSetterVisitor.java,v 1.1.2.1 2006/08/23 21:45:31 marco
 *          Exp $
 */
public class InOutVisitor implements NeighbourVisitor {
	private List<VirtualStation> income = new ArrayList<VirtualStation>();
	private List<VirtualStation> outcome = new ArrayList<VirtualStation>();

	/**
	 * 
	 * @return
	 */
	public List<VirtualStation> getIncome() {
		return income;
	}

	/**
	 * 
	 * @return
	 */
	public List<VirtualStation> getOutcome() {
		return outcome;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.visitor.NeighbourVisitor#visitVirtualStation(org.mmarini.railways.model.elements.VirtualStation)
	 */
	@Override
	public void visitVirtualStation(VirtualStation neighbour) {
		if (neighbour.isOutcome()) {
			getOutcome().add(neighbour);
		} else {
			getIncome().add(neighbour);
		}
	}
}
