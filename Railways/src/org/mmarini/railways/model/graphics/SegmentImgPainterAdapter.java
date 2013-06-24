package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Segment;

/**
 * @author $Author: marco $
 * @version $Id: SegmentImgPainterAdapter.java,v 1.1.4.1 2005/11/07 21:37:12
 *          marco Exp $
 */
public class SegmentImgPainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param segment
	 */
	public SegmentImgPainterAdapter(Segment segment) {
		super(segment);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		SegmentImgPainter.getInstance().paint(ctx, (Segment) getElement());
	}
}