package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $Author: marco $
 * @version $Id: SegmentLinePainterAdapter.java,v 1.2.2.1 2005/11/07 21:37:12
 *          marco Exp $
 */
public class SegmentLinePainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param segment
	 */
	public SegmentLinePainterAdapter(Segment segment) {
		super(segment);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		SegmentLinePainter.getInstance().paint(ctx.getGraphics(),
				(Segment) getElement());
	}

}