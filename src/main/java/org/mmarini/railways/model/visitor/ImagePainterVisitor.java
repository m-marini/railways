package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.graphics.CurvePainterAdapter;
import org.mmarini.railways.model.graphics.DeadTrackImgPainter;
import org.mmarini.railways.model.graphics.SegmentPainterAdapter;

public class ImagePainterVisitor extends ShapePainterVisitor {
	private static ImagePainterVisitor instance = new ImagePainterVisitor();

	/**
	 * @return Returns the instance.
	 */
	public static ShapePainterVisitor getInstance() {
		return instance;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ShapePainterVisitor#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve curve) {
		setPainter(new CurvePainterAdapter(curve));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeadTrack(org.mmarini.railways.model.elements.DeadTrack)
	 */
	@Override
	public void visitDeadTrack(DeadTrack track) {
		setPainter(DeadTrackImgPainter.getInstance());
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitPlatform(org.mmarini.railways.model.elements.Platform)
	 */
	@Override
	public void visitPlatform(Platform platform) {
		setPainter(new SegmentPainterAdapter(platform));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitSegment(org.mmarini.railways.model.elements.Segment)
	 */
	@Override
	public void visitSegment(Segment segment) {
		setPainter(new SegmentPainterAdapter(segment));
	}

}
