package org.mmarini.railways.swing;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.graphics.StationGraphEvent;
import org.mmarini.railways.model.visitor.ElementVisitorAdapter;

/**
 * @author $Author: marco $
 * @version $Id: EventHandlerVisitor.java,v 1.3.2.1 2005/11/07 21:37:13 marco
 *          Exp $
 */
public class EventHandlerVisitor extends ElementVisitorAdapter {
	private StationGraphEvent event;
	private GameHandler gameHandler;

	/**
	 * @param event
	 */
	public EventHandlerVisitor() {
	}

	/**
	 * @param event
	 *            the event to set
	 */
	public void setEvent(StationGraphEvent event) {
		this.event = event;
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitorAdapter#visitCrossDeviator(org.mmarini.railways.model.elements.CrossDeviator)
	 */
	@Override
	public void visitCrossDeviator(CrossDeviator element) {
		gameHandler.select(element);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitDeviator(org.mmarini.railways.model.elements.Deviator)
	 */
	@Override
	public void visitDeviator(Deviator element) {
		gameHandler.select(element);
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitLine(org.mmarini.railways.model.elements.Line)
	 */
	@Override
	public void visitLine(Line element) {
		gameHandler.select(element, event.getIndex());
	}

	/**
	 * @see org.mmarini.railways.model.visitor.ElementVisitor#visitSemaphore(org.mmarini.railways.model.elements.Semaphore)
	 */
	@Override
	public void visitSemaphore(Semaphore element) {
		gameHandler.select(element, event.getIndex());
	}
}