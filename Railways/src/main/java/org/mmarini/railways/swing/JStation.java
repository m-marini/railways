package org.mmarini.railways.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.graphics.GraphBuilder;
import org.mmarini.railways.model.graphics.GraphicContants;
import org.mmarini.railways.model.graphics.GraphicsContext;
import org.mmarini.railways.model.graphics.ImagePainterFactory;
import org.mmarini.railways.model.graphics.PainterFactory;
import org.mmarini.railways.model.graphics.StationGraph;
import org.mmarini.railways.model.graphics.StationGraphEvent;
import org.mmarini.railways.model.graphics.StationPainter;
import org.mmarini.railways.model.graphics.TrainGraphEvent;
import org.mmarini.railways.model.visitor.ElementWalker;

/**
 * @author $Author: marco $
 * @version $Id: JStation.java,v 1.10 2012/02/08 22:03:31 marco Exp $
 */
public class JStation extends JComponent implements Scrollable, GraphicContants {
	private static final long serialVersionUID = 1L;

	private StationGraph graph;
	private double scale;
	private boolean paintLabel;
	private boolean paintNodes;
	private Point mouseOffset;
	private Point lastViewLocation;
	private Dimension lastViewSize;
	private StationGraphEvent event;
	private List<StationGraphListener> elementListeners;
	private List<TrainGraphListener> trainListeners;
	private StationPainter painter;

	/**
	 * 
	 */
	public JStation() {
		this(ImagePainterFactory.getInstance());
	}

