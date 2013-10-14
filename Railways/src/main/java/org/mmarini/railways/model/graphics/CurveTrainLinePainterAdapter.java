package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Curve;

/**
 * @author $Author: marco $
 * @version $Id: CurveTrainLinePainterAdapter.java,v 1.1.2.3 2005/10/30 23:14:57
 *          marco Exp $
 */
public class CurveTrainLinePainterAdapter extends AbstractPainterAdapter {

	/**
	 * @param curve
	 * @param from
	 * @param to
	 */
	public CurveTrainLinePainterAdapter(Curve curve) {
		super(curve);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		CurveLinePainter.getInstance().paintTrain(ctx.getGraphics(),
				(Curve) getElement());
	}

}