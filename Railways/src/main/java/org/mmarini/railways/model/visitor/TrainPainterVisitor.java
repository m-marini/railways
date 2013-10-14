package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.graphics.CurveTrainLinePainterAdapter;
import org.mmarini.railways.model.graphics.Painter;
import org.mmarini.railways.model.graphics.SegmentTrainLinePainterAdapter;

public class TrainPainterVisitor extends ElementVisitorAdapter {
	private Painter painter;

	/**
	 * @return Returns the painter.
	 */
	public Painter getPainter() {
		return painter;
	}

	/**
	 * @param painter
	 *            The painter to set.
	 */
	private void setPainter(Painter painter) {
		this.painter = painter;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve curve) {
		double from = curve.getTrainHead();
		if (from < 0)
			return;
		setPainter(new CurveTrainLinePainterAdapter(curve));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform platform) {
		visitSegment(platform);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment segment) {
		double from = segment.getTrainHead();
		if (from < 0)
			return;
		double to = segment.getTrainTail();
		setPainter(new SegmentTrainLinePainterAdapter(segment, from, to));
	}

}
