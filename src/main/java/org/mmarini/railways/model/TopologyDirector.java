package org.mmarini.railways.model;

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
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.elements.StationNode;
import org.mmarini.railways.model.elements.Track;
import org.mmarini.railways.model.visitor.ElementVisitor;
import org.mmarini.railways.model.visitor.TraverseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $$Author: marco $$
 * @version $Id: TopologyDirector.java,v 1.7.10.1 2012/02/04 19:22:56 marco Exp
 *          $
 */
public class TopologyDirector extends TraverseSet<StationElement> implements
		ElementVisitor {
	private static Logger log = LoggerFactory.getLogger(TopologyDirector.class);

	private Station station;
	private TopologyBuilder builder;
	private StationElement previous;

	/**
	 * @param builder
	 * @param station
	 */
	public TopologyDirector(TopologyBuilder builder, Station station) {
		this.builder = builder;
		this.station = station;
	}

	/**
	 * 
	 * 
	 */
	public void create() {
		builder.buildStation(station);
		StationNode node = station.getReference();
		previous = null;
		node.accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCross(org.mmarini.railways.model.elements.Cross)
	 */
	@Override
	public void visitCross(Cross element) {
		if (isTraversed(element))
			return;
		addTraversed(element);
		builder.buildCross(element, false);
		StationElement from = previous;
		int fromIdx = 0;
		for (int i = 0; i < 4; ++i) {
			Track track = element.getTrack(i);
			if (track == from) {
				fromIdx = i;
				break;
			}
		}
		/*
		 * Visit opposite route
		 */
		int opposite = (fromIdx + 2) % 4;
		builder.push();
		Track track = element.getTrack(opposite);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("No track " + opposite + " for "
					+ element.toString() + " ");
		track.accept(this);
		builder.pop();

		/*
		 * Visit right route
		 */
		int next = (fromIdx + 1) % 4;
		double[] angleTable = new double[2];
		angleTable[0] = 180 - element.getAngle();
		angleTable[1] = element.getAngle();
		double turn = angleTable[fromIdx % 2];
		builder.turn(turn);
		builder.push();
		track = element.getTrack(next);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("No track " + next + " for "
					+ element.toString() + " ");
		track.accept(this);

		/*
		 * Visit left route
		 */
		opposite = (next + 2) % 4;
		builder.pop();
		builder.turn(180);
		track = element.getTrack(opposite);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("No track " + opposite + " for "
					+ element.toString() + " ");
		track.accept(this);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator element) {
		if (isTraversed(element))
			return;
		log.debug("visitCrossDeviator " + element);
		addTraversed(element);
		builder.buildCrossDeviator(element, false);
		StationElement from = previous;
		int fromIdx = 0;
		for (int i = 0; i < 4; ++i) {
			Track track = element.getTrack(i);
			if (track == from) {
				fromIdx = i;
				break;
			}
		}
		/*
		 * Visit opposite route
		 */
		int opposite = (fromIdx + 2) % 4;
		builder.push();
		Track track = element.getTrack(opposite);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("Track " + opposite
					+ " not found in " + element.toString());
		track.accept(this);
		builder.pop();

		/*
		 * Visit right route
		 */
		int next = (fromIdx + 1) % 4;
		double[] angleTable = new double[2];
		angleTable[0] = 180 - element.getAngle();
		angleTable[1] = element.getAngle();
		double turn = angleTable[fromIdx % 2];
		builder.turn(turn);
		builder.push();
		track = element.getTrack(next);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("Track " + next + " not found in "
					+ element.toString());
		track.accept(this);

		/*
		 * Visit left route
		 */
		opposite = (next + 2) % 4;
		builder.pop();
		builder.turn(180);
		track = element.getTrack(opposite);
		this.previous = element;
		if (track == null)
			throw new IllegalStateException("Track " + opposite
					+ " not found in " + element.toString());
		track.accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve element) {
		if (isTraversed(element))
			return;
		log.debug("visitCurve " + element);
		addTraversed(element);
		/*
		 * Determine the orientation
		 */
		StationNode node0 = element.getNode(0);
		StationElement from = previous;
		boolean inverted = node0 != from;
		builder.buildCurve(element, inverted);
		this.previous = element;
		element.getNode(0).accept(this);
		this.previous = element;
		element.getNode(1).accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeadTrack(org.mmarini.railways.model.elements.DeadTrack)
	 */
	@Override
	public void visitDeadTrack(DeadTrack element) {
		if (isTraversed(element))
			return;
		addTraversed(element);
		Track track = element.getTrack(0);
		/*
		 * Determine the orientation
		 */
		StationElement from = previous;
		boolean inverted = track == from;
		builder.buildDeadTrack(element, inverted);
		this.previous = element;
		track.accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator element) {
		if (isTraversed(element))
			return;
		addTraversed(element);
		Track income = element.getTrack(0);
		Track direct = element.getTrack(1);
		Track deviation = element.getTrack(2);
		StationElement from = previous;
		if (income == from) {
			builder.buildDeviator(element, false);
			builder.push();
			this.previous = element;
			direct.accept(this);
			builder.pop();
			this.previous = element;
			deviation.accept(this);
		} else if (direct == from) {
			builder.buildDeviator(element, true);
			builder.push();
			this.previous = element;
			income.accept(this);
			builder.pop();
			builder.turn(180);
			this.previous = element;
			deviation.accept(this);
		} else {
			builder.buildDeviator(element, true);
			builder.push();
			this.previous = element;
			income.accept(this);
			builder.pop();
			builder.turn(180);
			this.previous = element;
			direct.accept(this);
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line element) {
		if (isTraversed(element))
			return;
		log.debug("visitLine " + element);
		addTraversed(element);
		Track track = element.getTrack(0);
		/*
		 * Determine the orientation
		 */
		StationElement from = previous;
		boolean inverted = track == from;
		builder.buildLine(element, inverted);
		this.previous = element;
		track.accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform element) {
		if (isTraversed(element))
			return;
		log.debug("visitPlatform " + element);
		addTraversed(element);
		StationNode node0 = element.getNode(0);
		StationElement from = previous;
		boolean inverted = node0 != from;
		builder.buildPlatform(element, inverted);
		this.previous = element;
		element.getNode(0).accept(this);
		this.previous = element;
		element.getNode(1).accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPoint(org.mmarini.railways.model.elements.Point)
	 */
	@Override
	public void visitPoint(Point element) {
		if (isTraversed(element))
			return;
		addTraversed(element);
		Track track = element.getTrack(0);
		StationElement from = previous;
		if (track != from) {
			builder.buildPoint(element, true);
			this.previous = element;
			if (track == null)
				throw new IllegalStateException("Track 1 not found in "
						+ element.toString());
			track.accept(this);
		} else {
			builder.buildPoint(element, false);
			this.previous = element;
			track = element.getTrack(1);
			if (track == null)
				throw new IllegalStateException("Track 1 not found in "
						+ element.toString());
			track.accept(this);
		}
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment element) {
		if (isTraversed(element))
			return;
		log.debug("visitSegment " + element);
		addTraversed(element);
		StationNode node0 = element.getNode(0);
		StationElement from = previous;
		boolean inverted = node0 != from;
		builder.buildSegment(element, inverted);
		this.previous = element;
		element.getNode(0).accept(this);
		this.previous = element;
		element.getNode(1).accept(this);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore element) {
		if (isTraversed(element))
			return;
		addTraversed(element);
		Track track = element.getTrack(0);
		StationElement from = previous;
		if (track == from) {
			/*
			 * direct orientation
			 */
			builder.buildSemaphore(element, false);
			this.previous = element;
			element.getTrack(1).accept(this);
		} else {
			/*
			 * inverted orientation
			 */
			builder.buildSemaphore(element, true);
			this.previous = element;
			track.accept(this);
		}

	}
}