package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.DeadTrackIncome;
import org.mmarini.railways.model.routes.DeadTrackOutcome;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.visitor.ElementVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: DeadTrack.java,v 1.6 2012/02/08 22:03:21 marco Exp $
 */
public class DeadTrack extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * @param reference
	 */
	public DeadTrack(String reference) {
		super(reference, 1);
		DeadTrackIncome income = new DeadTrackIncome(this);
		DeadTrackOutcome outcome = new DeadTrackOutcome(this);
		income.setOpposite(outcome);
		outcome.setOpposite(income);
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(income, outcome);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitDeadTrack(this);
	}
}