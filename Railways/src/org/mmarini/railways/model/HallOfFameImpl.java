package org.mmarini.railways.model;

import java.util.SortedSet;
import java.util.TreeSet;

import org.mmarini.railways.model.xml.XMLBuilder;
import org.mmarini.railways.model.xml.XmlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFameImpl.java,v 1.4 2012/02/08 22:03:18 marco Exp $
 */
public class HallOfFameImpl implements HallOfFame, XmlConstants {

	public static final int MAX_ENTRIES = 20;
	private static Logger log = LoggerFactory.getLogger(HallOfFameImpl.class);

	private SortedSet<ManagerInfos> entries;

	/**
	 * 
	 */
	public HallOfFameImpl() {
		entries = new TreeSet<ManagerInfos>();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.HallOfFame#addNewEntry(org.mmarini.railways.model.ManagerInfos)
	 */
	@Override
	public void addNewEntry(ManagerInfos infos) {
		SortedSet<ManagerInfos> entries = getEntries();
		log.debug("Adding " + infos.getName() + " " + entries.contains(infos));
		entries.add(infos);
		if (entries.size() > MAX_ENTRIES) {
			entries.remove(entries.last());
		}
	}

	/**
	 * @see org.mmarini.railways.model.HallOfFame#clear()
	 */
	@Override
	public void clear() {
		entries.clear();
	}

	/**
	 * @throws SAXException
	 * @see org.mmarini.railways.model.HallOfFame#createXML(org.mmarini.railways.
	 *      model.xml.XMLBuilder)
	 */
	@Override
	public void createXML(XMLBuilder xmlBuilder) throws SAXException {
		xmlBuilder.setNameSpace(HALL_OF_FAME_NS_URI);
		xmlBuilder.startElement(HALL_OF_FAME_ELEM);

		for (ManagerInfos infos : entries) {
			infos.createXML(xmlBuilder);
		}

		xmlBuilder.setNameSpace(HALL_OF_FAME_NS_URI);
		xmlBuilder.endElement(HALL_OF_FAME_ELEM);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.HallOfFame#getEntries()
	 */
	@Override
	public SortedSet<ManagerInfos> getEntries() {
		return entries;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.HallOfFame#isNewEntry(org.mmarini.railways.model.ManagerInfos)
	 */
	@Override
	public boolean isNewEntry(ManagerInfos infos) {
		SortedSet<ManagerInfos> entries = getEntries();
		if (entries.size() < MAX_ENTRIES)
			return true;
		ManagerInfos worst = entries.last();
		return infos.compareTo(worst) < 0;
	}
}
