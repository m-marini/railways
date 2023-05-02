package org.mmarini.railways.model.xml;

/**
 * @author $Author: marco $
 * @version $Id: XmlConstants.java,v 1.7 2012/02/08 22:03:33 marco Exp $
 */
public interface XmlConstants {

	public static final String SCHEMA_LOCATION_ATTRIBUTE = "xsi:schemaLocation";

	public static final String SYSTEM_OPTIONS_NS_URI = "http://www.mmarini.org/railways/systemOptions-0.0.1";
	public static final String SYSTEM_OPTIONS_SCHEMA_LOCATION = SYSTEM_OPTIONS_NS_URI
			+ " systemOptions-0.0.1.xsd";
	public static final String SYSTEM_OPTIONS_PREFIX = "so:";
	public static final String SYSTEM_OPTIONS_ELEMENT = SYSTEM_OPTIONS_PREFIX
			+ "systemOptions";
	public static final String LOOK_AND_FEEL_CLASS_ATTRIBUTE = "lookAndFeelClass";
	public static final String TIME_SPEED_ATTRIBUTE = "timeSpeed";
	public static final String MUTE_ATTRIBUTE = "mute";
	public static final String GAIN_ATTRIBUTE = "gain";

	public static final String HALL_OF_FAME_NS_URI = "http://www.mmarini.org/railways/hallOfFame-0.0.1";
	public static final String HALL_OF_FAME__SCHEMA_LOCATION = HALL_OF_FAME_NS_URI
			+ " hallOfFame-0.0.1.xsd";
	public static final String HALL_OF_FAME_PREFIX = "hof:";
	public static final String HALL_OF_FAME_ELEM = HALL_OF_FAME_PREFIX
			+ "hallOfFame";

	public static final String MANAGER_INFOS_ELEMENT = "managerInfos";
	public static final String STATION_NAME_ATTRIBUTE = "stationName";
	public static final String GAME_LENGTH_ATTRIBUTE = "gameLength";
	public static final String TRAINS_WAIT_TIME_ATTRIBUTE = "trainsWaitTime";
	public static final String TRAINS_STOP_COUNT_ATTRIBUTE = "trainsStopCount";
	public static final String TRAINS_LIFETIME_ATTRIBUTE = "trainsLifeTime";
	public static final String TRAINS_DISTANCE_ATTRIBUTE = "trainsDistance";
	public static final String TOTAL_LIFETIME_ATTRIBUTE = "totalLifeTime";
	public static final String WRONG_OUTCOME_TRAIN_COUNT_ATTRIBUTE = "wrongOutcomeTrainCount";
	public static final String RIGHT_OUTCOME_TRAIN_COUNT_ATTRIBUTE = "rightOutcomeTrainCount";
	public static final String INCOME_TRAIN_COUNT_ATTRIBUTE = "incomeTrainCount";
	public static final String TIMESTAMP_ATTRIBUTE = "timestamp";

	public static final String STATION_NS_URI = "http://www.mmarini.org/railways-1.2.5";
	public static final String STATION_PREFIX = "s:";
	public static final String STATION_SCHEMA_LOCATION = STATION_NS_URI
			+ " station-1.2.5.xsd";

	public static final String DESTINATION_ATTRIBUTE = "destination";
	public static final String DEADTRACK_ELEMENT = "deadtrack";
	public static final String PLATFORM_ELEMENT = "platform";
	public static final String INCOME_ELEMENT = "income";
	public static final String OUTCOME_ELEMENT = "outcome";
	public static final String F_ATTRIBUTE = "f";
	public static final String REFERENCE_ATTRIBUTE = "reference";
	public static final String DEVIATED_ATTRIBUTE = "deviated";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String LEFT_VERSUS = "left";
	public static final String VERSUS_ATTRIBUTE = "versus";
	public static final String RIGHT_VERSUS = "right";
	public static final String NODE_ELEMENT = "node";
	public static final String INDEX_ATTRIBUTE = "index";
	public static final String LINKS_ELEMENT = "links";
	public static final String SEGMENT_ELEMENT = "segment";
	public static final String CURVE_ELEMENT = "curve";
	public static final String GAP_ATTRIBUTE = "gap";
	public static final String LENGTH_ATTRIBUTE = "length";
	public static final String SEMAPHORE_ELEMENT = "semaphore";
	public static final String LINE_ELEMENT = "line";
	public static final String POINT_ELEMENT = "point";
	public static final String DEVIATOR_ELEMENT = "deviator";
	public static final String ANGLE_ATTRIBUTE = "angle";
	public static final String CROSS_ELEMENT = "cross";
	public static final String CROSS_DEVIATOR_ELEMENT = "crossDeviator";
	public static final String NAME_ELEMENT = "name";
	public static final String NODES_ELEMENT = "nodes";
	public static final String STATION_REFERENCE_ATTRIBUTE = "reference";
	public static final String STATION_DIRECTION_ATTRIBUTE = "direction";
	public static final String STATION_ELEMENT = "station";
}
