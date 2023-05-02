package org.mmarini.railways.model;

public class GameParametersImpl implements GameParameters {
	private double gameLength;
	private double trainFrequence;
	private String stationName;

	/**
	 * 
	 * @see org.mmarini.railways.model.GameParameters#getGameLength()
	 */
	@Override
	public double getGameLength() {
		return gameLength;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameParameters#getStationName()
	 */
	@Override
	public String getStationName() {
		return stationName;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameParameters#getTrainFrequence()
	 */
	@Override
	public double getTrainFrequence() {
		return trainFrequence;
	}

	/**
	 * @param gameLength
	 *            the gameLength to set
	 */
	public void setGameLength(double gameLength) {
		this.gameLength = gameLength;
	}

	/**
	 * @param stationName
	 *            the stationName to set
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	/**
	 * @param trainFrequence
	 *            the trainFrequence to set
	 */
	public void setTrainFrequence(double trainFrequence) {
		this.trainFrequence = trainFrequence;
	}
}
