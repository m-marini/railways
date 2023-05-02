package org.mmarini.railways.model.graphics;

import java.awt.Color;

import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.visitor.LinePainterVisitor;
import org.mmarini.railways.model.visitor.TrainPainterVisitor;

/**
 * @author $Author: marco $
 * @version $Id: ShapePainterFactory.java,v 1.1.2.1 2005/10/18 17:30:10 marco
 *          Exp $
 */
public class LinePainterFactory implements PainterFactory, GraphicContants {

	private static PainterFactory instance = new LinePainterFactory();

	/**
	 * @return Returns the instance.
	 */
	public static PainterFactory getInstance() {
		return instance;
	}

	private Painter backgroundPainter;

	/**
	 * 
	 *
	 */
	protected LinePainterFactory() {
		this(PANEL_COLOR);
	}

	/**
	 * @param background
	 */
	protected LinePainterFactory(Color background) {
		this.backgroundPainter = new BackgroundPainter(background);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createBackground()
	 */
	@Override
	public Painter createBackground() {
		return backgroundPainter;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createCoach()
	 */
	@Override
	public Painter createCoach() {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createElement(org.mmarini.railways.model.elements.StationElement)
	 */
	@Override
	public Painter createElement(StationElement element) {
		LinePainterVisitor visitor = new LinePainterVisitor();
		element.accept(visitor);
		return visitor.getPainter();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createHead()
	 */
	@Override
	public Painter createHead() {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createTail()
	 */
	@Override
	public Painter createTail() {
		return null;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createTrainElement(org.mmarini.railways.model.elements.StationElement)
	 */
	@Override
	public Painter createTrainElement(StationElement element) {
		TrainPainterVisitor visitor = new TrainPainterVisitor();
		element.accept(visitor);
		return visitor.getPainter();
	}
}
