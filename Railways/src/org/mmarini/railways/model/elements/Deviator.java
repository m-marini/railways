package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.DeviatorIncome;
import org.mmarini.railways.model.routes.DeviatorOutcome;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.visitor.DeviatorLockedSetter;
import org.mmarini.railways.model.visitor.ElementVisitor;
import org.mmarini.railways.model.visitor.EndsLockedSetter;
import org.mmarini.railways.model.visitor.RouteDeviationVisitor;
import org.mmarini.railways.model.visitor.RouteLockingVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Deviator.java,v 1.9 2012/02/08 22:03:21 marco Exp $
 */
public class Deviator extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private DeviatorIncome[] income;
	private DeviatorOutcome[] outcome;
	private boolean deviated;
	private boolean locked;

	/**
	 * 
	 * @param reference
	 */
	public Deviator(String reference) {
		this(reference, false);
	}

	/**
	 * 
	 * @param reference
	 * @param deviated
	 */
	public Deviator(String reference, boolean deviated) {
		super(reference, 3);
		income = new DeviatorIncome[] { new DeviatorIncome(this),
				new DeviatorIncome(this), new DeviatorIncome(this) };
		outcome = new DeviatorOutcome[] { new DeviatorOutcome(this),
				new DeviatorOutcome(this), new DeviatorOutcome(this) };
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(income[0], outcome[0]);
		junction[1] = new NodeJunctionImpl(income[1], outcome[1]);
		junction[2] = new NodeJunctionImpl(income[2], outcome[2]);
		this.deviated = deviated;
		if (deviated)
			deviate();
		else
			direct();
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitDeviator(this);
	}

	/**
	 * 
	 */
	private void deviate() {
		income[0].setNext(outcome[2]);
		outcome[0].setPrevious(income[2]);
		income[1].setNext(null);
		outcome[1].setPrevious(null);
		income[2].setNext(outcome[0]);
		outcome[2].setPrevious(income[0]);
	}

	/**
	 * 
	 */
	private void direct() {
		income[0].setNext(outcome[1]);
		outcome[0].setPrevious(income[1]);
		income[1].setNext(outcome[0]);
		outcome[1].setPrevious(income[0]);
		income[2].setNext(null);
		outcome[2].setPrevious(null);
	}

	/**
	 * Returns true if the deviator is busy.
	 * <p>
	 * If a deviator is busy the deviated state can not be changed.
	 * </p>
	 * 
	 * @return true if the deviator is busy.
	 */
	public boolean isBusy() {
		return isTransit();
	}

	/**
	 * @return Returns the deviated.
	 */
	public boolean isDeviated() {
		return deviated;
	}

	/**
	 * @return Returns the held.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Returns true if a train is transiting in the deviator paths.
	 * 
	 * @return true if a train is transiting in the deviator paths.
	 */
	private boolean isTransit() {
		for (int i = 0; i < income.length; ++i) {
			if (income[i].isTransit())
				return true;
		}
		for (int i = 0; i < outcome.length; ++i) {
			if (outcome[i].isTransit())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * 
	 */
	public void lock() {
		if (income[0].isTransitable()) {
			EndsLockedSetter visitor = new EndsLockedSetter(true);
			getTrack(0).accept(visitor);
		} else {
			setLocked(true);
		}
	}

	/**
	 * 
	 * @param autoLock
	 */
	public void setAutoLock(boolean autoLock) {
		for (int i = 0; i < income.length; ++i)
			income[i].setAutoLock(autoLock);
	}

	/**
	 * @param deviated
	 *            The deviated to set.
	 */
	public void setDeviated(boolean deviated) {
		if (isBusy())
			throw new IllegalStateException("Deviator busy");
		this.deviated = deviated;
		if (deviated) {
			deviate();
		} else {
			direct();
		}
		if (locked) {
			lock();
		}
		/*
		 * Set the deviation status of all not busy deviators in the transitable
		 * path.
		 */
		accept(new RouteDeviationVisitor());

		if (!income[0].isTransitable())
			return;

		/*
		 * Set the locking status of all deviators in the transitable path.
		 */
		RouteLockingVisitor visitor = new RouteLockingVisitor();
		Track track = getTrack(0);
		track.accept(visitor);
		DeviatorLockedSetter setter = new DeviatorLockedSetter(
				visitor.isLocked());
		track.accept(setter);
	}

	/**
	 * @param held
	 *            The held to set.
	 */
	public void setLocked(boolean held) {
		this.locked = held;
	}

	/**
	 * 
	 * 
	 */
	public void unlock() {
		if (income[0].isTransitable()) {
			EndsLockedSetter visitor = new EndsLockedSetter(false);
			getTrack(0).accept(visitor);
		} else {
			setLocked(false);
		}
	}
}