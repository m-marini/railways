package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.elements.Track;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractTrackGraph.java,v 1.3.10.1 2005/09/29 05:55:56 marco
 *          Exp $
 */
public abstract class AbstractTrackGraph extends TransparentGraph {
	private Track track;
	private Area area;
	private TrainGraphEvent event = new TrainGraphEvent(this);

	/**
	 * @param track
	 * @param topology
	 */
	public AbstractTrackGraph(Track track) {
		super(track.getTopology());
		this.track = track;
	}

	/**
	 * @return Returns the shape.
	 */
	protected Area getArea() {
		return area;
	}

	/**
	 * @return Returns the event.
	 */
	protected TrainGraphEvent getEvent() {
		return event;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.TransparentGraph#getTopology()
	 */
	@Override
	public Topology getTopology() {
		return getTrack().getTopology();
	}

	/**
	 * @return Returns the link.
	 */
	public Track getTrack() {
		return track;
	}

	/**
	 * @param context
	 */
	private void paint(GraphicsContext context) {
		Painter painter = context.getPainterFactory().createElement(getTrack());
		if (painter == null)
			return;
		Graphics2D gr = context.getGraphics();
		AffineTransform oldTrans = gr.getTransform();
		gr.transform(getTransform());
		painter.paint(context);
		gr.setTransform(oldTrans);
	}

	/**
	 * @param context
	 */
	@Override
	public void paintTrain(GraphicsContext context) {
		Painter painter = context.getPainterFactory().createTrainElement(
				getTrack());
		if (painter == null)
			return;
		Graphics2D gr = context.getGraphics();
		AffineTransform oldTrans = gr.getTransform();
		gr.transform(getTransform());
		painter.paint(context);
		gr.setTransform(oldTrans);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintTransitable(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paintTransitable(GraphicsContext context) {
		Track element = getTrack();
		if (!element.isBusy())
			paint(context);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintUntransitable(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paintUntransitable(GraphicsContext context) {
		Track element = getTrack();
		if (element.isBusy())
			paint(context);
	}

	/**
	 * @param shape
	 *            The shape to set.
	 */
	protected void setArea(Area shape) {
		this.area = shape;
	}
}