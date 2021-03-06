package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Cross;
import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Point;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.elements.StationNode;
import org.mmarini.railways.model.elements.Track;

/**
 * This visitor scans all the element of a route setting the locked status of
 * the deviators.
 * 
 * @author $$Author: marco $$
 * @version $Id: DeviatorLockedSetter.java,v 1.2.2.1 2005/11/07 21:37:13 marco
 *          Exp $
 */
public class DeviatorLockedSetter extends TraverseSet<StationElement> implements
		ElementVisitor {
	private boolean locked;

	/**
	 * @param locked
	 */
	public DeviatorLockedSetter(boolean locked) {
		this.locked = locked;
	}

	/**
	 * @return Returns the locked.
	 */
	private boolean isLocked() {
		return locked;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCross(org.mmarini.railways.model.elements.Cross)
	 */
	@Override
	public void visitCross(Cross element) {
		visitNode(element);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			element.setLocked(isLocked());
			for (Track track : element.getTrack()) {
				track.accept(this);
			}
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve curve) {
		visitTrack(curve);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeadTrack(org.mmarini.railways.model.elements.DeadTrack)
	 */
	@Override
	public void visitDeadTrack(DeadTrack element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			element.getTrack()[0].accept(this);
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			element.setLocked(isLocked());
			Track[] track = element.getTrack();
			track[0].accept(this);
			if (element.isDeviated()) {
				track[2].accept(this);
			} else {
				track[1].accept(this);
			}
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			element.getTrack()[0].accept(this);
		}
	}

	/**
	 * @param node
	 */
	private void visitNode(StationNode node) {
		if (!isTraversed(node)) {
			addTraversed(node);
			for (Track track : node.getTrack()) {
				track.accept(this);
			}
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform platform) {
		visitTrack(platform);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPoint(org.mmarini.railways.model.elements.Point)
	 */
	@Override
	public void visitPoint(Point element) {
		visitNode(element);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment segment) {
		visitTrack(segment);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore element) {
		if (!isTraversed(element)) {
			addTraversed(element);
		}
	}

	/**
	 * @param track
	 */
	private void visitTrack(Track track) {
		if (!isTraversed(track)) {
			addTraversed(track);
			if (!track.getJunction(0).getIncome().isTransitable()
					|| !track.getJunction(1).getIncome().isTransitable())
				return;
			for (StationNode node : track.getNode()) {
				node.accept(this);
			}
		}
	}
}