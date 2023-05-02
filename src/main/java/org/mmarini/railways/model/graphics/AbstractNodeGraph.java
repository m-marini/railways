package org.mmarini.railways.model.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mmarini.railways.model.Topology;
import org.mmarini.railways.model.elements.StationNode;

/**
 * @author $$Author: marco $$
 * @version $Id: AbstractNodeGraph.java,v 1.1.2.1.2.1 2005/08/18 20:08:21 marco
 *          Exp $
 */
public abstract class AbstractNodeGraph extends TransparentGraph {
	private StationNode node;
	private Font font;
	private Rectangle2D labelBounds;

	/**
	 * @param node
	 */
	public AbstractNodeGraph(StationNode node) {
		super(node.getTopology());
		this.node = node;
		font = Font.decode("dialog bold");
	}

	/**
	 * @return
	 */
	private Rectangle2D createLabelBounds() {
		String label = getLabel();
		FontRenderContext ctx = new FontRenderContext(new AffineTransform(),
				false, false);
		Rectangle2D bounds = font.getStringBounds(label, ctx);
		double cx = bounds.getCenterX();
		double cy = bounds.getCenterY();
		double x = bounds.getX();
		double y = bounds.getY();
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		bounds.setRect(x - cx, y - cy, w, h);
		return bounds;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#findSelectedTrain(java.awt.geom.Point2D)
	 */
	@Override
	public TrainGraphEvent findSelectedTrain(Point2D location) {
		return null;
	}

	/**
	 * Gets the label to paint.
	 * 
	 * @return the label to paint.
	 */
	public String getLabel() {
		return getNode().getReference();
	}

	/**
	 * Return labels bounds.
	 * 
	 * @return labels bounds.
	 */
	private Rectangle2D getLabelBounds() {
		if (labelBounds == null)
			labelBounds = createLabelBounds();
		return labelBounds;
	}

	/**
	 * @return Returns the node.
	 */
	public StationNode getNode() {
		return node;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#getTopology()
	 */
	@Override
	public Topology getTopology() {
		return getNode().getTopology();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintLabels(GraphicsContext)
	 */
	@Override
	public void paintLabels(GraphicsContext context) {
		Graphics2D gr = context.getGraphics();
		AffineTransform trans = gr.getTransform();
		Rectangle2D bounds = getLabelBounds();
		gr.setTransform(AffineTransform.getTranslateInstance(bounds.getX(),
				bounds.getY()));
		Point2D pt = trans.transform(getTopology().getLocation(), null);
		gr.setFont(font);
		gr.setColor(Color.BLACK);
		String label = getLabel();
		gr.drawString(label, Math.round(pt.getX()), Math.round(pt.getY()));
		gr.setTransform(trans);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.GraphElement#paintNodes(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paintNodes(GraphicsContext context) {
		Graphics2D gr = context.getGraphics();
		AffineTransform oldTrans = gr.getTransform();
		gr.transform(getTransform());
		Painter painter = context.getPainterFactory().createElement(getNode());
		if (painter != null)
			painter.paint(context);
		gr.setTransform(oldTrans);
	}
}