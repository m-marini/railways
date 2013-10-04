package org.mmarini.railways.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mmarini.railways.model.elements.CrossDeviator;
import org.mmarini.railways.model.elements.Deviator;
import org.mmarini.railways.model.elements.Line;
import org.mmarini.railways.model.elements.Semaphore;
import org.mmarini.railways.model.elements.Station;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;
import org.mmarini.railways.model.visitor.ChangeStateVisitor;
import org.mmarini.railways.model.xml.ConfigXMLPersistence;
import org.mmarini.railways.model.xml.StationList;
import org.mmarini.railways.sounds.SoundPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $Author: marco $
 * @version $Id: GameHandlerImpl.java,v 1.6 2012/02/08 22:03:18 marco Exp $
 */
public class GameHandlerImpl implements RailwayConstants, GameHandler {

	private static Logger log = LoggerFactory.getLogger(GameHandlerImpl.class);

	private StationList stationList;
	private Game game;
	private ConfigXMLPersistence configXMLPersistence;
	private SoundPlayer soundPlayer;
	private boolean autoLock;

	/**
	 * 
	 */
	public GameHandlerImpl() {
		stationList = new StationList();
		configXMLPersistence = new ConfigXMLPersistence();
		soundPlayer = new SoundPlayer();
		SystemOptions systemOptions = configXMLPersistence.getSystemOptions();
		soundPlayer.setMute(systemOptions.isMute());
		soundPlayer.setGain(systemOptions.getGain());
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#changeState(org.mmarini.railways
	 *      .model.elements.StationElement)
	 */
	@Override
	public void changeState(StationElement element) {
		element.accept(ChangeStateVisitor.getInstance());
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#changeState(org.mmarini.railways
	 *      .model.train.Train)
	 */
	@Override
	public void changeState(Train train) {
		if (train.isStop()) {
			train.reverse();
			train.run();
		}
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#createNewGame(org.mmarini.railways.model.GameParameters)
	 */
	@Override
	public void createNewGame(GameParameters parms) {
		try {
			GameImpl game = new GameImpl();
			ManagerInfos infos = new ManagerInfos();
			infos.setGameLength(parms.getGameLength() * SPM);
			String stationName = parms.getStationName();
			infos.setStationName(stationName);
			game.setManagerInfos(infos);
			Station station = stationList.loadStation(stationName);
			station.setSoundPlayer(soundPlayer);
			station.setTrainFrequence(parms.getTrainFrequence()
					* FREQUENCE_TRAIN_SCALE);
			station.setAutoLock(autoLock);
			game.setStation(station);
			game.setTimeSpeed(getTimeSpeed());
			game.reset();
			this.game = game;
		} catch (Exception e) {
			log.error("Error loading station ", e);
		}
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#getGain()
	 */
	@Override
	public float getGain() {
		SystemOptions systemOptions = getSystemOptions();
		return systemOptions.getGain();
	}

	/**
	 * 
	 * @return
	 */
	public Game getGame() {
		return game;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#getLookAndFeelClass()
	 */
	@Override
	public String getLookAndFeelClass() {
		SystemOptions systemOptions = getSystemOptions();
		return systemOptions.getLookAndFeelClass();
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#getManagerInfos()
	 */
	@Override
	public ManagerInfos getManagerInfos() {
		Game game = getGame();
		if (game != null)
			return game.getManagerInfos();
		return new ManagerInfos();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#getStation()
	 */
	@Override
	public Station getStation() {
		return getGame().getStation();
	}

	/**
	 * @return
	 */
	private SystemOptions getSystemOptions() {
		return configXMLPersistence.getSystemOptions();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#getTimeSpeed()
	 */
	@Override
	public double getTimeSpeed() {
		SystemOptions systemOptions = getSystemOptions();
		return systemOptions.getTimeSpeed();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#getTrains()
	 */
	@Override
	public List<Train> getTrains() {
		Game game = getGame();
		if (game == null)
			return new ArrayList<Train>(0);
		return game.getStation().getSortedTrainList();
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#handleTimer()
	 */
	@Override
	public void handleTimer() {
		getGame().handleTimer();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#isGameEnded()
	 */
	@Override
	public boolean isGameEnded() {
		return getManagerInfos().isGameEnded();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#isMute()
	 */
	@Override
	public boolean isMute() {
		SystemOptions systemOptions = getSystemOptions();
		return systemOptions.isMute();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#isNewEntry()
	 */
	@Override
	public boolean isNewEntry() {
		return loadHallOfFame().isNewEntry(getManagerInfos());
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#loadHallOfFame()
	 */
	@Override
	public HallOfFame loadHallOfFame() {
		return configXMLPersistence.getHallOfFame();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#loadStationList()
	 */
	@Override
	public Set<String> loadStationList() {
		log.debug("loadStationList");
		return stationList.getStationNames();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#lockAllSemaphores()
	 */
	@Override
	public void lockAllSemaphores() {
		log.debug("lockAllSemaphores");
		Game game = getGame();
		if (game != null)
			game.lockAllSemaphores();
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#saveHallOfFame()
	 */
	@Override
	public void saveHallOfFame() {
		ConfigXMLPersistence persistence = configXMLPersistence;
		HallOfFame hof = persistence.getHallOfFame();
		hof.addNewEntry(getManagerInfos());
		persistence.setHallOfFame(hof);
	}

	/**
	 * 
	 * @param systemOptions
	 */
	private void saveSystemOption(SystemOptions systemOptions) {
		configXMLPersistence.setSystemOptions(systemOptions);
	}

	@Override
	public void select(CrossDeviator element) {
		if (!element.isBusy()) {
			element.setDeviated(!element.isDeviated());
			soundPlayer.playDeviator();
		}
	}

	@Override
	public void select(Deviator element) {
		if (!element.isBusy()) {
			element.setDeviated(!element.isDeviated());
			soundPlayer.playDeviator();
		}
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#select(org.mmarini.railways.model
	 *      .elements.Line, int)
	 */
	@Override
	public void select(Line element, int index) {
		if (index == 0) {
			element.setHeld(index, !element.isHeld(index));
		}
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#select(org.mmarini.railways.model
	 *      .elements.Semaphore, int)
	 */
	@Override
	public void select(Semaphore element, int index) {
		element.setLocked(index, !element.isLocked(index));
	}

	/**
	 * @see org.mmarini.railways.model.GameHandler#select(org.mmarini.railways.model
	 *      .train.Train)
	 */
	@Override
	public void select(Train train) {
		if (train.isBrakeing()) {
			train.run();
		} else {
			train.brake();
		}
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setAutoLock(boolean)
	 */
	@Override
	public void setAutoLock(boolean autoLock) {
		log.debug("setAutoLock " + autoLock);
		this.autoLock = autoLock;
		Game game = getGame();
		if (game == null)
			return;
		game.getStation().setAutoLock(autoLock);
	}

	/**
	 * @param configXMLPersistence
	 *            the configXMLPersistence to set
	 */
	public void setConfigXMLPersistence(
			ConfigXMLPersistence configXMLPersistence) {
		this.configXMLPersistence = configXMLPersistence;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setGain(float)
	 */
	@Override
	public void setGain(float volume) {
		soundPlayer.setGain(volume);
		SystemOptions systemOptions = getSystemOptions();
		systemOptions.setGain(volume);
		saveSystemOption(systemOptions);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setLookAndFeelClass(java.lang.String)
	 */
	@Override
	public void setLookAndFeelClass(String lookAndFeelClass) {
		SystemOptions systemOptions = getSystemOptions();
		systemOptions.setLookAndFeelClass(lookAndFeelClass);
		saveSystemOption(systemOptions);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setMute(boolean)
	 */
	@Override
	public void setMute(boolean mute) {
		log.debug("setMute " + mute);
		soundPlayer.setMute(mute);
		SystemOptions systemOptions = getSystemOptions();
		systemOptions.setMute(mute);
		saveSystemOption(systemOptions);
	}

	/**
	 * @param soundPlayer
	 *            the soundPlayer to set
	 */
	public void setSoundPlayer(SoundPlayer soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#setTimeSpeed(double)
	 */
	@Override
	public void setTimeSpeed(double timeSpeed) {
		Game game = getGame();
		if (game != null)
			game.setTimeSpeed(timeSpeed);
		SystemOptions systemOptions = getSystemOptions();
		systemOptions.setTimeSpeed(timeSpeed);
		saveSystemOption(systemOptions);
	}

	/**
	 * 
	 * @see org.mmarini.railways.model.GameHandler#stopAllTrains()
	 */
	@Override
	public void stopAllTrains() {
		log.debug("stopAllTrains");
		Game game = getGame();
		if (game != null)
			game.stopAllTrains();
	}
}