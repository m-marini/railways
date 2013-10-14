package org.mmarini.railways.model.elements;

import java.io.Serializable;

import org.mmarini.railways.model.routes.LineIncome;
import org.mmarini.railways.model.routes.LineJunction;
import org.mmarini.railways.model.routes.LineOutcome;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.NodeJunctionImpl;
import org.mmarini.railways.model.visitor.DeviatorLockedSetter;
import org.mmarini.railways.model.visitor.ElementVisitor;
import org.mmarini.railways.model.visitor.RouteLockingVisitor;

/**
 * @author $$Author: marco $$
 * @version $Id: Line.java,v 1.7 2012/02/08 22:03:21 marco Exp $
 */
public class Line extends AbstractNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private LineJunction lineJunction;
	private LineIncome income;
	private LineOutcome outcome;
	private Neighbour neighbour;

	/**
	 * @param reference
	 */
	public Line(String reference) {
		super(reference, 1);
		income = new LineIncome(this);
		outcome = new LineOutcome(this);
		outcome.setOpposite(income);
		income.setOpposite(outcome);
		lineJunction = new LineJunction(income, outcome);
		NodeJunction[] junction = getJunction();
		junction[0] = new NodeJunctionImpl(outcome, income);
	}

	/**
	 * @see org.mmarini.railways.model.elements.StationElement#accept(org.mmarini.railways.model.visitor.ElementVisitor)
	 */
	@Override
	public void accept(ElementVisitor visitor) {
		visitor.visitLine(this);
	}

	/**
	 * Attachs a neighbour to the line.
	 * 
	 * @param neighbour
	 *            the neighbour.
	 */
	public void attach(Neighbour neighbour) {
		getLineJunction().attach(neighbour.getJunction());
		this.neighbour = neighbour;
		neighbour.attachLine(this);
	}

	/**
	 * @return Returns the lineJunction.
	 */
	public LineJunction getLineJunction() {
		return lineJunction;
	}

	/**
	 * Gets the neightbour.
	 * 
	 * @return the neightbour..
	 */
	public Neighbour getNeighbour() {
		return neighbour;
	}

	/**
	 * Returns true if the line is busy.
	 * 
	 * @return true if the line is busy.
	 */
	public boolean isBusy() {
		return income.isBusy();
	}

	/**
	 * Returns true if a line junction is held.
	 * 
	 * @param index
	 *            the index of junction.
	 * @return true if the line junction is held.
	 */
	public boolean isHeld(int index) {
		if (index == 0) {
			return income.isHeld();
		}
		throw new IllegalArgumentException("Index " + index + " illegal");
	}

	/**
	 * Returns true if the neighbour is busy.
	 * 
	 * @return true if the neighbour is busy.
	 */
	public boolean isNeighbourBusy() {
		return this.outcome.isBusy();
	}

	/**
	 * @param index
	 * @param held
	 */
	public void setHeld(int index, boolean held) {
		if (index != 0) {
			throw new IllegalArgumentException("Index " + index + " illegal");
		}
		income.setHeld(held);
		RouteLockingVisitor visitor = new RouteLockingVisitor();
		Track track = getTrack(0);
		track.accept(visitor);
		DeviatorLockedSetter setter = new DeviatorLockedSetter(
				visitor.isLocked());
		track.accept(setter);
	}
}