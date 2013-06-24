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
import org.mmarini.railways.model.elements.Track;
import org.mmarini.railways.model.routes.NodeJunction;
import org.mmarini.railways.model.routes.TrackJunction;
import org.mmarini.railways.model.routes.TrainHead;

/**
 * @author $Author: marco $
 * @version $Id: ElementFinderByRoute.java,v 1.2.4.1 2005/09/08 22:25:49 marco
 *          Exp $
 */
public class ElementFinderByRoute implements ElementVisitor {
	private TrainHead head;
	private StationElement found;

	/**
	 * 
	 */
	public ElementFinderByRoute() {
	}

	/**
	 * @param target
	 */
	public ElementFinderByRoute(TrainHead target) {
	}

	/**
	 * @param element
	 * @param junction
	 */
	private void checksForRoute(StationElement element, NodeJunction junction) {
		TrainHead head = getHead();
		if (junction.getIncome().equals(head)
				|| junction.getOutcome().equals(head))
			setFound(element);
	}

	/**
	 * @param element
	 * @param junction
	 */
	private void checksForRoute(Track element, TrackJunction junction) {
		TrainHead head = getHead();
		if (junction.getIncome().equals(head)
				|| junction.getOutcome().equals(head))
			setFound(element);
	}

	/**
	 * @return Returns the found.
	 */
	public StationElement getFound() {
		return found;
	}

	/**
	 * @return Returns the head.
	 */
	private TrainHead getHead() {
		return head;
	}

	/**
	 * @param found
	 *            The found to set.
	 */
	private void setFound(StationElement found) {
		this.found = found;
	}

	/**
	 * @param head
	 *            The head to set.
	 */
	public void setHead(TrainHead head) {
		this.head = head;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCross(org.mmarini.railways.model.elements.Cross)
	 */
	@Override
	public void visitCross(Cross element) {
		for (int i = 0; i < 4; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator element) {
		for (int i = 0; i < 4; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve element) {
		for (int i = 0; i < 2; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeadTrack(org.mmarini.railways.model.elements.DeadTrack)
	 */
	@Override
	public void visitDeadTrack(DeadTrack deadTrack) {
		checksForRoute(deadTrack, deadTrack.getJunction(0));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator element) {
		for (int i = 0; i < 3; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line line) {
		checksForRoute(line, line.getJunction(0));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform element) {
		for (int i = 0; i < 2; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPoint(org.mmarini.railways.model.elements.Point)
	 */
	@Override
	public void visitPoint(Point element) {
		for (int i = 0; i < 2; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment element) {
		for (int i = 0; i < 2; ++i) {
			checksForRoute(element, element.getJunction(i));
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore semaphore) {
		for (int i = 0; i < 2; ++i) {
			checksForRoute(semaphore, semaphore.getJunction(i));
		}
	}
}