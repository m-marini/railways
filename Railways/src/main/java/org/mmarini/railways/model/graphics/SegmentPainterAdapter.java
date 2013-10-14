package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Segment;

/**
 * 
 * @author US00852
 * 
 */
public class SegmentPainterAdapter extends AbstractPainterAdapter {

	/**
	 * 
	 * @param segment
	 */
	public SegmentPainterAdapter(Segment segment) {
		super(segment);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways
	 *      .model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		SegmentPainter.getInstance().paint(ctx.getGraphics(),
				(Segment) getElement());
	}

}
