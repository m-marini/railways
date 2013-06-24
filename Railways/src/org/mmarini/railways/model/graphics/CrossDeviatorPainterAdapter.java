package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.CrossDeviator;

public class CrossDeviatorPainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param deviator
	 */
	public CrossDeviatorPainterAdapter(CrossDeviator deviator) {
		super(deviator);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		DeviatorPainter.getInstance().paintNodes(ctx,
				(CrossDeviator) getElement());
	}

}
