package org.mmarini.railways.model;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * @author $Author: marco $
 * @version $Id: TopologyBuilder.java,v 1.7 2012/02/08 22:03:18 marco Exp $
 */
public class TopologyBuilder implements RailwayConstants {

	private static Log log = LogFactory.getLog(TopologyBuilder.class);

	private Topology topology;
	private Queue<Topology> stack;

	/**
	 * 
	 */
	public TopologyBuilder() {
		topology = new Topology();
		stack = Collections.asLifoQueue(new LinkedList<Topology>());
	}

	/**
	 * @param element
	 * @param inverted
	 */
	public void buildCross(Cross element, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		element.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param element
	 * @param inverted
	 */
	public void buildCrossDeviator(CrossDeviator element, boolean inverted) {
		log.debug("buildCrossDeviator " + element + "," + inverted);
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		element.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param curve
	 * @param inverted
	 */
	public void buildCurve(Curve curve, boolean inverted) {
		/*
		 * Calculate end point
		 */
		double direction = getDirection();
		double angle = curve.getAngle();
		Point2D from = getLocation();
		if (inverted) {
			Point2D to = calculateEndPoint(curve.getRadius(), -angle,
					direction, from);
			curve.setTopology(new Topology(to, Normalizer.normalize(direction
					- angle + 180)));
			setLocation(to);
			setDirection(Normalizer.normalize(direction - angle));
		} else {
			Point2D to = calculateEndPoint(curve.getRadius(), angle, direction,
					from);
			curve.setTopology(new Topology(from, direction));
			setLocation(to);
			setDirection(Normalizer.normalize(direction + angle));
		}
	}

	/**
	 * @param deadtTrack
	 * @param inverted
	 */
	public void buildDeadTrack(DeadTrack deadtTrack, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		deadtTrack.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param deviator
	 * @param inverted
	 */
	public void buildDeviator(Deviator deviator, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		deviator.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param line
	 * @param inverted
	 */
	public void buildLine(Line line, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		line.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param element
	 * @param inverted
	 */
	public void buildPlatform(Platform element, boolean inverted) {
		Point2D from = getLocation();
		double direction = getDirection();
		double length = element.getLength();
		double a = Math.toRadians(direction);
		double x = from.getX();
		double y = from.getY();
		double xf = x + length * Math.sin(a);
		double yf = y - length * Math.cos(a);
		Point2D to = new Point2D.Double(xf, yf);
		Point2D location = from;
		if (inverted) {
			location = to;
			direction += 180;
		}
		element.setTopology(new Topology(location, Normalizer
				.normalize(direction)));
		setLocation(to);
	}

	/**
	 * @param point
	 * @param inverted
	 */
	public void buildPoint(Point point, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		point.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param segment
	 * @param inverted
	 */
	public void buildSegment(Segment segment, boolean inverted) {
		Point2D from = getLocation();
		double direction = getDirection();
		double length = segment.getLength();
		double a = Math.toRadians(direction);
		double x = from.getX();
		double y = from.getY();
		double xf = x + length * Math.sin(a);
		double yf = y - length * Math.cos(a);
		Point2D to = new Point2D.Double(xf, yf);
		Point2D location = from;
		if (inverted) {
			location = to;
			direction += 180;
		}
		segment.setTopology(new Topology(location, Normalizer
				.normalize(direction)));
		setLocation(to);
	}

	/**
	 * @param semaphore
	 * @param inverted
	 */
	public void buildSemaphore(Semaphore semaphore, boolean inverted) {
		double angle = getDirection();
		if (inverted)
			angle += 180;
		angle = Normalizer.normalize(angle);
		semaphore.setTopology(new Topology(getLocation(), angle));
	}

	/**
	 * @param station
	 */
	public void buildStation(Station station) {
		setDirection(station.getDirection());
	}

	/**
	 * Calculate the final point of a curve.
	 * 
	 * @param radius
	 *            the radius of curve
	 * @param angle
	 *            the angle of the curve (>0 right curve).
	 * @param direction
	 *            the direction of curve.
	 * @param from
	 *            offset point
	 * @return the final angle of the curve.
	 */
	private Point2D calculateEndPoint(double radius, double angle,
			double direction, Point2D from) {
		Point2D end = null;
		double rad = Math.toRadians(angle);
		double cos = radius * Math.cos(rad);
		double sin = radius * Math.sin(rad);
		/*
		 * Creates the end point respect point O(0, 0) and direction 360 deg
		 */
		double x;
		double y;
		if (angle > 0) {
			x = radius - cos;
			y = -sin;
		} else {
			x = cos - radius;
			y = sin;
		}
		/*
		 * Rotates the end point of direction
		 */
		rad = Math.toRadians(direction);
		cos = Math.cos(rad);
		sin = Math.sin(rad);
		double x1 = x * cos - y * sin;
		double y1 = x * sin + y * cos;
		/*
		 * Translate of the vector from
		 */
		end = new Point2D.Double(from.getX() + x1, from.getY() + y1);
		return end;
	}

	/**
	 * @return Returns the direction.
	 */
	private double getDirection() {
		return topology.getDirection();
	}

	/**
	 * @return Returns the point.
	 */
	private Point2D getLocation() {
		return topology.getLocation();
	}

	/**
	 * 
	 * 
	 */
	public void pop() {
		this.topology = stack.remove();
	}

	/**
	 * 
	 * 
	 */
	public void push() {
		stack.offer(new Topology(topology));
	}

	/**
	 * @param direction
	 *            The direction to set.
	 */
	private void setDirection(double direction) {
		topology.setDirection(direction);
	}

	/**
	 * @param point
	 *            The point to set.
	 */
	private void setLocation(Point2D point) {
		topology.setLocation(point);
	}

	/**
	 * @param angle
	 */
	public void turn(double angle) {
		setDirection(Normalizer.normalize(getDirection() + angle));
	}
}