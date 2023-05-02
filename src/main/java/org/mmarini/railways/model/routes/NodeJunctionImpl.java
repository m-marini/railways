package org.mmarini.railways.model.routes;

import java.io.Serializable;

/**
 * @author $$Author: marco $$
 * @version $Id: NodeJunctionImpl.java,v 1.2.16.1 2012/02/04 19:22:57 marco Exp
 *          $
 */
public class NodeJunctionImpl implements NodeJunction, Serializable {
	private static final long serialVersionUID = 1L;
	private NodeIncome income;
	private NodeOutcome outcome;

	/**
	 * @param income
	 * @param outcome
	 */
	public NodeJunctionImpl(NodeIncome income, NodeOutcome outcome) {
		this.income = income;
		this.outcome = outcome;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeJunction#attach(org.mmarini.railways.model.routes.TrackJunction)
	 */
	@Override
	public void attach(TrackJunction segJunction) {
		getIncome().attach(segJunction.getOutcome());
		getOutcome().attach(segJunction.getIncome());
		segJunction.attach(this);
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeJunction#getIncome()
	 */
	@Override
	public NodeIncome getIncome() {
		return income;
	}

	/**
	 * @see org.mmarini.railways.model.routes.NodeJunction#getOutcome()
	 */
	@Override
	public NodeOutcome getOutcome() {
		return outcome;
	}
}