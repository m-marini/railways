package org.mmarini.railways.model.graphics;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.train.Train;

/**
 * @author $Author: marco $
 * @version $Id: StationPainter.java,v 1.5 2012/02/08 22:03:31 marco Exp $
 */
public class StationPainter {
	private GraphicsContext bgCtx;
	private GraphicsContext fgCtx;
	private Dimension size;
	private StationGraph station;

	/**
	 * 
	 * @param painterFactory
	 */
	public StationPainter(PainterFactory painterFactory) {
		bgCtx = new GraphicsContext();
		bgCtx.setPainterFactory(painterFactory);
		fgCtx = new GraphicsContext();
		fgCtx.setPainterFactory(painterFactory);
	}

	/**
	 * Create the transformation.
	 * 
	 * @return the transformation.
	 */
	protected AffineTransform createTransform() {
		StationGraph stat = station;
		if (stat == null)
			return new AffineTransform();
		Rectangle2D bounds = stat.getBounds();
		AffineTransform transform = AffineTransform.getTranslateInstance(
				size.getWidth() / 2., size.getHeight() / 2.);
		double sx = size.getWidth() / bounds.getWidth();
		double sy = size.getHeight() / bounds.getHeight();
		transform.scale(sx, sy);
		transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
		return transform;
	}

	/**
	 * @param ctx
	 * @param station
	 */
	public void paint(Graphics2D gr) {
		gr.transform(createTransform());
		GraphicsContext ctx = bgCtx;
		ctx.setGraphics(gr);
		ctx.setBounds(station.getBounds());
		station.paintBackground(ctx);
		station.paintTrain(ctx);
		Station station1 = station.getStation();
		List<Train> trainList = station1.getTrainList();
		for (Train train : trainList) {
			station.paintTrain(ctx, train);
		}
	}

	/**
	 * @param size
	 *            The size to set.
	 */
	public void setSize(Dimension size) {
		this.size = size;
	}

	/**
	 * @param station
	 *            The station to set.
	 */
	public void setStation(StationGraph station) {
		this.station = station;
	}

	/**
	 * @param ctx
	 * @param station
	 */
	public void update() {
	}
}
