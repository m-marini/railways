package org.mmarini.railways.model;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFamePersistence.java,v 1.1.2.1 2006/08/25 16:57:42 marco
 *          Exp $
 */
public interface SystemOptionsPersistence {
	/**
	 * 
	 * @return
	 */
	public abstract SystemOptions loadSystemOptions();

	/**
	 * 
	 * @param systemOptions
	 */
	public abstract void saveSystemOptions(SystemOptions systemOptions);
}
