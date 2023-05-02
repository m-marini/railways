package org.mmarini.railways.swing;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.mmarini.railways.model.GameHandler;

/**
 * 
 * @author US00852
 * @version $Id: HallOfFamePane.java,v 1.3 2012/02/08 22:03:32 marco Exp $
 */
public class HallOfFamePane extends JPanel {
	private static final long serialVersionUID = 1L;

	private HallOfFameModel hallOfFameModel;

	/**
	 * 
	 */
	public HallOfFamePane() {
		hallOfFameModel = new HallOfFameModel();
		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(new JTable(hallOfFameModel));
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * 
	 * @see org.mmarini.railways.swing.HallOfFameModel#reload()
	 */
	public void reload() {
		hallOfFameModel.reload();
	}

	/**
	 * @param gameHandler
	 * @see org.mmarini.railways.swing.HallOfFameModel#setGameHandler(org.mmarini.railways.model.GameHandler)
	 */
	public void setGameHandler(GameHandler gameHandler) {
		hallOfFameModel.setGameHandler(gameHandler);
	}
}
