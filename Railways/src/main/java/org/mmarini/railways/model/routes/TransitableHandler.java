package org.mmarini.railways.model.routes;

/**
 * @author $$Author: marco $$
 * @version $Id: ITransitableHandler.java,v 1.1.4.2 2005/08/22 18:11:12 marco
 *          Exp $
 */
public interface TransitableHandler {

	/**
	 * Return true if the object is transitable.
	 * 
	 * @return true if the object is transitable.
	 */
	public abstract boolean isTransitable();
}