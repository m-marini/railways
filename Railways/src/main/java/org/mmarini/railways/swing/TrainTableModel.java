package org.mmarini.railways.swing;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.RailwayConstants;
import org.mmarini.railways.model.elements.StationElement;
import org.mmarini.railways.model.train.Train;

/**
 * @author $Author: marco $
 * @version $Id: TrainTableModel.java,v 1.10 2012/02/08 22:03:31 marco Exp $
 */
public class TrainTableModel extends AbstractTableModel implements
		RailwayConstants {
	private static final long serialVersionUID = 1L;
	public static final String UNKONWN_STRING = "???"; //$NON-NLS-1$

	private GameHandler gameHandler;
	private List<Train> trainList;

	/**
	 * 
	 */
	public TrainTableModel() {
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 1:
			return Long.class;
		case 2:
			return Boolean.class;
		default:
			return String.class;
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 6;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return Messages.getString("TrainTableModel.header.name"); //$NON-NLS-1$
		case 1:
			return Messages.getString("TrainTableModel.header.speed"); //$NON-NLS-1$
		case 2:
			return Messages.getString("TrainTableModel.header.arrived"); //$NON-NLS-1$
		case 3:
			return Messages.getString("TrainTableModel.header.location"); //$NON-NLS-1$
		case 4:
			return Messages.getString("TrainTableModel.header.destination"); //$NON-NLS-1$
		case 5:
			return Messages.getString("TrainTableModel.header.status"); //$NON-NLS-1$
		}
		return UNKONWN_STRING;
	}

	/**
	 * @return the gameHandler
	 */
	private GameHandler getGameHandler() {
		return gameHandler;
	}

	/**
	 * @param train
	 * @return
	 */
	private String getLocation(Train train) {
		StationElement element = train.getElementLocation();
		if (element == null)
			return null;
		return element.toString();
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		List<Train> list = trainList;
		if (list == null)
			return 0;
		return list.size();
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		List<Train> list = trainList;
		if (list == null)
			return UNKONWN_STRING;
		Train train = list.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return train.getName();
		case 1:
			return new Long(Math.round(train.getSpeed() / KMH));
		case 2:
			return new Boolean(train.isArrived());
		case 3:
			return getLocation(train);
		case 4:
			return train.getDestination().getName();
		case 5:
			return Messages
					.getString("TrainTableModel.value.status." + train.getStateId()); //$NON-NLS-1$
		}
		return UNKONWN_STRING;
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		GameHandler handler = getGameHandler();
		List<Train> list = handler.getTrains();
		setTrainList(list);
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
		reload();
	}

	/**
	 * @param trainList
	 *            the trainList to set
	 */
	private void setTrainList(List<Train> trainList) {
		this.trainList = trainList;
		fireTableDataChanged();
	}
}