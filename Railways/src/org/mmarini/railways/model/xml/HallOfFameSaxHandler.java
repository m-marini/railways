/**
 * 
 */
package org.mmarini.railways.model.xml;

import org.mmarini.railways.model.HallOfFame;
import org.mmarini.railways.model.ManagerInfos;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author US00852
 * 
 */
public class HallOfFameSaxHandler extends DefaultHandler implements
		XmlConstants {
	private HallOfFame hallOfFame;
	private Locator locator;
	private StringBuilder text;
	private ManagerInfos infos;

	/**
	 * 
	 */
	public HallOfFameSaxHandler() {
		text = new StringBuilder();
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
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (!HALL_OF_FAME_NS_URI.equals(uri))
			return;
		if (MANAGER_INFOS_ELEMENT.equals(qName)) {
			infos.setName(text.toString());
			hallOfFame.addNewEntry(infos);
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
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator
	 *      )
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 * @param hallOfFame
	 *            the hallOfFame to set
	 */
	public void setHallOfFame(HallOfFame hallOfFame) {
		this.hallOfFame = hallOfFame;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		text.setLength(0);
		if (!HALL_OF_FAME_NS_URI.equals(uri))
			return;
		if (HALL_OF_FAME_ELEM.equals(qName)) {
			hallOfFame.clear();
		} else if (MANAGER_INFOS_ELEMENT.equals(qName)) {
			infos = new ManagerInfos();
			try {
				double gameLength = Double.parseDouble(attributes
						.getValue(GAME_LENGTH_ATTRIBUTE));
				int incomeTrainCount = Integer.parseInt(attributes
						.getValue(INCOME_TRAIN_COUNT_ATTRIBUTE));
				int rightOutcomeTrainCount = Integer.parseInt(attributes
						.getValue(RIGHT_OUTCOME_TRAIN_COUNT_ATTRIBUTE));
				String stationName = attributes
						.getValue(STATION_NAME_ATTRIBUTE);
				long timestamp = Long.parseLong(attributes
						.getValue(TIMESTAMP_ATTRIBUTE));
				double totalLifeTime = Double.parseDouble(attributes
						.getValue(TOTAL_LIFETIME_ATTRIBUTE));
				double trainsDistance = Double.parseDouble(attributes
						.getValue(TRAINS_DISTANCE_ATTRIBUTE));
				double trainsLifeTime = Double.parseDouble(attributes
						.getValue(TRAINS_LIFETIME_ATTRIBUTE));
				int trainsStopCount = Integer.parseInt(attributes
						.getValue(TRAINS_STOP_COUNT_ATTRIBUTE));
				double trainsWaitTime = Double.parseDouble(attributes
						.getValue(TRAINS_WAIT_TIME_ATTRIBUTE));
				int wrongOutcomeTrainCount = Integer.parseInt(attributes
						.getValue(WRONG_OUTCOME_TRAIN_COUNT_ATTRIBUTE));
				infos.setGameLength(gameLength);
				infos.setIncomeTrainCount(incomeTrainCount);
				infos.setRightOutcomeTrainCount(rightOutcomeTrainCount);
				infos.setStationName(stationName);
				infos.setTimestamp(timestamp);
				infos.setTotalLifeTime(totalLifeTime);
				infos.setTrainsDistance(trainsDistance);
				infos.setTrainsLifeTime(trainsLifeTime);
				infos.setTrainsStopCount(trainsStopCount);
				infos.setTrainsWaitTime(trainsWaitTime);
				infos.setWrongOutcomeTrainCount(wrongOutcomeTrainCount);
			} catch (NumberFormatException e) {
				throw new SAXParseException(e.getMessage(), locator, e);
			}
		}
	}
}
