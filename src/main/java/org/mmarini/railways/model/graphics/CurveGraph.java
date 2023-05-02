package org.mmarini.railways.model.graphics;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.mmarini.railways.model.elements.Curve;

/**
 * @author $$Author: marco $$
 * @version $Id: CurveGraph.java,v 1.8 2012/02/08 22:03:31 marco Exp $
 */
public class CurveGraph extends AbstractTrackGraph {
	/**
	 * @param track
	 */
	public CurveGraph(Curve track) {
		super(track);
		createTransformedBounds(CurvePainter.getInstance().getBounds(track));
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedTrain(java.awt.geom.Point2D)
	 */
	@Override
	public TrainGraphEvent findSelectedTrain(Point2D location) {
		Curve elem = (Curve) getTrack();
		double from = elem.getTrainHead();
		if (from < 0)
			return null;
		AffineTransform trans = getInvTransform();
		Point2D point = trans.transform(location, null);
		if (!CurvePainter.getInstance().isOverTrain(point, elem))
			return null;
		TrainGraphEvent ev = getEvent();
		ev.setLocation(location);
		ev.setTrain(elem.getTrain());
		return ev;
	}
}