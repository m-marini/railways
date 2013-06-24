package org.mmarini.railways.swing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.HallOfFame;
import org.mmarini.railways.model.ManagerInfos;
import org.mmarini.railways.model.RailwayConstants;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFameModel.java,v 1.4 2012/02/08 22:03:31 marco Exp $
 */
public class HallOfFameModel extends AbstractTableModel implements
		RailwayConstants {

	public static final String UNKNOWN = "???"; //$NON-NLS-1$
	private static final long serialVersionUID = -4709768516955635414L;
	private static Log log = LogFactory.getLog(HallOfFameModel.class);

	private List<ManagerInfos> entries;
	private GameHandler gameHandler;

	/**
	 * 
	 */
	public HallOfFameModel() {
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 0:
		case 5:
			return Integer.class;
		case 2:
			return Date.class;
		case 3:
		case 4:
		case 6:
			return Double.class;
		}
		return String.class;
	}

	/**
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 8;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return Messages.getString("HallOfFameModel.columm.name"); //$NON-NLS-1$
		case 1:
			return Messages.getString("HallOfFameModel.player.name"); //$NON-NLS-1$
		case 2:
			return Messages.getString("HallOfFameModel.date.name"); //$NON-NLS-1$
		case 3:
			return Messages.getString("HallOfFameModel.performance.name"); //$NON-NLS-1$
		case 4:
			return Messages.getString("HallOfFameModel.gameLength.name"); //$NON-NLS-1$
		case 5:
			return Messages.getString("HallOfFameModel.trains.name"); //$NON-NLS-1$
		case 6:
			return Messages.getString("HallOfFameModel.frequence.name"); //$NON-NLS-1$
		case 7:
			return Messages.getString("HallOfFameModel.station.name"); //$NON-NLS-1$
		}
		return UNKNOWN;
	}

	/**
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		List<ManagerInfos> hallOfFame = entries;
		if (hallOfFame == null)
			return 0;
		return hallOfFame.size();
	}

	/**
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		ManagerInfos entry = entries.get(row);
		switch (col) {
		case 0:
			return row + 1;
		case 1:
			return entry.getName();
		case 2:
			return new Date(entry.getTimestamp());
		case 3:
			return entry.getPerformance() * SPH;
		case 4:
			return entry.getTotalLifeTime() / SPM;
		case 5:
			return entry.getRightOutcomeTrainCount();
		case 6:
			return entry.getFrequence() * SPH;
		case 7:
			return entry.getStationName();
		}
		return UNKNOWN;
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		log.debug("reload"); //$NON-NLS-1$
		HallOfFame hof = gameHandler.loadHallOfFame();
		entries = new ArrayList<ManagerInfos>(hof.getEntries());
		fireTableDataChanged();
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
		reload();
	}
}
