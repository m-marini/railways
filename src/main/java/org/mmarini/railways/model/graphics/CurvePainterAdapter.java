package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Curve;

public class CurvePainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param curve
	 */
	public CurvePainterAdapter(Curve curve) {
		super(curve);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		CurvePainter.getInstance().paint(ctx.getGraphics(),
				(Curve) getElement());
	}
}
