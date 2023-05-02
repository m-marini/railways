package org.mmarini.railways.swing;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.graphics.StationGraphEvent;
import org.mmarini.railways.model.graphics.TrainGraphEvent;

/**
 * @author $Author: marco $
 * @version $Id: JElementSelector.java,v 1.7.20.1 2012/02/04 19:22:56 marco Exp
 *          $
 */
public class JElementSelector extends JStation {
	private static final long serialVersionUID = 1L;

	private GameHandler gameHandler;
	private EventHandlerVisitor selectHandlerVisitor;

	/**
	 * 
	 */
	public JElementSelector() {
		selectHandlerVisitor = new EventHandlerVisitor();
		addStationGraphListener(new StationGraphListener() {

			@Override
			public void elementSelected(StationGraphEvent event) {
				handleElementSelected(event);
			}

			@Override
			public void elementStateChanged(StationGraphEvent event) {
				gameHandler.changeState(event.getElement());
			}

			@Override
			public void pointSelected(StationGraphEvent event) {
			}
		});
		addTrainGraphListener(new TrainGraphListener() {

			@Override
			public void trainSelected(TrainGraphEvent event) {
				gameHandler.select(event.getTrain());
			}

			@Override
			public void trainStateChanged(TrainGraphEvent event) {
				gameHandler.changeState(event.getTrain());
			}
		});
	}

	/**
	 * 
	 * @param event
	 */
	private void handleElementSelected(StationGraphEvent event) {
		selectHandlerVisitor.setEvent(event);
		event.getElement().accept(selectHandlerVisitor);
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
		selectHandlerVisitor.setGameHandler(gameHandler);
	}
}