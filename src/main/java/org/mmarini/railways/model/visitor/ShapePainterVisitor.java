package org.mmarini.railways.model.visitor;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.graphics.CrossDeviatorPainterAdapter;
import org.mmarini.railways.model.graphics.CurvePainterAdapter;
import org.mmarini.railways.model.graphics.DeviatorPainterAdapter;
import org.mmarini.railways.model.graphics.LinePainterAdapter;
import org.mmarini.railways.model.graphics.Painter;
import org.mmarini.railways.model.graphics.SegmentPainterAdapter;
import org.mmarini.railways.model.graphics.SemaphorePainterAdapter;

public class ShapePainterVisitor extends ElementVisitorAdapter {
	private static ShapePainterVisitor instance = new ShapePainterVisitor();

	/**
	 * @return Returns the instance.
	 */
	public static ShapePainterVisitor getInstance() {
		return instance;
	}

	private Painter painter;

	/**
	 * 
	 *
	 */
	protected ShapePainterVisitor() {
	}

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
	protected void setPainter(Painter painter) {
		this.painter = painter;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator deviator) {
		setPainter(new CrossDeviatorPainterAdapter(deviator));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCurve(org.mmarini.railways.model.elements.Curve)
	 */
	@Override
	public void visitCurve(Curve curve) {
		setPainter(new CurvePainterAdapter(curve));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator deviator) {
		setPainter(new DeviatorPainterAdapter(deviator));
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line line) {
		setPainter(new LinePainterAdapter(line));
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

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore semaphore) {
		setPainter(new SemaphorePainterAdapter(semaphore));
	}

}
