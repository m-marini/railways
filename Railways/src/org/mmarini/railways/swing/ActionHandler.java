package org.mmarini.railways.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.mmarini.railways.model.GameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $Author: marco $
 * @version $Id: ActionHandler.java,v 1.11 2012/02/08 22:03:32 marco Exp $
 */
public class ActionHandler {
	/**
	 * 
	 * @author US00852
	 * @version $Id: ActionHandler.java,v 1.10.14.1 2012/02/04 19:22:55 marco
	 *          Exp $
	 */
	class LookAndFeelAction implements ActionListener {
		private LookAndFeelInfo lafInfo;

		/**
		 * 
		 * @param lafInfo
		 */
		public LookAndFeelAction(LookAndFeelInfo lafInfo) {
			this.lafInfo = lafInfo;
		}

		/**
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			handleChangeLookAndFeel(lafInfo);
		}
	}

	public static final String DISABLED_ICON = "selectedIcon";

	private static Logger log = LoggerFactory.getLogger(ActionHandler.class);

	private JMenu lookAndFeelMenu;
	private JMenuBar gameMenuBar;
	private JToolBar gameToolBar;
	private GameFrame gameFrame;
	private GameHandler gameHandler;
	private LookAndFeelListener lookAndFeelListener;
	private JToggleButton.ToggleButtonModel muteModel = new JToggleButton.ToggleButtonModel();
	private JToggleButton.ToggleButtonModel autoLockModel = new JToggleButton.ToggleButtonModel();

	private Action lockAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			gameHandler.lockAllSemaphores();
		}
	};

	private Action stopAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			gameHandler.stopAllTrains();
		}
	};

	private Action aboutAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			gameFrame.handleAboutAction();
		}
	};
	private Action muteAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	private Action autoLockAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	private Action optionsAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			gameFrame.handleOptionsAction();
		}

	};

	private Action exitAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			gameFrame.handleExitAction();
		}

	};
	private Action newGameAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			handleNewGameAction();
		}

	};

	/**
	 * 
	 */
	public ActionHandler() {
	}

	/**
	 * @param menu
	 * @param action
	 * @param model
	 */
	private void addMenuBar(JMenu menu, Action action, ButtonModel model) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
		setToogleButton(action, model, item);
		menu.add(item);
	}

	/**
	 * 
	 */
	private void addToolbarButton(Action action) {
		JButton btn = new JButton(action);
		if (action.getValue(Action.SMALL_ICON) != null)
			btn.setText(null);
		getGameToolBar().add(btn);
	}

	/**
	 * @param action
	 */
	private void addToolbarToogleButton(Action action, ButtonModel model) {
		JToggleButton btn = new JToggleButton(action);
		setToogleButton(action, model, btn);
		if (action.getValue(Action.SMALL_ICON) != null)
			btn.setText(null);
		getGameToolBar().add(btn);
	}

	/**
	 * 
	 */
	private void createGameMenuBar() {
		gameMenuBar = new JMenuBar();

		/*
		 * File
		 */
		JMenu menu = new JMenu(
				Messages.getString("ActionHandler.fileMenu.name")); //$NON-NLS-1$
		menu.setMnemonic(Messages
				.getString("ActionHandler.fileMenu.mnemonic").charAt(0)); //$NON-NLS-1$
		gameMenuBar.add(menu);

		menu.add(new JMenuItem(newGameAction));
		menu.add(new JPopupMenu.Separator());

		menu.add(new JMenuItem(exitAction));

		/*
		 * Tools
		 */
		menu = new JMenu(Messages.getString("ActionHandler.toolsMenu.name")); //$NON-NLS-1$
		menu.setMnemonic(Messages
				.getString("ActionHandler.toolsMenu.mnemonic").charAt(0)); //$NON-NLS-1$
		addMenuBar(menu, muteAction, muteModel);
		menu.add(new JMenuItem(lockAction));
		menu.add(new JMenuItem(stopAction));
		addMenuBar(menu, autoLockAction, autoLockModel);
		menu.add(new JMenuItem(optionsAction));

		gameMenuBar.add(menu);

		/*
		 * View
		 */
		menu = new JMenu(Messages.getString("ActionHandler.viewMenu.name")); //$NON-NLS-1$
		menu.setMnemonic(Messages
				.getString("ActionHandler.viewMenu.mnemonic").charAt(0)); //$NON-NLS-1$
		menu.add(lookAndFeelMenu);
		gameMenuBar.add(menu);

		/*
		 * Help
		 */
		menu = new JMenu(Messages.getString("ActionHandler.helpMenu.name")); //$NON-NLS-1$
		menu.setMnemonic(Messages
				.getString("ActionHandler.helpMenu.mnemonic").charAt(0)); //$NON-NLS-1$
		gameMenuBar.add(menu);

		menu.add(new JMenuItem(aboutAction));
	}

	/**
	 * 
	 */
	private void createGameToolBar() {
		gameToolBar = new JToolBar();

		addToolbarButton(newGameAction);

		gameToolBar.add(new JToolBar.Separator());
		addToolbarButton(lockAction);
		addToolbarButton(stopAction);
		addToolbarToogleButton(autoLockAction, autoLockModel);

		gameToolBar.add(new JToolBar.Separator());
		Action action = muteAction;
		addToolbarToogleButton(action, muteModel);

		addToolbarButton(optionsAction);

		gameToolBar.add(new JToolBar.Separator());
		addToolbarButton(exitAction);

	}

