package org.mmarini.railways.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmarini.railways.model.GameHandlerImpl;

/**
 * @author $Author: marco $
 * @version $Id: GameFrame.java,v 1.15 2012/02/08 22:03:32 marco Exp $
 */
public class GameFrame extends JFrame implements LookAndFeelListener {
	private static final String IMAGE_RESOURCE_NAME = "org/mmarini/railways/swing/railways.png";

	private static final long serialVersionUID = 1L;

	private static Log logger = LogFactory.getLog(GameFrame.class);

	/**
	 * @param arg
	 * @throws Throwable
	 */
	public static void main(String[] arg) throws Throwable {
		logger.info("Starting Railways ...");
		GameFrame frame = new GameFrame();
		frame.setVisible(true);
	}

	private MainPane mainPane;
	private ActionHandler actionsHandler;

	/**
	 * 
	 */
	public GameFrame() {
		mainPane = new MainPane();
		actionsHandler = new ActionHandler();
		GameHandlerImpl gameHandler = new GameHandlerImpl();
		mainPane.setGameHandler(gameHandler);
		actionsHandler.setGameHandler(gameHandler);
		actionsHandler.setGameFrame(this);
		actionsHandler.init();
		init();
	}

	/**
	 * 
	 * @return
	 */
	public boolean handleAboutAction() {
		return mainPane.handleAboutAction();
	}

	/**
	 * 
	 * @return
	 */
	public boolean handleExitAction() {
		mainPane.handleExitAction();
		dispose();
		return true;
	}

	/**
	 * @see org.mmarini.railways.swing.LookAndFeelListener#handleLAFChanged()
	 */
	@Override
	public void handleLAFChanged() {
		SwingUtilities.updateComponentTreeUI(this);
		mainPane.handleLAFChanged();
	}

	/**
	 * 
	 * @return
	 */
	public boolean handleNewGameAction() {
		return mainPane.handleNewGameAction();
	}

	/**
	 * 
	 * @return
	 */
	public boolean handleOptionsAction() {
		return mainPane.handleOptionsAction();
	}

	/**
     * 
     */
	public void init() {
		logger.debug("init");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		MainPane mainPane = this.mainPane;
		contentPane.add(actionsHandler.getGameToolBar(), BorderLayout.NORTH);
		contentPane.add(mainPane, BorderLayout.CENTER);

		setJMenuBar(actionsHandler.getGameMenuBar());

		String title = Messages.getString("GameFrame.title"); //$NON-NLS-1$
		title = MessageFormat
				.format(title,
						new Object[] {
								Messages.getString("Railways.name"), Messages.getString("Railways.version"), //$NON-NLS-1$ //$NON-NLS-2$
								Messages.getString("Railways.author") }); //$NON-NLS-1$
		setTitle(title);
		URL imgResource = Thread.currentThread().getContextClassLoader()
				.getResource(IMAGE_RESOURCE_NAME);
		ImageIcon imageIcon = new ImageIcon(imgResource);
		setIconImage(imageIcon.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screen);
		setLocation(0, 0);
		actionsHandler.setLookAndFeelListener(this);
		actionsHandler.reload();
	}
}