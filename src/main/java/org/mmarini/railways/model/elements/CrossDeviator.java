package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.CrossDeviatorIncome;
import org.mmarini.railways.model.routes.CrossDeviatorOutcome;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.visitor.DeviatorLockedSetter;
import org.mmarini.railways.model.visitor.ElementVisitor;
import org.mmarini.railways.model.visitor.EndsLockedSetter;
import org.mmarini.railways.model.visitor.RouteDeviationVisitor;
import org.mmarini.railways.model.visitor.RouteLockingVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: CrossDeviator.java,v 1.3 2012/02/08 22:03:21 marco Exp $
 */
public class CrossDeviator extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private CrossDeviatorIncome[] income;
	private CrossDeviatorOutcome[] outcome;
	private boolean deviated;
	private boolean locked;
	private double angle;

	/**
	 * 
	 * @param reference
	 */
	public CrossDeviator(String reference) {
		this(reference, false, DEFAULT_CROSS_ANGLE);
	}

	/**
	 * 
	 * @param reference
	 * @param deviated
	 * @param angle
	 */
	public CrossDeviator(String reference, boolean deviated, double angle) {
		super(reference, 4);
		this.angle = angle;
		income = new CrossDeviatorIncome[] { new CrossDeviatorIncome(this),
				new CrossDeviatorIncome(this), new CrossDeviatorIncome(this),
				new CrossDeviatorIncome(this) };
		outcome = new CrossDeviatorOutcome[] { new CrossDeviatorOutcome(this),
				new CrossDeviatorOutcome(this), new CrossDeviatorOutcome(this),
				new CrossDeviatorOutcome(this) };
		income[0].setLateral(income[1]);
		income[1].setLateral(income[2]);
		income[2].setLateral(income[3]);
		income[3].setLateral(income[0]);
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(income[0], outcome[0]);
		junction[1] = new NodeJunctionImpl(income[1], outcome[1]);
		junction[2] = new NodeJunctionImpl(income[2], outcome[2]);
		junction[3] = new NodeJunctionImpl(income[3], outcome[3]);
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
		visitor.visitCrossDeviator(this);
	}

	/**
	 * 
	 */
	private void deviate() {
		income[0].setNext(outcome[3]);
		outcome[0].setPrevious(income[3]);
		income[0].setLateral(income[1]);

		income[1].setNext(outcome[2]);
		outcome[1].setPrevious(income[2]);
		income[1].setLateral(income[3]);

		income[2].setNext(outcome[1]);
		outcome[2].setPrevious(income[1]);
		income[2].setLateral(income[0]);

		income[3].setNext(outcome[0]);
		outcome[3].setPrevious(income[0]);
		income[3].setLateral(income[2]);
	}

	/**
	 * 
	 */
	private void direct() {
		income[0].setNext(outcome[2]);
		income[0].setLateral(income[1]);
		outcome[0].setPrevious(income[2]);

		income[1].setNext(outcome[3]);
		outcome[1].setPrevious(income[3]);
		income[1].setLateral(income[2]);

		income[2].setNext(outcome[0]);
		outcome[2].setPrevious(income[0]);
		income[2].setLateral(income[3]);

		income[3].setNext(outcome[1]);
		outcome[3].setPrevious(income[1]);
		income[3].setLateral(income[0]);
	}

	/**
	 * @return the angle
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * 
	 * @param from
	 * @return
	 */
	public Track getNextTrack(Track from) {
		for (int i = 0; i < 4; ++i)
			if (from == getTrack(i)) {
				if (deviated) {
					switch (i) {
					case 0:
						return getTrack(3);
					case 1:
						return getTrack(2);
					case 2:
						return getTrack(1);
					case 3:
						return getTrack(0);
					default:
						return null;
					}
				} else {
					return getTrack((i + 2) % 4);
				}
			}
		return null;
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
	 */
	public void lock() {
		EndsLockedSetter visitor = new EndsLockedSetter(true);
		if (income[0].isTransitable()) {
			getTrack(0).accept(visitor);
		}
		if (income[1].isTransitable()) {
			getTrack(1).accept(visitor);
		}
		setLocked(true);
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

		if (!(income[0].isTransitable() && income[1].isTransitable()))
			return;

		/*
		 * Set the locking status of all deviators in the transitable path.
		 */
		RouteLockingVisitor visitor = new RouteLockingVisitor();
		Track track0 = getTrack(0);
		track0.accept(visitor);
		Track track1 = getTrack(1);
		track0.accept(visitor);
		DeviatorLockedSetter setter = new DeviatorLockedSetter(
				visitor.isLocked());
		track0.accept(setter);
		track1.accept(setter);
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
	 */
	public void unlock() {
		EndsLockedSetter visitor = new EndsLockedSetter(false);
		if (income[0].isTransitable()) {
			getTrack(0).accept(visitor);
		}
		if (income[1].isTransitable()) {
			getTrack(1).accept(visitor);
		}
		setLocked(false);
	}
}