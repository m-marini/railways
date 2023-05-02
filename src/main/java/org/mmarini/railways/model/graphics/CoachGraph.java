package org.mmarini.railways.model.graphics;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.Topology;

/**
 * @author $Author: marco $
 * @version $Id: CoachGraph.java,v 1.6 2012/02/08 22:03:30 marco Exp $
 */
public class CoachGraph implements RailwayConstants, GraphicContants {
	private AffineTransform transform;
	private Painter painter;

	/**
	 * @param topology
	 * @param painter
	 */
	public CoachGraph(Topology topology, Painter painter) {
		createTransform(topology);
		this.painter = painter;
	}

	/**
	 * @param topology
	 */
	private void createTransform(Topology topology) {
		Point2D location = topology.getLocation();
		transform = AffineTransform.getTranslateInstance(location.getX(),
				location.getY());
		double direction = topology.getDirection();
		transform.rotate(Math.toRadians(direction));
	}

	/**
	 * @return Returns the painter.
	 */
	private Painter getPainter() {
		return painter;
	}

	/**
	 * @return Returns the transform.
	 */
	private AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @param context
	 */
	public void paint(GraphicsContext context) {
		Graphics2D gr = context.getGraphics();
		AffineTransform oldTrans = gr.getTransform();
		gr.transform(getTransform());
		getPainter().paint(context);
		gr.setTransform(oldTrans);
	}
}