	/**
	 * 
	 * @return
	 */
	private void createLookAndFeelMenu() {
		JMenu menu = new JMenu(Messages.getString("ActionHandler.lafMenu.name"));
		menu.setMnemonic(Messages
				.getString("ActionHandler.lafMenu.mnemonic").charAt(0)); //$NON-NLS-1$
		LookAndFeelInfo[] entries = UIManager.getInstalledLookAndFeels();
		ButtonGroup group = new ButtonGroup();
		String currentLafClass = UIManager.getLookAndFeel().getClass()
				.getName();
		for (int i = 0; i < entries.length; ++i) {
			LookAndFeelInfo entry = entries[i];
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(
					entry.getName());
			if (entry.getClassName().equals(currentLafClass)) {
				item.setSelected(true);
			}
			menu.add(item);
			item.addActionListener(new LookAndFeelAction(entry));
			group.add(item);
		}
		this.lookAndFeelMenu = menu;
	}

	/**
	 * @return Returns the gameMenuBar.
	 */
	public JMenuBar getGameMenuBar() {
		if (gameMenuBar == null)
			createGameMenuBar();
		return gameMenuBar;
	}

	/**
	 * @return Returns the gameToolBar.
	 */
	public JToolBar getGameToolBar() {
		if (gameToolBar == null)
			createGameToolBar();
		return gameToolBar;
	}

	/**
	 * 
	 * @param info
	 */
	private void handleChangeLookAndFeel(LookAndFeelInfo info) {
		log.debug("handleChangeLookAndFeel " + info);
		try {
			String lafClass = info.getClassName();
			UIManager.setLookAndFeel(lafClass);
			gameHandler.setLookAndFeelClass(info.getName());
			LookAndFeelListener lookAndFeelListener = this.lookAndFeelListener;
			if (lookAndFeelListener != null)
				lookAndFeelListener.handleLAFChanged();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	private void handleNewGameAction() {
		gameFrame.handleNewGameAction();
	}

	/**
	 * 
	 * 
	 */
	protected void init() {
		setupAction(optionsAction, "optionsAction"); //$NON-NLS-1$
		setupAction(aboutAction, "aboutAction"); //$NON-NLS-1$
		setupAction(exitAction, "exitAction"); //$NON-NLS-1$
		setupAction(newGameAction, "newGameAction"); //$NON-NLS-1$
		setupAction(muteAction, "muteAction"); //$NON-NLS-1$
		setupAction(lockAction, "lockAction"); //$NON-NLS-1$
		setupAction(stopAction, "stopAction"); //$NON-NLS-1$
		setupAction(autoLockAction, "autoLockAction"); //$NON-NLS-1$
		boolean mute = gameHandler.isMute();
		ToggleButtonModel model = muteModel;
		model.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				GameHandler handler = gameHandler;
				handler.setMute(muteModel.isSelected());
			}
		});
		model.setSelected(mute);
		model = autoLockModel;
		model.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				gameHandler.setAutoLock(autoLockModel.isSelected());
			}
		});
		model.setSelected(true);
		createLookAndFeelMenu();
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		JMenu menu = lookAndFeelMenu;
		Component[] menus = menu.getMenuComponents();
		String name = gameHandler.getLookAndFeelClass();
		for (int i = 0; i < menus.length; ++i) {
			JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) menus[i];
			String text = menuItem.getText();
			if (text.equals(name)) {
				menuItem.doClick();
				break;
			}
		}
	}

	/**
	 * @param handler
	 *            The handler to set.
	 */
	public void setGameFrame(GameFrame handler) {
		this.gameFrame = handler;
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}

	/**
	 * @param lookAndFeelListener
	 *            the lookAndFeelListener to set
	 */
	public void setLookAndFeelListener(LookAndFeelListener lookAndFeelListener) {
		this.lookAndFeelListener = lookAndFeelListener;
	}

	/**
	 * @param action
	 * @param model
	 * @param btn
	 */
	private void setToogleButton(Action action, ButtonModel model,
			AbstractButton btn) {
		btn.setModel(model);
		Icon icon = (Icon) action.getValue(DISABLED_ICON);
		if (icon != null) {
			btn.setSelectedIcon(icon);
		}
	}

	/**
	 * @param action
	 * @param name
	 */
	private void setupAction(Action action, String name) {

		String value = Messages.getString("ActionHandler." + name + ".name"); //$NON-NLS-1$ //$NON-NLS-2$
		action.putValue(Action.NAME, value);

		value = Messages.getString("ActionHandler." + name + ".accelerator"); //$NON-NLS-1$ //$NON-NLS-2$
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(value));

		value = Messages.getString("ActionHandler." + name + ".mnemonic"); //$NON-NLS-1$ //$NON-NLS-2$
		action.putValue(Action.MNEMONIC_KEY, Integer.valueOf(value.charAt(0)));

		value = Messages.getString("ActionHandler." + name + ".tip"); //$NON-NLS-1$ //$NON-NLS-2$
		action.putValue(Action.SHORT_DESCRIPTION, value);

		value = Messages.getString("ActionHandler." + name + ".icon"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!value.startsWith("!")) { //$NON-NLS-1$
			URL url = getClass().getResource(value);
			if (url != null)
				action.putValue(Action.SMALL_ICON, new ImageIcon(url));
		}
		value = Messages
				.getString("ActionHandler." + name + "." + DISABLED_ICON); //$NON-NLS-1$ //$NON-NLS-2$
		if (!value.startsWith("!")) { //$NON-NLS-1$
			URL url = getClass().getResource(value);
			if (url != null)
				action.putValue(DISABLED_ICON, new ImageIcon(url));
		}
	}
}