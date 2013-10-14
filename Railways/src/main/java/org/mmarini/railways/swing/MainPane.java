package org.mmarini.railways.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.mmarini.railways.model.GameHandler;
import org.mmarini.railways.model.GameParameters;
import org.mmarini.railways.model.graphics.StationGraphEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $Author: marco $
 * @version $Id: MainPane.java,v 1.11 2012/02/08 22:03:32 marco Exp $
 */
public class MainPane extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final int REFRESH_TIME = 20;

	private static Logger log = LoggerFactory.getLogger(MainPane.class);

	private JSplitPane verticalSplitPane;
	private JSplitPane upperPane;
	private JStationGlobal globalPane;
	private InfosPane infoPane;
	private JStationDetails detailsPane;
	private GameDialog gameDialog;
	private OptionsPane optionsPane;
	private GameHandler gameHandler;
	private SummaryPane summaryPane;
	private HallOfFamePane hallOfFamePane;
	private Timer timer;

	/**
	 * 
	 */
	public MainPane() {
		globalPane = new JStationGlobal();
		optionsPane = new OptionsPane();
		gameDialog = new GameDialog();
		hallOfFamePane = new HallOfFamePane();
		summaryPane = new SummaryPane();
		infoPane = new InfosPane();
		detailsPane = new JStationDetails();
		timer = new Timer(REFRESH_TIME, new ActionListener() {

			/**
			 * 
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				handleTimer();
			}
		});
		init();
	}

	/**
	 * 
	 * 
	 */
	private void createUpperPane() {
		JSplitPane pane = new JSplitPane();
		pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		pane.setOneTouchExpandable(true);
		pane.setResizeWeight(1);
		pane.setLeftComponent(globalPane);
		pane.setRightComponent(infoPane);
		this.upperPane = pane;
	}

	/**
	 * 
	 * 
	 */
	private void createVerticalSplitPane() {
		JSplitPane pane = new JSplitPane();
		pane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		pane.setOneTouchExpandable(true);
		pane.setResizeWeight(0);
		pane.setTopComponent(getUpperPane());
		pane.setBottomComponent(detailsPane);
		this.verticalSplitPane = pane;
	}

	/**
	 * @return Returns the upperPane.
	 */
	private JSplitPane getUpperPane() {
		if (upperPane == null) {
			createUpperPane();
		}
		return upperPane;
	}

	/**
	 * @return Returns the verticalSplitPane.
	 */
	private JSplitPane getVerticalSplitPane() {
		if (verticalSplitPane == null)
			createVerticalSplitPane();
		return verticalSplitPane;
	}

	/**
	 * @see org.mmarini.railways.swing.UserHandler#handleAboutAction()
	 */
	public boolean handleAboutAction() {
		String text = Messages.getString("MainPane.about.text"); //$NON-NLS-1$
		text = MessageFormat
				.format(text,
						new Object[] {
								Messages.getString("Railways.name"), Messages.getString("Railways.version"), //$NON-NLS-1$ //$NON-NLS-2$
								Messages.getString("Railways.author") }); //$NON-NLS-1$
		JOptionPane.showConfirmDialog(this, text,
				Messages.getString("MainPane.about.title"), //$NON-NLS-1$
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		return false;
	}

	/**
	 * @see org.mmarini.railways.swing.UserHandler#handleExitAction()
	 */
	public boolean handleExitAction() {
		timer.stop();
		return true;
	}

	/**
	 * 
	 * 
	 */
	private void handleGameEnded() {
		SummaryPane pane = summaryPane;
		pane.reload();
		pane.setBorder(BorderFactory.createEtchedBorder());
		showMessage(Messages.getString("MainPane.summaryDialog.title"), pane); //$NON-NLS-1$
		pane.apply();
		GameHandler handler = gameHandler;
		if (handler.isNewEntry()) {
			handler.saveHallOfFame();
			hallOfFamePane.reload();
			showMessage("Hall Of Fame", hallOfFamePane);
			infoPane.reload();
		}
	}

	/**
	 * 
	 */
	public void handleLAFChanged() {
		SwingUtilities.updateComponentTreeUI(gameDialog);
		SwingUtilities.updateComponentTreeUI(optionsPane);
		SwingUtilities.updateComponentTreeUI(summaryPane);
		SwingUtilities.updateComponentTreeUI(hallOfFamePane);
	}

	/**
	 * @see org.mmarini.railways.swing.UserHandler#handleNewGameAction()
	 */
	public boolean handleNewGameAction() {
		GameDialog dialog = gameDialog;
		dialog.setVisible(true);
		if (!dialog.isCompleted())
			return false;

		GameParameters parms = dialog.createGameParameters();
		GameHandler handler = gameHandler;
		handler.createNewGame(parms);
		setGameHandler(handler);
		infoPane.reload();
		reloadStation();
		return true;
	}

	/**
	 * @see org.mmarini.railways.swing.UserHandler#handleOptionsAction()
	 */
	public boolean handleOptionsAction() {
		OptionsPane optionPane = optionsPane;
		optionPane.reload();
		optionPane.doLayout();
		if (!showConfirm(
				Messages.getString("MainPane.optionDialog.title"), optionPane)) //$NON-NLS-1$
			return false;
		optionPane.apply();
		return true;
	}

	/**
	 * @param ev
	 */
	private void handlePointSelected(StationGraphEvent ev) {
		detailsPane.setDetailsLocation(ev.getLocation());
	}

	/**
	 * 
	 */
	private void handleTimer() {
		try {
			GameHandler handler = gameHandler;
			if (handler.isGameEnded()) {
				return;
			}
			handler.handleTimer();
			if (handler.isGameEnded()) {
				handleGameEnded();
			}
		} catch (Throwable e) {
			LoggerFactory.getLogger(getClass()).error("Error", e); //$NON-NLS-1$
			if (JOptionPane
					.showConfirmDialog(
							this,
							new Object[] {
									e.getMessage(),
									"", Messages.getString("MainPane.error.text") }, Messages.getString("MainPane.error.title"), //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION)
				System.exit(1);
		}
		detailsPane.repaint();
		globalPane.repaint();
		infoPane.reload();
	}

	/**
	 * 
	 */
	private void init() {
		log.debug("init"); //$NON-NLS-1$
		setLayout(new BorderLayout());
		add(getVerticalSplitPane(), BorderLayout.CENTER);
		globalPane.addStationGraphListener(new StationGraphListener() {

			@Override
			public void elementSelected(StationGraphEvent event) {
			}

			@Override
			public void elementStateChanged(StationGraphEvent ev) {
			}

			@Override
			public void pointSelected(StationGraphEvent ev) {
				handlePointSelected(ev);
			}
		});
	}

	/**
	 * 
	 */
	private void reloadStation() {
		detailsPane.showStation(gameHandler.getStation());
		globalPane.showStation(gameHandler.getStation());
		detailsPane.doLayout();
		timer.start();
	}

	/**
	 * @param handler
	 *            The handler to set.
	 */
	public void setGameHandler(GameHandler handler) {
		this.gameHandler = handler;
		optionsPane.setGameHandler(gameHandler);
		gameDialog.setGameHandler(gameHandler);
		hallOfFamePane.setGameHandler(gameHandler);
		summaryPane.setGameHandler(gameHandler);
		infoPane.setGameHandler(gameHandler);
		detailsPane.setGameHandler(gameHandler);
	}

	/**
	 * @param title
	 * @param content
	 * @return
	 */
	private boolean showConfirm(String title, JComponent content) {
		int selectedValue = JOptionPane.showConfirmDialog(this, content, title,
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		return selectedValue == JOptionPane.OK_OPTION;
	}

	/**
	 * 
	 * @param title
	 * @param content
	 * @return
	 */
	private void showMessage(String title, JComponent content) {
		JOptionPane.showMessageDialog(this, content, title,
				JOptionPane.PLAIN_MESSAGE);
	}
}