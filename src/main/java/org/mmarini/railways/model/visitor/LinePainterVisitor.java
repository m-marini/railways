package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.graphics.CurveLinePainterAdapter;
import org.mmarini.railways.model.graphics.Painter;
import org.mmarini.railways.model.graphics.SegmentLinePainterAdapter;

public class LinePainterVisitor extends ElementVisitorAdapter {
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
		setPainter(new CurveLinePainterAdapter(curve));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform platform) {
		setPainter(new SegmentLinePainterAdapter(platform));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment segment) {
		setPainter(new SegmentLinePainterAdapter(segment));
	}

}
