package org.mmarini.railways.swing;

import java.util.EventListener;

import org.mmarini.railways.model.graphics.TrainGraphEvent;

/**
 * @author $Author: marco $
 * @version $Id: TrainGraphListener.java,v 1.3 2005/09/18 10:58:40 marco Exp $
 */
public interface TrainGraphListener extends EventListener {

	/**
	 * @param ev
	 */
	public abstract void trainSelected(TrainGraphEvent ev);

	public abstract void trainStateChanged(TrainGraphEvent ev);
}