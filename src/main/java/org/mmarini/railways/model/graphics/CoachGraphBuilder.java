package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.routes.RoutePoint;
import org.mmarini.railways.model.train.Train;

/**
 * @author $Author: marco $
 * @version $Id: CoachGraphBuilder.java,v 1.6.16.1 2012/02/04 19:22:59 marco Exp
 *          $
 */
public class CoachGraphBuilder implements RailwayConstants, GraphicContants {
	private Train train;
	private List<CoachGraph> coachGraphs = new ArrayList<CoachGraph>();
	private RoutePoint routePoint;
	private PainterFactory painterFactory;

	/**
	 * @param train
	 * @param painterFactory
	 */
	public CoachGraphBuilder(Train train, PainterFactory painterFactory) {
		this.train = train;
		this.painterFactory = painterFactory;
	}

	/**
	 * @param hrl
	 * @param trl
	 * @return
	 */
	private Topology calculateCoachTopology(RoutePoint hrl, RoutePoint trl) {
		Point2D head = hrl.getLocation2D();
		Point2D tail = trl.getLocation2D();
		double xh = head.getX();
		double yh = head.getY();
		double xt = tail.getX();
		double yt = tail.getY();
		double alpha = Math.atan2(xh - xt, yt - yh);
		double x0 = xh + COACH_RAIL_DISTANCE * Math.sin(alpha);
		double y0 = yh - COACH_RAIL_DISTANCE * Math.cos(alpha);
		alpha = Math.toDegrees(alpha);
		if (alpha < 0)
			alpha += 360;
		return new Topology(new Point2D.Double(x0, y0), alpha);
	}

	/**
	 * @return
	 */
	private RoutePoint calculateHeadRailLocation() {
		return routePoint.calculateNextLocation(COACH_RAIL_DISTANCE);
	}

	/**
	 * @param tailRailLocation
	 * @return
	 */
	private RoutePoint calculateNextCoachLocation(RoutePoint tailRailLocation) {
		return tailRailLocation.calculateNextLocation(COACH_RAIL_DISTANCE);
	}

	/**
	 * @param headRailLocation
	 * @return
	 */
	private RoutePoint calculateTailRailLocation(RoutePoint headRailLocation) {
		return headRailLocation.calculateNextLocation(COACH_LENGTH - 2
				* COACH_RAIL_DISTANCE);
	}

	/**
	 * @return
	 */
	public void create() {
		RoutePoint point = train.getRouteLocation();
		routePoint = point;
		int n = (int) Math.round(train.getLength() / COACH_LENGTH);
		PainterFactory fact = painterFactory;
		for (int i = 0; routePoint != null && i < n; ++i) {
			RoutePoint hrl = calculateHeadRailLocation();
			if (hrl == null)
				break;
			RoutePoint trl = calculateTailRailLocation(hrl);
			if (trl == null)
				break;
			RoutePoint nextLocation = calculateNextCoachLocation(trl);
			routePoint = nextLocation;
			if (hrl.isInvisible() || trl.isInvisible())
				continue;
			Topology topology = calculateCoachTopology(hrl, trl);
			if (topology == null)
				break;
			Painter painter;
			if (i == 0) {
				painter = fact.createHead();
			} else if (i == n - 1) {
				painter = fact.createTail();
			} else {
				painter = fact.createCoach();
			}
			if (painter != null) {
				CoachGraph coach = new CoachGraph(topology, painter);
				getCoachGraphs().add(coach);
			}
		}
	}

	/**
	 * @return Returns the wagons.
	 */
	public List<CoachGraph> getCoachGraphs() {
		return coachGraphs;
	}
}