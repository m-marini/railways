package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.StationElement;

/**
 * @author $Author: marco $
 * @version $Id: PainterFactory.java,v 1.3 2012/02/08 22:03:31 marco Exp $
 */
public interface PainterFactory {

	/**
	 * @return
	 */
	public abstract Painter createBackground();

	/**
	 * @return
	 */
	public abstract Painter createCoach();

	/**
	 * @param element
	 * @return
	 */
	public abstract Painter createElement(StationElement element);

	/**
	 * @return
	 */
	public abstract Painter createHead();

	/**
	 * @return
	 */
	public abstract Painter createTail();

	/**
	 * @param track
	 * @return
	 */
	public abstract Painter createTrainElement(StationElement element);
}
