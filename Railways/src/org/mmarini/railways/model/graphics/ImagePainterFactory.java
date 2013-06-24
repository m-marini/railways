package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.visitor.ImagePainterVisitor;

/**
 * @author $Author: marco $
 * @version $Id: ImagePainterFactory.java,v 1.1.2.1 2005/10/18 17:30:10 marco
 *          Exp $
 */
public class ImagePainterFactory extends ShapePainterFactory implements
		RailwayConstants, GraphicContants {

	private static PainterFactory instance = new ImagePainterFactory();

	/**
	 * @return Returns the instance.
	 */
	public static PainterFactory getInstance() {
		return instance;
	}

	private Painter headPainter;
	private Painter coachPainter;
	private Painter tailPainter;

	/**
	 *
	 *
	 */
	protected ImagePainterFactory() {
		super();
		headPainter = new ImagePainter("/img/head.png", -COACH_WIDTH
				* DEFAULT_SCALE / 2, 0, DEFAULT_SCALE);
		coachPainter = new ImagePainter("/img/coach.png", -COACH_WIDTH
				* DEFAULT_SCALE / 2, 0, DEFAULT_SCALE);
		tailPainter = new ImagePainter("/img/tail.png", -COACH_WIDTH
				* DEFAULT_SCALE / 2, 0, DEFAULT_SCALE);
		setVisitor(ImagePainterVisitor.getInstance());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.LinePainterFactory#createBackground()
	 */
	// public IPainter createBackground() {
	// return backgroundPainter;
	// }

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createCoach()
	 */
	@Override
	public Painter createCoach() {
		return coachPainter;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createHead()
	 */
	@Override
	public Painter createHead() {
		return headPainter;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createTail()
	 */
	@Override
	public Painter createTail() {
		return tailPainter;
	}
}
