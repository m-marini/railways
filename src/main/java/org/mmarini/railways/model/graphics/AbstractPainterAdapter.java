package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.StationElement;

public abstract class AbstractPainterAdapter implements Painter {
	private StationElement element;

	/**
	 * @param element
	 */
	public AbstractPainterAdapter(StationElement element) {
		this.element = element;
	}

	/**
	 * @return Returns the segment.
	 */
	protected StationElement getElement() {
		return element;
	}

}
