package org.mmarini.railways.swing;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.mmarini.railways.model.GameHandler;

/**
 * @author $Author: marco $
 * @version $Id: InfosPane.java,v 1.4 2012/02/08 22:03:32 marco Exp $
 */
public class InfosPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	private TrainPane trainPane;
	private ManagerPane managerPane;
	private HallOfFamePane hallOfFamePane;

	/**
	 * 
	 */
	public InfosPane() {
		hallOfFamePane = new HallOfFamePane();
		trainPane = new TrainPane();
		managerPane = new ManagerPane();

		addTabKey("InfosPane.trainPane", trainPane); //$NON-NLS-1$
		addTabKey("InfosPane.managerPane", managerPane); //$NON-NLS-1$
		addTabKey("InfosPane.hallOfFamePane", hallOfFamePane); //$NON-NLS-1$
	}

	/**
	 * 
	 */
	private void addTabKey(String key, JComponent comp) {
		addTab(Messages.getString(key + ".title"), null, comp, Messages.getString(key + ".tip")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * 
	 */
	public void reload() {
		managerPane.reload();
		trainPane.reload();
		hallOfFamePane.reload();
	}

	/**
	 * @param gameHandler
	 * @see org.mmarini.railways.swing.HallOfFamePane#setGameHandler(org.mmarini.railways.model.GameHandler)
	 */
	public void setGameHandler(GameHandler gameHandler) {
		hallOfFamePane.setGameHandler(gameHandler);
		trainPane.setGameHandler(gameHandler);
		managerPane.setHandler(gameHandler);
	}
}