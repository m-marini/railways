package org.mmarini.railways.swing;

import java.util.EventListener;

import org.mmarini.railways.model.graphics.StationGraphEvent;

/**
 * @author $Author: marco $
 * @version $Id: StationGraphListener.java,v 1.2.46.1 2012/02/04 19:22:56 marco
 *          Exp $
 */
public interface StationGraphListener extends EventListener {

	/**
	 * @param event
	 */
	public void elementSelected(StationGraphEvent event);

	/**
	 * @param ev
	 */
	public void elementStateChanged(StationGraphEvent ev);

	/**
	 * @param ev
	 */
	public void pointSelected(StationGraphEvent ev);
}