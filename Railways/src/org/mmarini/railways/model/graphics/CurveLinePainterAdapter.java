package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Curve;

public class CurveLinePainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param curve
	 */
	public CurveLinePainterAdapter(Curve curve) {
		super(curve);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		CurveLinePainter.getInstance().paint(ctx.getGraphics(),
				(Curve) getElement());
	}

}
