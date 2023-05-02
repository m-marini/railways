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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Vistor scan the path to set the deviation status of the deviators in the
 * path
 * 
 * @author $$Author: marco $$
 * @version $Id: RouteLockingVisitor.java,v 1.2.2.1 2005/11/07 21:37:13 marco
 *          Exp $
 */
public class RouteDeviationVisitor extends TraverseSet<StationElement>
		implements ElementVisitor {
	private static Logger log = LoggerFactory
			.getLogger(RouteDeviationVisitor.class);
	private Track from;

	/**
	 * @return Returns the from.
	 */
	private Track getFrom() {
		return from;
	}

	/**
	 * @param element
	 */
	private void parseNext(Deviator element) {
		Track next;
		if (element.isDeviated()) {
			next = element.getTrack(2);
		} else {
			next = element.getTrack(1);
		}
		next.accept(this);
	}

	/**
	 * @param from
	 *            The from to set.
	 */
	private void setFrom(Track from) {
		this.from = from;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCross(org.mmarini.railways.model.elements.Cross)
	 */
	@Override
	public void visitCross(Cross element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			log.debug("Traversing " + element + " ...");
			Track[] tracks = element.getTrack();
			Track from = getFrom();
			Track next = null;
			for (int i = 0; i < 4; ++i)
				if (from == tracks[i]) {
					next = tracks[(i + 2) % 4];
					break;
				}
			next.accept(this);
		}
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator element) {
		if (!isTraversed(element)) {
			addTraversed(element);
			log.debug("Traversing " + element + " ...");
			if (element.isBusy()) {
				/*
				 * End of deviated path
				 */
				return;
			}
			Track from = getFrom();
			Track next;
			if (from != null) {
				next = element.getNextTrack(from);
				next.accept(this);
			} else {
				next = element.getTrack(0);
				next.accept(this);
				next = element.getTrack(1);
				next.accept(this);
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
			log.debug("Traversing " + element + " ...");
			if (element.isBusy()) {
				/*
				 * End of deviated path
				 */
				return;
			}
			Track from = getFrom();
			Track[] track = element.getTrack();
			if (from == null || from == track[0]) {
				/*
				 * Activated deviator
				 */
				log.debug("Deviator " + element + " activated");
				parseNext(element);
				return;

			}
			if (from == track[1]) {
				/*
				 * Deviatore in diretta
				 */
				if (element.isDeviated()) {
					log.debug("Deviator " + element + " directed");
					element.setDeviated(false);
				}
				track[0].accept(this);
				return;
			}
			/*
			 * Deviatore in deviata
			 */
			if (!element.isDeviated()) {
				log.debug("Deviator " + element + " deviated");
				element.setDeviated(true);
			}
			track[0].accept(this);
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
		if (!isTraversed(element))
			addTraversed(element);
	}

	/**
	 * @param track
	 */
	private void visitTrack(Track track) {
		if (!isTraversed(track)) {
			addTraversed(track);
			log.debug("Traversing " + track + " ...");
			for (StationNode node : track.getNode()) {
				setFrom(track);
				node.accept(this);
			}
		}
	}
}