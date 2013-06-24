package org.mmarini.railways.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.GameParameters;
import org.mmarini.railways.model.GameParametersImpl;

/**
 * @author $Author: marco $
 * @version $Id: GameDialog.java,v 1.3 2012/02/08 22:03:31 marco Exp $
 */
public class GameDialog extends AbstractOKCancelDialog {
	public static final int DEFAULT_GAME_LEVEL_INDEX = 1;
	public static final int DEFAULT_GAME_LENGTH_INDEX = 1;
	public static final String NONE_MESSAGE = "";
	public static final String CUSTOM_GAME_LEVEL = "GameDialog.gameLevel.custom"; //$NON-NLS-1$
	public static final String HARD_GAME_LEVEL = "GameDialog.gameLevel.hard"; //$NON-NLS-1$
	public static final String MEDIUM_GAME_LEVEL = "GameDialog.gameLevel.medium"; //$NON-NLS-1$
	public static final String EASY_GAME_LEVEL = "GameDialog.gameLevel.easy"; //$NON-NLS-1$
	public static final String CUSTOM_GAME_LENGTH = "GameDialog.gameLength.custom"; //$NON-NLS-1$
	public static final String LONG_GAME_LENGTH = "GameDialog.gameLength.long"; //$NON-NLS-1$
	public static final String MEDIUM_GAME_LENGTH = "GameDialog.gameLength.medium"; //$NON-NLS-1$
	public static final String SHORT_GAME_LENGTH = "GameDialog.gameLength.short"; //$NON-NLS-1$

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(GameDialog.class);

	private DefaultListModel stationModel;
	private JList stationList;
	private JScrollPane stationScrollPane;
	private MessagesListModel gameLengthModel;
	private JList gameLengthList;
	private JScrollPane gameLengthScrollPane;
	private JTextField gameLengthField;
	private MessagesListModel gameLevelModel;
	private JList gameLevelList;
	private JScrollPane gameLevelScrollPane;
	private JTextField frequenceField;
	private JLabel info;
	private GameHandler gameHandler;

	/**
	 * 
	 */
	public GameDialog() {
		stationModel = new DefaultListModel();
		stationList = new JList(stationModel);
		stationScrollPane = new JScrollPane(stationList);
		gameLengthModel = new MessagesListModel();
		gameLengthList = new JList(gameLengthModel);
		gameLengthScrollPane = new JScrollPane(gameLengthList);
		gameLengthField = new JTextField();
		gameLevelModel = new MessagesListModel();
		gameLevelList = new JList(gameLevelModel);
		gameLevelScrollPane = new JScrollPane(gameLevelList);
		frequenceField = new JTextField();
		info = new JLabel();

		setTitle(Messages.getString("GameDialog.title")); //$NON-NLS-1$
		init();
	}

	/**
	 * 
	 * @param msg
	 * @param key
	 * @param text
	 */
	private void appendMessage(StringBuffer msg, String key, String text) {
		if (msg.length() > 0)
			msg.append(", ");
		String fmt = Messages.getString(key);
		MessageFormat msgFmt = new MessageFormat(fmt, getLocale());
		msg.append(msgFmt.format(new Object[] { text }));
	}

