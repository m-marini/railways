package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Curve;

/**
 * @author $Author: marco $
 * @version $Id: SegmentImgPainterAdapter.java,v 1.1.4.1 2005/11/07 21:37:12
 *          marco Exp $
 */
public class CurveImgPainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param curve
	 */
	public CurveImgPainterAdapter(Curve curve) {
		super(curve);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		CurveImgPainter.getInstance().paint(ctx, (Curve) getElement());
	}
}