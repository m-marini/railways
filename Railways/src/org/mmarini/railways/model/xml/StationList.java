package org.mmarini.railways.model.xml;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmarini.railways.model.ValidationException;
import org.mmarini.railways.model.elements.Station;
import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * @version $Id: StationList.java,v 1.4 2012/02/08 22:03:33 marco Exp $
 */
public class StationList {
	private static final String[] RESOURCE_LIST = {
			"/stations/downville.station.xml", "/stations/passing.station.xml",
			"/stations/terminal.station.xml", "/stations/crossing.station.xml" };
	private static final String STATION_XSD = "/stations/station-1.2.5.xsd";
	private static Log log = LogFactory.getLog(StationList.class);

	private Map<String, URL> stationMap;
	private StationSaxHandler handler;
	private SAXParserFactory factory;

	/**
	 * 
	 */
	public StationList() {
		stationMap = new HashMap<String, URL>();
		handler = new StationSaxHandler();
		factory = SAXParserFactory.newInstance();

		factory.setNamespaceAware(true);
		URL schemaUrl = getClass().getResource(STATION_XSD);
		if (schemaUrl != null)
			try {
				Schema schema = SchemaFactory.newInstance(
						XMLConstants.W3C_XML_SCHEMA_NS_URI)
						.newSchema(schemaUrl);
				factory.setSchema(schema);
			} catch (SAXException e) {
				log.error(e.getMessage(), e);
			}

		createStationMap();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws ValidationException
	 * @throws SAXException
	 */
	private void createStationMap() {
		for (String resource : RESOURCE_LIST) {
			URL url = getClass().getResource(resource);
			try {
				Station station = loadStation(url);
				if (station != null) {
					stationMap.put(station.getName(), url);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getStationNames() {
		return stationMap.keySet();
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws ValidationException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Station loadStation(String name) throws IOException, SAXException,
			ValidationException, ParserConfigurationException {
		URL url = stationMap.get(name);
		if (url == null) {
			log.warn("Missing station " + name);
			return null;
		}
		return loadStation(url);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ValidationException
	 * @throws ParserConfigurationException
	 */
	private Station loadStation(URL url) throws IOException, SAXException,
			ValidationException, ParserConfigurationException {
		factory.newSAXParser().parse(url.openStream(), handler);
		return handler.getStation();
		// StationBuilderImpl builder = new StationBuilderImpl();
		// StationDirector director = new StationDirector(builder, url);
		// director.create();
		// return builder.getStation();
	}
}
