package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.visitor.ShapePainterVisitor;

/**
 * @author $Author: marco $
 * @version $Id: ShapePainterFactory.java,v 1.1.2.1 2005/10/18 17:30:10 marco
 *          Exp $
 */
public class ShapePainterFactory extends LinePainterFactory {
	private static PainterFactory instance = new ShapePainterFactory();

	/**
	 * @return Returns the instance.
	 */
	public static PainterFactory getInstance() {
		return instance;
	}

	private ShapePainterVisitor visitor;

	/**
	 * 
	 *
	 */
	protected ShapePainterFactory() {
		super(BACKGROUND_COLOR);
		setVisitor(ShapePainterVisitor.getInstance());
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createCoach()
	 */
	@Override
	public Painter createCoach() {
		return CoachPainter.getInstance();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createElement(org.mmarini.railways.model.elements.StationElement)
	 */
	@Override
	public Painter createElement(StationElement element) {
		element.accept(visitor);
		return visitor.getPainter();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createHead()
	 */
	@Override
	public Painter createHead() {
		return HeadPainter.getInstance();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createTail()
	 */
	@Override
	public Painter createTail() {
		return TailPainter.getInstance();
	}

	/**
	 * @see org.mmarini.railways.model.graphics.PainterFactory#createTrainElement(org.mmarini.railways.model.elements.StationElement)
	 */
	@Override
	public Painter createTrainElement(StationElement element) {
		return null;
	}

	/**
	 * @return Returns the visitor.
	 */
	protected ShapePainterVisitor getVisitor() {
		return visitor;
	}

	/**
	 * @param visitor
	 *            The visitor to set.
	 */
	protected void setVisitor(ShapePainterVisitor visitor) {
		this.visitor = visitor;
	}
}
