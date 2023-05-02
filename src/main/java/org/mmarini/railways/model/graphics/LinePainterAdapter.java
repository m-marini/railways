package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Line;

/**
 * @author $Author: marco $
 * @version $Id: LinePainterAdapter.java,v 1.4.16.1 2012/02/04 19:22:59 marco
 *          Exp $
 */
public class LinePainterAdapter extends AbstractPainterAdapter {
	/**
	 * @param line
	 */
	public LinePainterAdapter(Line line) {
		super(line);
	}

	/**
	 * @see org.mmarini.railways.model.graphics.Painter#paint(org.mmarini.railways.model.graphics.GraphicsContext)
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		LinePainter.getInstance().paintNodes(ctx, (Line) getElement());
	}

}
