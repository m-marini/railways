package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Deviator;

public class DeviatorPainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param deviator
	 */
	public DeviatorPainterAdapter(Deviator deviator) {
		super(deviator);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		DeviatorPainter.getInstance().paintNodes(ctx, (Deviator) getElement());
	}

}
