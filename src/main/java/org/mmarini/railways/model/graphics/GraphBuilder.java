package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.visitor.ElementVisitorAdapter;

/**
 * @author $$Author: marco $$
 * @version $Id: GraphBuilder.java,v 1.10 2012/02/08 22:03:31 marco Exp $
 */
public class GraphBuilder extends ElementVisitorAdapter {
	private StationGraph stationGraph;

	/**
	 * @param station
	 */
	public void buildStation(Station station) {
		StationGraph stationGraph = new StationGraph(station);
		this.stationGraph = stationGraph;
	}

	/**
	 * @return
	 */
	private GraphList getList() {
		return stationGraph.getList();
	}

	/**
	 * @return
	 */
	public StationGraph getStationGraph() {
		return stationGraph;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.IGraphBuilder#pop()
	 */
	public void pop() {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.IGraphBuilder#push()
	 */
	public void push() {
	}

	/**
	 * @see org.mmarini.railways.model.graphics.IGraphBuilder#turn(double)
	 */
	public void turn(double angle) {
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator deviator) {
		CrossDeviatorGraph graph = new CrossDeviatorGraph(deviator);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve curve) {
		CurveGraph graph = new CurveGraph(curve);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeadTrack(org.mmarini.railways.model.elements.DeadTrack)
	 */
	@Override
	public void visitDeadTrack(DeadTrack track) {
		DeadTrackGraph graph = new DeadTrackGraph(track);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		DeviatorGraph graph = new DeviatorGraph(deviator);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line line) {
		LineGraph lineGraph = new LineGraph(line);
		getList().add(lineGraph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform element) {
		SegmentGraph graph = new SegmentGraph(element);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment segment) {
		SegmentGraph graph = new SegmentGraph(segment);
		getList().add(graph);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore semaphore) {
		SemaphoreGraph graph = new SemaphoreGraph(semaphore);
		getList().add(graph);
	}
}