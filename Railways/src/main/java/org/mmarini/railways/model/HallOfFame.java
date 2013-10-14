package org.mmarini.railways.model;

import java.util.SortedSet;

import org.mmarini.railways.model.xml.XMLBuilder;
import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFame.java,v 1.3 2012/02/08 22:03:18 marco Exp $
 */
public interface HallOfFame {

	/**
	 * 
	 * @param infos
	 */
	public abstract void addNewEntry(ManagerInfos infos);

	/**
	 * 
	 */
	public abstract void clear();

	/**
	 * 
	 * @param xmlBuilder
	 * @throws SAXException
	 */
	public abstract void createXML(XMLBuilder xmlBuilder) throws SAXException;

	/**
	 * 
	 * @return
	 */
	public abstract SortedSet<ManagerInfos> getEntries();

	/**
	 * 
	 * @param infos
	 * @return
	 */
	public abstract boolean isNewEntry(ManagerInfos infos);
}
