package org.mmarini.railways.model.graphics;

import org.mmarini.railways.model.elements.Semaphore;

/**
 * 
 * @author US00852
 * 
 */
public class SemaphorePainterAdapter extends AbstractPainterAdapter {
	/**
	 * 
	 * @param semaphore
	 */
	public SemaphorePainterAdapter(Semaphore semaphore) {
		super(semaphore);
	}

	/**
	 * 
	 */
	@Override
	public void paint(GraphicsContext ctx) {
		SemaphorePainter.getInstance()
				.paintNodes(ctx, (Semaphore) getElement());
	}

}
