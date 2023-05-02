package org.mmarini.railways.model.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $$Author: marco $$
 * @version $Id: SegmentGraph.java,v 1.8 2012/02/08 22:03:31 marco Exp $
 */
public class SegmentGraph extends AbstractTrackGraph {
	/**
	 * @param track
	 * @param location
	 * @param direction
	 */
	public SegmentGraph(Segment track) {
		super(track);
		createTransformedBounds(SegmentPainter.getInstance().getBounds(track));
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedTrain(java.awt.geom.Point2D)
	 */
	@Override
	public TrainGraphEvent findSelectedTrain(Point2D location) {
		Segment elem = (Segment) getTrack();
		double from = elem.getTrainHead();
		if (from < 0)
			return null;
		double to = elem.getTrainTail();
		AffineTransform trans = getInvTransform();
		Point2D point = trans.transform(location, null);
		if (!SegmentPainter.getInstance().isOverTrain(point, from, to))
			return null;
		TrainGraphEvent ev = getEvent();
		ev.setLocation(location);
		ev.setTrain(elem.getTrain());
		return ev;
	}
}