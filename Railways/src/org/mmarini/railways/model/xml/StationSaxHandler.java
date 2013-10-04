/**
 * 
 */
package org.mmarini.railways.model.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.TopologyBuilder;
import org.mmarini.railways.model.TopologyDirector;
import org.mmarini.railways.model.ValidationException;
import org.mmarini.railways.model.elements.AbstractTrack;
import org.mmarini.railways.model.elements.Cross;
import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Curve;
import org.mmarini.railways.model.elements.DeadTrack;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Platform;
import org.mmarini.railways.model.elements.Point;
import org.mmarini.railways.model.elements.Segment;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.elements.StationNode;
import org.mmarini.railways.model.elements.VirtualStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author US00852
 * 
 */
public class StationSaxHandler extends DefaultHandler implements
		RailwayConstants {
	public static final double INCOME_CANCEL_FREQUENCE = 0.;
	private static final double OUTCOME_BUILD_FREQUENCE = 0.;

	private static Logger log = LoggerFactory
			.getLogger(StationSaxHandler.class);
	private Locator locator;
	private StringBuilder text;
	private Station station;
	private String stationReference;
	private Map<String, StationNode> map;
	private List<LineInfo> lineList;
	private boolean deviated;
	private double angle;
	private String name;
	private double length;
	private String versus;
	private int gap;
	private int[] nodeIndexes;
	private String[] nodeRefs;
	private int nodeIdx;
	private boolean destination;
	private String reference;
	private String stationName;
	private int direction;

	/**
	 * 
	 */
	public StationSaxHandler() {
		text = new StringBuilder();
		map = new HashMap<String, StationNode>();
		nodeIndexes = new int[2];
		nodeRefs = new String[2];
		lineList = new ArrayList<LineInfo>();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		text.append(ch, start, length);
	}

	/**
	 * 
	 */
	private void endCross() {
		String reference = text.toString();
		Cross node = new Cross(reference, angle);
		map.put(reference, node);
	}

	/**
	 * 
	 */
	private void endCrossDeviator() {
		String reference = text.toString();
		CrossDeviator node = new CrossDeviator(reference, deviated, angle);
		map.put(reference, node);
	}

	/**
	 * @throws SAXParseException
	 */
	private void endCurve() throws SAXParseException {
		double radius = RADIUS + gap * TRACK_GAP;
		double l = length * CURVE_RADS * radius;
		Curve link = new Curve(l, radius, "left".equalsIgnoreCase(versus));
		setupTrack(link);
	}

	/**
	 * 
	 */
	private void endDeadTrack() {
		String reference = text.toString();
		DeadTrack node = new DeadTrack(reference);
		map.put(reference, node);
	}

	/**
	 * 
	 */
	private void endDeviator() {
		String reference = text.toString();
		Deviator node = new Deviator(reference, deviated);
		map.put(reference, node);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (!STATION_NS_URI.equals(uri))
			return;
		log.debug("End element " + localName);
		if (STATION_ELEMENT.equals(localName)) {
			endStation();
		} else if (NAME_ELEMENT.equals(localName)) {
			stationName = text.toString();
		} else if (LINE_ELEMENT.equals(localName)) {
			endLine();
		} else if (SEMAPHORE_ELEMENT.equals(localName)) {
			endSemaphore();
		} else if (POINT_ELEMENT.equals(localName)) {
			endPoint();
		} else if (DEADTRACK_ELEMENT.equals(localName)) {
			endDeadTrack();
		} else if (DEVIATOR_ELEMENT.equals(localName)) {
			endDeviator();
		} else if (CROSS_ELEMENT.equals(localName)) {
			endCross();
		} else if (CROSS_DEVIATOR_ELEMENT.equals(localName)) {
			endCrossDeviator();
		} else if (PLATFORM_ELEMENT.equals(localName)) {
			endPlatform();
		} else if (SEGMENT_ELEMENT.equals(localName)) {
			endSegment();
		} else if (CURVE_ELEMENT.equals(localName)) {
			endCurve();
		} else if (NODE_ELEMENT.equals(localName)) {
			nodeRefs[nodeIdx++] = text.toString();
		}
	}

	/**
	 * 
	 */
	private void endLine() {
		Line line = new Line(reference);
		lineList.add(new LineInfo(line, destination));
		map.put(reference, line);
	}

	/**
	 * @throws SAXParseException
	 */
	private void endPlatform() throws SAXParseException {
		Platform link = new Platform(length * SEGMENT_LENGTH);
		link.setName(name);
		setupTrack(link);
	}

	/**
	 * 
	 */
	private void endPoint() {
		reference = text.toString();
		Point node = new Point(reference);
		map.put(reference, node);
	}

	/**
	 * @throws SAXParseException
	 */
	private void endSegment() throws SAXParseException {
		Segment link = new Segment(length * SEGMENT_LENGTH);
		setupTrack(link);
	}

	/**
	 * 
	 */
	private void endSemaphore() {
		reference = text.toString();
		Semaphore node = new Semaphore(reference);
		map.put(reference, node);
	}

	/**
	 * @throws SAXParseException
	 */
	private void endStation() throws SAXParseException {
		StationNode ref = map.get(stationReference);
		if (ref == null) {
			throw new SAXParseException(
					"Missing reference " + stationReference, locator);
		}

		station = new Station();
		station.setName(stationName);
		station.setDirection(direction);
		station.setReference(ref);

		/*
		 * Create the topology
		 */
		TopologyDirector topDirector = new TopologyDirector(
				new TopologyBuilder(), station);
		topDirector.create();

		for (LineInfo info : lineList) {
			VirtualStation virtual = new VirtualStation();
			Line line = info.getLine();
			String reference = line.getReference();
			virtual.setName(reference);
			try {
				station.addNeighbour(reference, virtual);
			} catch (ValidationException e) {
				throw new SAXParseException(e.getMessage(), locator, e);
			}
			boolean dest = info.isDestination();
			virtual.setOutcome(dest);
			if (dest) {
				virtual.setBuildFrequence(OUTCOME_BUILD_FREQUENCE);
			} else {
				virtual.setCancelFrequence(INCOME_CANCEL_FREQUENCE);
			}
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		throw e;
	}

	/**
	 * @return the station
	 */
	public Station getStation() {
		return station;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator
	 *      )
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 * 
	 * @param link
	 * @throws SAXParseException
	 */
	private void setupTrack(AbstractTrack link) throws SAXParseException {
		for (int i = 0; i < 2; ++i) {
			String nodeName = nodeRefs[i];
			StationNode node = map.get(nodeName);
			if (node == null)
				throw new SAXParseException("Node " + nodeName + " not found",
						locator);
			int nodeIndex = nodeIndexes[i];
			try {
				node.attach(nodeIndex, link, i);
			} catch (ValidationException e) {
				throw new SAXParseException(e.getMessage(), locator, e);
			}
		}
	}

	/**
	 * @param attributes
	 */
	private void startCross(Attributes attributes) {
		String value = attributes.getValue(ANGLE_ATTRIBUTE);
		if (value != null)
			angle = Double.parseDouble(value);
		else
			angle = DEFAULT_CROSS_ANGLE;
	}

	/**
	 * @param attributes
	 */
	private void startCrossDeviator(Attributes attributes) {
		String value = attributes.getValue(ANGLE_ATTRIBUTE);
		if (value != null)
			angle = Double.parseDouble(value);
		else
			angle = DEFAULT_CROSS_ANGLE;
		deviated = Boolean
				.parseBoolean(attributes.getValue(DEVIATED_ATTRIBUTE));
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		locator = null;
		text.setLength(0);
		station = null;
		stationReference = null;
		map.clear();
		lineList.clear();
		deviated = false;
		angle = 0;
		name = null;
		length = 0;
		versus = null;
		gap = 0;
		Arrays.fill(nodeRefs, null);
		Arrays.fill(nodeIndexes, 0);
		nodeIdx = 0;
		destination = false;
		reference = null;
		stationName = null;
		direction = 0;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		text.setLength(0);
		if (!STATION_NS_URI.equals(uri))
			return;
		log.debug("Start element " + localName);
		if (STATION_ELEMENT.equals(localName)) {
			stationReference = attributes.getValue(REFERENCE_ATTRIBUTE);
			direction = Integer.parseInt(attributes
					.getValue(STATION_DIRECTION_ATTRIBUTE));
		} else if (NODES_ELEMENT.equals(localName)) {
			map.clear();
			lineList.clear();
		} else if (LINE_ELEMENT.equals(localName)) {
			reference = attributes.getValue(REFERENCE_ATTRIBUTE);
			destination = Boolean.parseBoolean(attributes
					.getValue(DESTINATION_ATTRIBUTE));
		} else if (DEVIATOR_ELEMENT.equals(localName)) {
			deviated = Boolean.parseBoolean(attributes
					.getValue(DEVIATED_ATTRIBUTE));
		} else if (CROSS_ELEMENT.equals(localName)) {
			startCross(attributes);
		} else if (CROSS_DEVIATOR_ELEMENT.equals(localName)) {
			startCrossDeviator(attributes);
		} else if (PLATFORM_ELEMENT.equals(localName)) {
			name = attributes.getValue(NAME_ATTRIBUTE);
			length = Double.parseDouble(attributes.getValue(LENGTH_ATTRIBUTE));
			nodeIdx = 0;
		} else if (SEGMENT_ELEMENT.equals(localName)) {
			length = Double.parseDouble(attributes.getValue(LENGTH_ATTRIBUTE));
			nodeIdx = 0;
		} else if (CURVE_ELEMENT.equals(localName)) {
			length = Double.parseDouble(attributes.getValue(LENGTH_ATTRIBUTE));
			versus = attributes.getValue(VERSUS_ATTRIBUTE);
			gap = Integer.parseInt(attributes.getValue(GAP_ATTRIBUTE));
			nodeIdx = 0;
		} else if (NODE_ELEMENT.equals(localName)) {
			nodeIndexes[nodeIdx] = Integer.parseInt(attributes
					.getValue(INDEX_ATTRIBUTE));
		}
	}
}
