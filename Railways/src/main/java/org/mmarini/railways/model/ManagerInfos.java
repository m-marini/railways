package org.mmarini.railways.model;

import org.mmarini.railways.model.xml.XMLBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author $Author: marco $
 * @version $Id: ManagerInfos.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class ManagerInfos implements RailwayConstants, Comparable<ManagerInfos> {
	private static final String CDATA = "CDATA";
	private static final String NULL_STRING = "";
	private static final String EMPTY_STRING = NULL_STRING;
	private static final double WORST_PERFORMANCE = 0;
	public static final double EPSILON_TIME = 1e-3;

	private String name;
	private String stationName;
	private int incomeTrainCount;
	private int wrongOutcomeTrainCount;
	private int rightOutcomeTrainCount;
	private double trainsLifeTime;
	private double trainsDistance;
	private int trainsStopCount;
	private double trainsWaitTime;
	private double totalLifeTime;
	private double gameLength;
	private long timestamp;

	/**
	 * 
	 */
	public ManagerInfos() {
		name = EMPTY_STRING;
		stationName = EMPTY_STRING;
		timestamp = System.currentTimeMillis();
	}

	/**
	 * 
	 */
	public void addIncomeTrainCount() {
		++incomeTrainCount;
	}

	/**
	 * 
	 */
	public void addRightOutcomeTrainCount() {
		++rightOutcomeTrainCount;
	}

	/**
	 * @param time
	 */
	public void addTotalLifeTime(double time) {
		totalLifeTime += time;
	}

	/**
	 * @param movement
	 */
	public void addTrainsDistance(double movement) {
		trainsDistance += movement;
	}

	/**
	 * @param time
	 */
	public void addTrainsLifeTime(double time) {
		trainsLifeTime += time;
	}

	/**
	 * 
	 */
	public void addTrainsStopCount() {
		++trainsStopCount;
	}

	/**
	 * @param waitTime
	 */
	public void addTrainsWaitTime(double waitTime) {
		trainsWaitTime += waitTime;
	}

	/**
	 * 
	 */
	public void addWrongOutcomeTrainCount() {
		++wrongOutcomeTrainCount;
	}

	/**
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ManagerInfos other) {
		double perf1 = getPerformance();
		double perf2 = other.getPerformance();
		if (perf1 < perf2)
			return 1;
		if (perf1 > perf2)
			return -1;
		double freq1 = getFrequence();
		double freq2 = other.getFrequence();
		if (freq1 < freq2)
			return 1;
		if (freq1 > freq2)
			return -1;
		long ts1 = timestamp;
		long ts2 = other.timestamp;
		if (ts1 > ts2)
			return 1;
		if (ts1 < ts2)
			return -1;
		return 0;
	}

	/**
	 * 
	 * @param xmlBuilder
	 * @throws SAXException
	 */
	public void createXML(XMLBuilder xmlBuilder) throws SAXException {
		// TODO Auto-generated method stub
		AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				GAME_LENGTH_ATTRIBUTE, CDATA, String.valueOf(gameLength));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				INCOME_TRAIN_COUNT_ATTRIBUTE, CDATA,
				String.valueOf(incomeTrainCount));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				RIGHT_OUTCOME_TRAIN_COUNT_ATTRIBUTE, CDATA,
				String.valueOf(rightOutcomeTrainCount));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				WRONG_OUTCOME_TRAIN_COUNT_ATTRIBUTE, CDATA,
				String.valueOf(wrongOutcomeTrainCount));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				TOTAL_LIFETIME_ATTRIBUTE, CDATA, String.valueOf(totalLifeTime));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				TRAINS_DISTANCE_ATTRIBUTE, CDATA,
				String.valueOf(getTrainsDistance()));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				TRAINS_LIFETIME_ATTRIBUTE, CDATA,
				String.valueOf(getTrainsLifeTime()));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				TRAINS_STOP_COUNT_ATTRIBUTE, CDATA,
				String.valueOf(trainsStopCount));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				TRAINS_WAIT_TIME_ATTRIBUTE, CDATA,
				String.valueOf(trainsWaitTime));
		attributes.addAttribute(NULL_STRING, NULL_STRING, TIMESTAMP_ATTRIBUTE,
				CDATA, String.valueOf(timestamp));
		attributes.addAttribute(NULL_STRING, NULL_STRING,
				STATION_NAME_ATTRIBUTE, CDATA, stationName);

		xmlBuilder.setNameSpace(HALL_OF_FAME_NS_URI);
		xmlBuilder.startElement(MANAGER_INFOS_ELEMENT, attributes);
		xmlBuilder.createText(name);
		xmlBuilder.endElement(MANAGER_INFOS_ELEMENT);
	}

	/**
	 * 
	 * @return
	 */
	public double getFrequence() {
		return incomeTrainCount / totalLifeTime;
	}

	/**
	 * @return Returns the incomeTrainCount.
	 */
	public int getIncomeTrainCount() {
		return incomeTrainCount;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public double getPerformance() {
		if (totalLifeTime < EPSILON_TIME)
			return WORST_PERFORMANCE;
		return rightOutcomeTrainCount / totalLifeTime;
	}

	/**
	 * @return Returns the rightOutcomeTrainCount.
	 */
	public int getRightOutcomeTrainCount() {
		return rightOutcomeTrainCount;
	}

	/**
	 * 
	 * @return
	 */
	public String getStationName() {
		return stationName;
	}

	/**
	 * @return
	 */
	public int getStationTrainCount() {
		return incomeTrainCount - wrongOutcomeTrainCount
				- rightOutcomeTrainCount;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return Returns the totalLifeTime.
	 */
	public double getTotalLifeTime() {
		return totalLifeTime;
	}

	/**
	 * @return Returns the trainsDistance.
	 */
	public double getTrainsDistance() {
		return trainsDistance;
	}

	/**
	 * @return Returns the trainsLifeTime.
	 */
	public double getTrainsLifeTime() {
		return trainsLifeTime;
	}

	/**
	 * @return Returns the trainsStopCount.
	 */
	public int getTrainsStopCount() {
		return trainsStopCount;
	}

	/**
	 * @return Returns the trainsWaitTime.
	 */
	public double getTrainsWaitTime() {
		return trainsWaitTime;
	}

	/**
	 * @return Returns the wrongOutcomeTrainCount.
	 */
	public int getWrongOutcomeTrainCount() {
		return wrongOutcomeTrainCount;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGameEnded() {
		return totalLifeTime >= gameLength;
	}

	/**
	 * 
	 * 
	 */
	public void reset() {
		setTotalLifeTime(0);
		setIncomeTrainCount(0);
		setRightOutcomeTrainCount(0);
		setTrainsDistance(0);
		setTrainsLifeTime(0);
		setTrainsStopCount(0);
		setTrainsWaitTime(0);
		setWrongOutcomeTrainCount(0);
	}

	/**
	 * 
	 * @param gameLength
	 */
	public void setGameLength(double gameLength) {
		this.gameLength = gameLength;
	}

	/**
	 * @param incomeTrainCount
	 *            The incomeTrainCount to set.
	 */
	public void setIncomeTrainCount(int incomeTrainCount) {
		this.incomeTrainCount = incomeTrainCount;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param rightOutcomeTrainCount
	 *            The rightOutcomeTrainCount to set.
	 */
	public void setRightOutcomeTrainCount(int rightOutcomeTrainCount) {
		this.rightOutcomeTrainCount = rightOutcomeTrainCount;
	}

	/**
	 * @param stationName
	 *            the stationName to set
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @param totalLifeTime
	 *            The totalLifeTime to set.
	 */
	public void setTotalLifeTime(double totalLifeTime) {
		this.totalLifeTime = totalLifeTime;
	}

	/**
	 * @param trainsDistance
	 *            The trainsDistance to set.
	 */
	public void setTrainsDistance(double trainsDistance) {
		this.trainsDistance = trainsDistance;
	}

	/**
	 * @param trainsLifeTime
	 *            The trainsLifeTime to set.
	 */
	public void setTrainsLifeTime(double trainsLifeTime) {
		this.trainsLifeTime = trainsLifeTime;
	}

	/**
	 * @param trainsStopCount
	 *            The trainsStopCount to set.
	 */
	public void setTrainsStopCount(int trainsStopCount) {
		this.trainsStopCount = trainsStopCount;
	}

	/**
	 * @param trainsWaitTime
	 *            The trainsWaitTime to set.
	 */
	public void setTrainsWaitTime(double trainsWaitTime) {
		this.trainsWaitTime = trainsWaitTime;
	}

	/**
	 * @param wrongOutcomeTrainCount
	 *            The wrongOutcomeTrainCount to set.
	 */
	public void setWrongOutcomeTrainCount(int wrongOutcomeTrainCount) {
		this.wrongOutcomeTrainCount = wrongOutcomeTrainCount;
	}
}