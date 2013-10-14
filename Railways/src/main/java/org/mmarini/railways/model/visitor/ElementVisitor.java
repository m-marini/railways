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

/**
 * @author $$Author: marco $$
 * @version $Id: ElementVisitor.java,v 1.4 2012/02/08 22:03:25 marco Exp $
 */
public interface ElementVisitor {

	/**
	 * @param element
	 */
	public abstract void visitCross(Cross element);

	/**
	 * 
	 * @param deviator
	 */
	public abstract void visitCrossDeviator(CrossDeviator deviator);

	/**
	 * @param element
	 */
	public abstract void visitCurve(Curve element);

	/**
	 * @param track
	 */
	public abstract void visitDeadTrack(DeadTrack track);

	/**
	 * @param element
	 */
	public abstract void visitDeviator(Deviator element);

	/**
	 * @param element
	 */
	public abstract void visitLine(Line element);

	/**
	 * @param platform
	 */
	public abstract void visitPlatform(Platform platform);

	/**
	 * @param element
	 */
	public abstract void visitPoint(Point element);

	/**
	 * @param element
	 */
	public abstract void visitSegment(Segment element);

	/**
	 * @param element
	 */
	public abstract void visitSemaphore(Semaphore element);
}