	/**
	 * @param painterFactory
	 */
	public JStation(PainterFactory painterFactory) {
		this.painter = new StationPainter(painterFactory);
		event = new StationGraphEvent(this);
		scale = DEFAULT_SCALE;
		paintLabel = true;
		paintNodes = true;
		setDoubleBuffered(true);
		setAutoscrolls(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleMouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent ev) {
				handleMousePressed(ev);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent ev) {
				handleMouseDragged(ev);
			}
		});
	}

	/**
	 * @param l
	 */
	public synchronized void addStationGraphListener(StationGraphListener l) {
		List<StationGraphListener> list = elementListeners;
		if (list == null) {
			list = new ArrayList<StationGraphListener>(1);
		} else if (list.contains(l)) {
			return;
		} else {
			list = new ArrayList<StationGraphListener>(list);
		}
		list.add(l);
		this.elementListeners = list;
	}

	/**
	 * @param l
	 */
	public synchronized void addTrainGraphListener(TrainGraphListener l) {
		List<TrainGraphListener> list = trainListeners;
		if (list == null) {
			list = new ArrayList<TrainGraphListener>(1);
		} else if (list.contains(l)) {
			return;
		} else {
			list = new ArrayList<TrainGraphListener>(list);
		}
		list.add(l);
		this.trainListeners = list;
	}

	/**
	 * @param gr
	 * @return
	 */
	protected GraphicsContext createGraphicsContext(Graphics2D gr) {
		GraphicsContext ctx = new GraphicsContext();
		ctx.setGraphics(gr);
		return ctx;
	}

	/**
	 * Create the transformation.
	 * 
	 * @return the transformation.
	 */
	protected AffineTransform createTransform() {
		StationGraph stat = getGraph();
		if (stat == null)
			return new AffineTransform();
		Dimension size = getSize();
		Rectangle2D bounds = graph.getBounds();
		AffineTransform transform = AffineTransform.getTranslateInstance(
				size.getWidth() / 2., size.getHeight() / 2.);
		double sx = size.getWidth() / bounds.getWidth();
		double sy = size.getHeight() / bounds.getHeight();
		transform.scale(sx, sy);
		transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
		return transform;
	}

	/**
	 * @param ev
	 */
	protected void fireElementSelected(StationGraphEvent ev) {
		List<StationGraphListener> list = elementListeners;
		if (list == null) {
			return;
		}
		for (StationGraphListener l : list) {
			l.elementSelected(ev);
		}
	}

	/**
	 * @param ev
	 */
	protected void fireElementStateChanged(StationGraphEvent ev) {
		List<StationGraphListener> list = elementListeners;
		if (list == null) {
			return;
		}
		for (StationGraphListener l : list) {
			l.elementStateChanged(ev);
		}
	}

	/**
	 * @param ev
	 */
	protected void firePointSelected(StationGraphEvent ev) {
		List<StationGraphListener> list = elementListeners;
		if (list == null) {
			return;
		}
		for (StationGraphListener l : list) {
			l.pointSelected(ev);
		}
	}

	/**
	 * @param ev
	 */
	protected void fireTrainSelected(TrainGraphEvent ev) {
		List<TrainGraphListener> list = trainListeners;
		if (list == null) {
			return;
		}
		for (TrainGraphListener l : list) {
			l.trainSelected(ev);
		}
	}

	/**
	 * @param ev
	 */
	protected void fireTrainStateChanged(TrainGraphEvent ev) {
		List<TrainGraphListener> list = trainListeners;
		if (list == null) {
			return;
		}
		for (TrainGraphListener l : list) {
			l.trainStateChanged(ev);
		}
	}

	/**
	 * @return
	 */
	private Rectangle getChildBounds() {
		Insets in = getInsets();
		if (in == null)
			in = new Insets(0, 0, 0, 0);
		Dimension size = this.getSize();
		return new Rectangle(in.left, in.top, size.width - in.left - in.right,
				size.height - in.top - in.bottom);
	}

	/**
	 * @return Returns the graph.
	 */
	public StationGraph getGraph() {
		return graph;
	}

	/**
	 * Gets the logical location (staion coordinate system) of a phisical point.
	 * 
	 * @param phisicalLocation
	 *            the phisical point.
	 * @returnthe logical location.
	 */
	public Point2D getLogicalLocation(Point2D phisicalLocation) {
		AffineTransform trans = getTransform();
		try {
			trans = trans.createInverse();
		} catch (NoninvertibleTransformException e) {
			return null;
		}
		return trans.transform(phisicalLocation, null);
	}

	/**
	 * @param location
	 * @return
	 */
	public Point2D getPhisicalLocation(Point2D location) {
		AffineTransform trans = getTransform();
		return trans.transform(location, null);
	}

	/**
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * @see java.awt.Component#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		StationGraph stat = getGraph();
		if (stat == null)
			return new Dimension();
		Rectangle2D bounds = stat.getBounds();
		int w = (int) Math.round(bounds.getWidth() * getScale());
		int h = (int) Math.round(bounds.getHeight() * getScale());
		return new Dimension(w, h);
	}

	/**
	 * @return Returns the scale.
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,
	 *      int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (direction > 0) {
			if (orientation == SwingConstants.VERTICAL) {
				return Math.min(visibleRect.y, visibleRect.height);
			}
			return Math.min(visibleRect.x, visibleRect.width);
		}
		Dimension size = getPreferredScrollableViewportSize();
		if (orientation == SwingConstants.VERTICAL) {
			return Math.min(size.height - visibleRect.y, visibleRect.height);
		}
		return Math.min(size.width - visibleRect.x, visibleRect.width);
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/**
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,
	 *      int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 1;
	}

	/**
	 * @return
	 */
	private AffineTransform getTransform() {
		return createTransform();
	}

	/**
	 * @param e
	 */
	private void handleMouseClicked(MouseEvent e) {
		if (isEnabled()) {
			StationGraph graph = getGraph();
			if (graph == null)
				return;
			Point2D point = getLogicalLocation(e.getPoint());
			if (point == null)
				return;
			StationGraphEvent ev = event;
			ev.setLocation(point);
			firePointSelected(ev);

			ev = graph.findSelectedElement(point);
			if (ev != null) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					fireElementSelected(ev);
				} else {
					fireElementStateChanged(ev);
				}
				return;
			}
			TrainGraphEvent tev = graph.findSelectedTrain(point);
			if (tev != null) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					fireTrainSelected(tev);
				} else {
					fireTrainStateChanged(tev);
				}
				return;
			}
		}
	}

	/**
	 * @param ev
	 */
	private void handleMouseDragged(MouseEvent ev) {
		if (mouseOffset == null)
			return;
		int dx = -ev.getX() + mouseOffset.x;
		int dy = -ev.getY() + mouseOffset.y;
		lastViewLocation.x += dx;
		lastViewLocation.y += dy;
		Rectangle r = new Rectangle(lastViewLocation.x, lastViewLocation.y,
				lastViewSize.width, lastViewSize.height);
		scrollRectToVisible(r);
	}

	/**
	 * 
	 * @param ev
	 */
	private void handleMousePressed(MouseEvent ev) {
		Container c = getParent();
		if (!(c instanceof JViewport))
			return;
		JViewport viewport = (JViewport) c;
		lastViewSize = viewport.getExtentSize();
		lastViewLocation = new Point(viewport.getViewPosition());
		mouseOffset = new Point(ev.getPoint());
	}

	/**
	 * 
	 */
	private void initGraph() {
		StationGraph stat = getGraph();
		if (stat == null)
			return;
		stat.setPaintLabels(isPaintLabel());
		stat.setPaintNodes(isPaintNodes());
		revalidate();
		repaint();
	}

	/**
	 * @return Returns the paintLabel.
	 */
	public boolean isPaintLabel() {
		return paintLabel;
	}

	/**
	 * @return Returns the paintNodes.
	 */
	public boolean isPaintNodes() {
		return paintNodes;
	}

	/**
	 * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
	 */
	@Override
	protected void paintChildren(Graphics g) {
		Rectangle rect = getChildBounds();
		Graphics2D gr = (Graphics2D) g.create(rect.x, rect.y, rect.width,
				rect.height);
		paintGraph(gr);
	}

	/**
	 * @param gr
	 */
	private void paintGraph(Graphics2D gr) {
		if (graph == null)
			return;
		painter.setSize(getSize());
		painter.setStation(graph);
		painter.paint(gr);
	}

	/**
	 * @param l
	 */
	public synchronized void removeStationGraphListener(StationGraphListener l) {
		List<StationGraphListener> list = elementListeners;
		if (list == null || list.contains(l)) {
			return;
		}
		list = new ArrayList<StationGraphListener>(list);
		list.remove(l);
		this.elementListeners = list;
	}

	/**
	 * @param l
	 */
	public synchronized void removeTrainGraphListener(TrainGraphListener l) {
		List<TrainGraphListener> list = trainListeners;
		if (list == null || list.contains(l)) {
			return;
		}
		list = new ArrayList<TrainGraphListener>(list);
		list.remove(l);
		this.trainListeners = list;
	}

	/**
	 * @param graph
	 *            The graph to set.
	 */
	private void setGraph(StationGraph graph) {
		StationGraph oldGraph = this.graph;
		this.graph = graph;
		firePropertyChange("graph", oldGraph, graph);
		initGraph();
	}

	/**
	 * @param paintLabel
	 *            The paintLabel to set.
	 */
	public void setPaintLabel(boolean paintLabel) {
		boolean oldPaintLabel = this.paintLabel;
		this.paintLabel = paintLabel;
		initGraph();
		firePropertyChange("paintLabel", oldPaintLabel, paintLabel);
	}

	/**
	 * @param paintNodes
	 *            The paintNodes to set.
	 */
	public void setPaintNodes(boolean paintNodes) {
		boolean oldPaintNodes = this.paintNodes;
		this.paintNodes = paintNodes;
		initGraph();
		firePropertyChange("paintNodes", oldPaintNodes, paintNodes);
	}

	/**
	 * @param scale
	 *            The scale to set.
	 */
	public void setScale(double scale) {
		double oldScale = this.scale;
		this.scale = scale;
		revalidate();
		repaint();
		firePropertyChange("scale", oldScale, scale);
	}

	/**
	 * @param station
	 */
	public void showStation(Station station) {
		GraphBuilder builder = new GraphBuilder();
		builder.buildStation(station);
		ElementWalker walker = new ElementWalker(builder);
		station.getReference().accept(walker);
		StationGraph graph = builder.getStationGraph();
		setGraph(graph);
	}
}