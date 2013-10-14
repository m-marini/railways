package org.mmarini.railways.model.graphics;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.train.Train;

/**
 * @author $$Author: marco $$
 * @version $Id: StationGraph.java,v 1.9 2012/02/08 22:03:31 marco Exp $
 */
public class StationGraph implements GraphElement, GraphicContants {
	private Station station;

	private GraphList list = new GraphList();

	private Dimension size;
	private Rectangle2D bounds;
	private boolean paintLabels = true;
	private boolean paintNodes = true;

	/**
	 * @param station
	 */
	public StationGraph(Station station) {
		this.station = station;
	}

	/**
	 * @return
	 */
	private Rectangle2D createBounds() {
		Iterator<GraphElement> iter = list.iterator();
		GraphElement elem = iter.next();
		Rectangle2D bounds = (Rectangle2D) elem.getBounds().clone();
		while (iter.hasNext()) {
			elem = iter.next();
			Rectangle2D.union(bounds, elem.getBounds(), bounds);
		}
		return bounds;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedElement(java.awt.geom.Point2D)
	 */
	@Override
	public StationGraphEvent findSelectedElement(Point2D point) {
		for (GraphElement elem : list) {
			StationGraphEvent ev = elem.findSelectedElement(point);
			if (ev != null)
				return ev;
		}
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedTrain(java.awt.geom.Point2D)
	 */
	@Override
	public TrainGraphEvent findSelectedTrain(Point2D point) {
		for (GraphElement elem : list) {
			TrainGraphEvent ev = elem.findSelectedTrain(point);
			if (ev != null)
				return ev;
		}
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#getBounds()
	 */
	@Override
	public Rectangle2D getBounds() {
		Rectangle2D bounds = this.bounds;
		if (bounds == null) {
			bounds = createBounds();
			this.bounds = bounds;
		}
		return bounds;
	}

	/**
	 * @return
	 */
	public GraphList getList() {
		return list;
	}

	/**
	 * @return Returns the size.
	 */
	public Dimension getSize() {
		return size;
	}

	public Station getStation() {
		return station;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#getTopology()
	 */
	@Override
	public Topology getTopology() {
		return null;
	}

	/**
	 * @return Returns the paintLabels.
	 */
	public boolean isPaintLabels() {
		return paintLabels;
	}

	/**
	 * @return Returns the paintNodes.
	 */
	public boolean isPaintNodes() {
		return paintNodes;
	}

	/**
	 * @param ctx
	 */
	public void paintBackground(GraphicsContext ctx) {
		PainterFactory factory = ctx.getPainterFactory();
		factory.createBackground().paint(ctx);
		paintBase(ctx);
		paintUntransitable(ctx);
		paintTransitable(ctx);
		if (isPaintNodes())
			paintNodes(ctx);
		if (isPaintLabels())
			paintLabels(ctx);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintBase(GraphicsContext)
	 */
	@Override
	public void paintBase(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintBase(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintLabels(GraphicsContext)
	 */
	@Override
	public void paintLabels(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintLabels(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintNodes(GraphicsContext)
	 */
	@Override
	public void paintNodes(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintNodes(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintTrain(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paintTrain(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintTrain(context);
		}
	}

	/**
	 * @param context
	 * @param train
	 */
	public void paintTrain(GraphicsContext context, Train train) {
		CoachGraphBuilder builder = new CoachGraphBuilder(train,
				context.getPainterFactory());
		builder.create();
		for (CoachGraph wagon : builder.getCoachGraphs()) {
			wagon.paint(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintTransitable(GraphicsContext)
	 */
	@Override
	public void paintTransitable(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintTransitable(context);
		}
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintUntransitable(GraphicsContext)
	 */
	@Override
	public void paintUntransitable(GraphicsContext context) {
		for (GraphElement elem : list) {
			elem.paintUntransitable(context);
		}
	}

	/**
	 * @param paintLabels
	 *            The paintLabels to set.
	 */
	public void setPaintLabels(boolean paintLabels) {
		this.paintLabels = paintLabels;
	}

	/**
	 * @param paintNodes
	 *            The paintNodes to set.
	 */
	public void setPaintNodes(boolean paintNodes) {
		this.paintNodes = paintNodes;
	}
}