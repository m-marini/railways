package org.mmarini.railways.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;
import org.xml.sax.SAXException;

public interface GameHandler {

	/**
	 * 
	 * @param element
	 */
	public abstract void changeState(StationElement element);

	/**
	 * 
	 * @param train
	 */
	public abstract void changeState(Train train);

	/**
	 * 
	 * @param parms
	 */
	public abstract void createNewGame(GameParameters parms);

	/**
	 * 
	 * @param volume
	 */
	public abstract float getGain();

	/**
	 * 
	 * @return
	 */
	public abstract String getLookAndFeelClass();

	/**
	 * @return Returns the managerInfos.
	 */
	public abstract ManagerInfos getManagerInfos();

	/**
	 * @return Returns the station.
	 */
	public abstract Station getStation();

	/**
	 * @return Returns the timeSpeed.
	 */
	public abstract double getTimeSpeed();

	/**
	 * 
	 * @return
	 */
	public abstract List<Train> getTrains();

	/**
	 * 
	 */
	public abstract void handleTimer();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isGameEnded();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isMute();

	/**
	 * 
	 * @return
	 */
	public abstract boolean isNewEntry();

	/**
	 * 
	 * 
	 */
	public abstract HallOfFame loadHallOfFame();

	/**
	 * @throws IOException
	 * @throws ValidationException
	 * @throws SAXException
	 */
	public abstract Set<String> loadStationList();

	/**
	 * 
	 * 
	 */
	public abstract void lockAllSemaphores();

	/**
	 * 
	 * @param hallOfFame
	 */
	public abstract void saveHallOfFame();

	/**
	 * 
	 * @param element
	 */
	public abstract void select(CrossDeviator element);

	/**
	 * 
	 * @param element
	 */
	public abstract void select(Deviator element);

	/**
	 * 
	 * @param element
	 * @param index
	 */
	public abstract void select(Line element, int index);

	/**
	 * 
	 * @param element
	 * @param index
	 */
	public abstract void select(Semaphore element, int index);

	/**
	 * 
	 * @param train
	 */
	public abstract void select(Train train);

	/**
	 * 
	 * @param autoLock
	 */
	public abstract void setAutoLock(boolean autoLock);

	/**
	 * 
	 * @param volume
	 */
	public abstract void setGain(float volume);

	/**
	 * 
	 * @return
	 */
	public abstract void setLookAndFeelClass(String lookAndFeelName);

	/**
	 * 
	 * @param mute
	 */
	public abstract void setMute(boolean mute);

	/**
	 * @param timeSpeed
	 *            The timeSpeed to set.
	 */
	public abstract void setTimeSpeed(double timeSpeed);

	/**
	 * 
	 * 
	 */
	public abstract void stopAllTrains();
}