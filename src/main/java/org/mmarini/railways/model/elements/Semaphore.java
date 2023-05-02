package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.routes.SemaphoreIncome;
import org.mmarini.railways.model.routes.SemaphoreOutcome;
import org.mmarini.railways.model.visitor.DeviatorLockedSetter;
import org.mmarini.railways.model.visitor.ElementVisitor;
import org.mmarini.railways.model.visitor.RouteLockingVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Signal.java,v 1.7 2012/02/08 22:03:21 marco Exp $
 */
public class Semaphore extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private SemaphoreIncome[] income;
	private SemaphoreOutcome[] outcome;

	/**
	 * @param reference
	 */
	public Semaphore(String reference) {
		super(reference, 2);
		income = new SemaphoreIncome[2];
		outcome = new SemaphoreOutcome[2];
		outcome[0] = new SemaphoreOutcome(this);
		outcome[1] = new SemaphoreOutcome(this);
		income[0] = new SemaphoreIncome(this);
		income[0].setOpposite(outcome[0]);
		income[1] = new SemaphoreIncome(this);
		income[1].setOpposite(outcome[1]);
		income[0].setNext(outcome[1]);
		outcome[1].setPrevious(income[0]);
		outcome[1].setOpposite(income[1]);
		income[1].setNext(outcome[0]);
		outcome[0].setPrevious(income[1]);
		outcome[0].setOpposite(income[0]);

		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(income[0], outcome[0]);
		junction[1] = new NodeJunctionImpl(income[1], outcome[1]);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitSemaphore(this);
	}

	/**
	 * Returns true if a junction is busy.
	 * 
	 * @param index
	 *            the index of junction.
	 * @return true if the junction is busy.
	 */
	public boolean isBusy(int index) {
		return income[index].isBusy();
	}

	/**
	 * Returns true if a junction is held.
	 * 
	 * @param index
	 *            the index of junction.
	 * @return true if a junction is held.
	 */
	public boolean isLocked(int index) {
		return income[index].isLocked();
	}

	/**
	 * @param index
	 * @param locked
	 */
	public void setLocked(int index, boolean locked) {
		SemaphoreIncome income = this.income[index];
		income.setLocked(locked);
		RouteLockingVisitor visitor = new RouteLockingVisitor();
		Track track = getTrack(1 - index);
		track.accept(visitor);
		DeviatorLockedSetter setter = new DeviatorLockedSetter(
				visitor.isLocked());
		track.accept(setter);
	}
}