	/**
	 * 
	 * @see org.mmarini.railways.swing.AbstractOKCancelDialog#createDialogContent()
	 */
	@Override
	protected JPanel createDialogContent() {
		JPanel content = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		content.setLayout(gbl);
		content.setBorder(BorderFactory.createEtchedBorder());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		JComponent comp;

		comp = new JLabel(Messages.getString("GameDialog.stationList.title")); //$NON-NLS-1$
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = stationScrollPane;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JSeparator();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JLabel(Messages.getString("GameDialog.gameLength.title")); //$NON-NLS-1$
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = gameLengthScrollPane;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JLabel(
				Messages.getString("GameDialog.gameLengthField.label.text")); //$NON-NLS-1$
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = gameLengthField;
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JSeparator();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JLabel(Messages.getString("GameDialog.gameLevel.title")); //$NON-NLS-1$
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = gameLevelScrollPane;
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JLabel(
				Messages.getString("GameDialog.trainFrequenceField.label.text")); //$NON-NLS-1$
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = frequenceField;
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JSeparator();
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = info;
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);
		return content;
	}

	/**
	 * 
	 * @return
	 */
	public GameParameters createGameParameters() {
		GameParametersImpl parms = new GameParametersImpl();
		parms.setStationName(stationList.getSelectedValue().toString());
		try {
			parms.setGameLength(parseGameLength().doubleValue());
			parms.setTrainFrequence(getTrainFrequence());
			return parms;
		} catch (ParseException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	private double getTrainFrequence() throws ParseException {
		double value = parseGameLevel().doubleValue();
		log.debug("value=" + value);
		return value;
	}

	/**
	 * 
	 * @param e
	 */
	private void handleGameLengthChanged(ListSelectionEvent e) {
		log.debug("handleGameLengthChanged");
		JTextField field = gameLengthField;
		if (isCustomGameLength()) {
			field.setEditable(true);
			field.setEnabled(true);
		} else {
			field.setEditable(false);
			field.setEnabled(false);
		}
		validateInput();
	}

	/**
	 * 
	 * @param e
	 */
	private void handleGameLevelChanged(ListSelectionEvent e) {
		log.debug("handleGameLengthChanged");
		JTextField field = frequenceField;
		if (isCustomGameLevel()) {
			field.setEditable(true);
			field.setEnabled(true);
		} else {
			field.setEditable(false);
			field.setEnabled(false);
		}
		validateInput();
	}

	/**
	 * @param e
	 */
	private void handleStationListChanged(ListSelectionEvent e) {
		validateInput();
	}

	/**
	 * 
	 * 
	 */
	@Override
	protected void init() {

		/*
		 * StationList init
		 */
		JList list = stationList;
		DefaultListModel model = stationModel;
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleStationListChanged(e);
			}
		});
		list.setModel(model);

		/*
		 * GameLengthList init
		 */
		MessagesListModel gameLengthModel = this.gameLengthModel;
		gameLengthModel.add(SHORT_GAME_LENGTH);
		gameLengthModel.add(MEDIUM_GAME_LENGTH);
		gameLengthModel.add(LONG_GAME_LENGTH);
		gameLengthModel.add(CUSTOM_GAME_LENGTH);
		list = gameLengthList;
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleGameLengthChanged(e);
			}
		});
		list.setSelectedIndex(DEFAULT_GAME_LENGTH_INDEX);

		JTextField field = gameLengthField;
		field.setColumns(5);
		field.setEditable(false);
		field.setEnabled(false);
		DocumentListener docList = new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				validateInput();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validateInput();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validateInput();
			}

		};
		field.getDocument().addDocumentListener(docList);

		/*
		 * DifficultList init
		 */
		MessagesListModel gameLevelModel = this.gameLevelModel;
		gameLevelModel.add(EASY_GAME_LEVEL);
		gameLevelModel.add(MEDIUM_GAME_LEVEL);
		gameLevelModel.add(HARD_GAME_LEVEL);
		gameLevelModel.add(CUSTOM_GAME_LEVEL);

		list = gameLevelList;
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				handleGameLevelChanged(e);
			}
		});
		list.setSelectedIndex(DEFAULT_GAME_LEVEL_INDEX);

		field = frequenceField;
		field.setColumns(8);
		field.setEditable(false);
		field.setEnabled(false);
		field.getDocument().addDocumentListener(docList);

		getOkAction().setEnabled(false);

		info.setForeground(Color.RED);

		super.init();
		stationList.setSelectedIndex(0);
	}

	/**
	 * 
	 */
	private void init2() {
		try {
			GameHandler handler = gameHandler;
			Set<String> locations = handler.loadStationList();
			setStationName(locations);
		} catch (Exception e) {
			log.error(Messages.getString("GameDialog.loadStation.error"), e); //$NON-NLS-1$
		}
	}

	/**
	 * @return
	 */
	private boolean isCustomGameLength() {
		boolean customGameLength;
		int idx = gameLengthList.getSelectedIndex();
		if (idx < 0)
			return false;
		String key = gameLengthModel.getValue(idx);
		customGameLength = CUSTOM_GAME_LENGTH.equals(key);
		return customGameLength;
	}

	/**
	 * @return
	 */
	private boolean isCustomGameLevel() {
		int idx = gameLevelList.getSelectedIndex();
		if (idx < 0)
			return false;
		String key = gameLevelModel.getValue(idx);
		boolean isCustomGameLevel = CUSTOM_GAME_LEVEL.equals(key);
		return isCustomGameLevel;
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	private Number parseGameLength() throws ParseException {
		int idx = gameLengthList.getSelectedIndex();
		if (idx < 0) {
			return null;
		}
		if (isCustomGameLength()) {
			JTextField field = gameLengthField;
			String text = field.getText();
			NumberFormat fmt = NumberFormat
					.getNumberInstance(field.getLocale());
			Number value = fmt.parse(text);
			return value;
		}
		String key = gameLengthModel.getValue(idx);
		return new Double(Messages.getString(key + ".value"));
	}

	/**
	 * 
	 * @return
	 * @throws ParseException
	 */
	private Number parseGameLevel() throws ParseException {
		int idx = gameLevelList.getSelectedIndex();
		if (idx < 0) {
			return null;
		}
		if (isCustomGameLevel()) {
			JTextField field = frequenceField;
			NumberFormat fmt = NumberFormat
					.getNumberInstance(field.getLocale());
			return fmt.parse(field.getText());
		}
		String key = gameLevelModel.getValue(idx);
		return new Double(Messages.getString(key + ".value"));
	}

	/**
	 * @param handler
	 *            the handler to set
	 */
	public void setGameHandler(GameHandler handler) {
		this.gameHandler = handler;
		init2();
	}

	/**
	 * 
	 * @param locations
	 */
	private void setStationName(Set<String> locations) {
		List<String> list = new ArrayList<String>(locations);
		Collections.sort(list);
		stationModel.clear();
		for (String name : list) {
			stationModel.addElement(name);
		}
	}

	/**
	 * 
	 * 
	 */
	private void validateInput() {
		log.debug("validate");
		StringBuffer msg = new StringBuffer();
		boolean validInput = true;
		if (stationList.getSelectedIndex() < 0) {
			validInput = false;
		}
		try {
			if (parseGameLength() == null) {
				validInput = false;
			}
		} catch (ParseException e1) {
			appendMessage(msg, "GameDialog.gameLength.error",
					gameLengthField.getText());
			validInput = false;
		}
		try {
			if (parseGameLevel() == null) {
				validInput = false;
			}
		} catch (ParseException e1) {
			appendMessage(msg, "GameDialog.gameLevel.error",
					frequenceField.getText());
			validInput = false;
		}
		JLabel info = this.info;
		info.setText(msg.toString());
		Action action = getOkAction();
		if (validInput) {
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}
}