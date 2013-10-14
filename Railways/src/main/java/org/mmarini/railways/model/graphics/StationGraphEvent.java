package org.mmarini.railways.model.graphics;

import java.awt.geom.Point2D;
import java.util.EventObject;

import org.mmarini.railways.model.elements.StationElement;

/**
 * @author $Author: marco $
 * @version $Id: ElementSelectionEvent.java,v 1.1.2.1 2005/08/30 18:01:18 marco
 *          Exp $
 */
public class StationGraphEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private StationElement element;
	private int index;
	private Point2D location;

	/**
	 * @param source
	 */
	public StationGraphEvent(Object source) {
		super(source);
	}

	/**
	 * @return Returns the element.
	 */
	public StationElement getElement() {
		return element;
	}

	/**
	 * @return Returns the index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return Returns the location.
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 * Sets the element.
	 * 
	 * @param element
	 *            the element.
	 */
	public void setElement(StationElement element) {
		this.element = element;
	}

	/**
	 * @param index
	 *            The index to set.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @param location
	 */
	public void setLocation(Point2D location) {
		this.location = location;
	}
}