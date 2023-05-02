package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Segment;

public class SegmentTrainLinePainterAdapter extends AbstractPainterAdapter {
	private double from;
	private double to;

	/**
	 * @param segment
	 * @param from
	 * @param to
	 */
	public SegmentTrainLinePainterAdapter(Segment segment, double from,
			double to) {
		super(segment);
		this.from = from;
		this.to = to;
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		SegmentLinePainter.getInstance()
				.paintTrain(ctx.getGraphics(), from, to);
	}

}
// Segment elem = (Segment) getTrack();
// double from = elem.getTrainHead();
// if (from < 0)
// return;
// double to = elem.getTrainTail();
// AffineTransform oldTrans = gr.getTransform();
// gr.transform(getTransform());
// gr.setTransform(oldTrans);
// }
