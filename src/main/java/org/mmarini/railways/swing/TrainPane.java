package org.mmarini.railways.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.mmarini.railways.model.GameHandler;

/**
 * @author $Author: marco $
 * @version $Id: TrainPane.java,v 1.8 2012/02/08 22:03:31 marco Exp $
 */
public class TrainPane extends JPanel {
	public static final Font DEFAULT_FONT = Font.decode("Dialog Plain 10");
	private static final long serialVersionUID = 1L;

	private TrainTableModel model;
	private JTable table;

	/**
	 * 
	 */
	public TrainPane() {
		model = new TrainTableModel();
		table = new JTable(model);

		setLayout(new BorderLayout());
		int h = table.getCellRenderer(0, 0)
				.getTableCellRendererComponent(table, "", false, false, 0, 0)
				.getSize().height;
		table.setPreferredScrollableViewportSize(new Dimension(100, h * 11));
		table.setFont(DEFAULT_FONT);
		JScrollPane pane = new JScrollPane(table);
		add(pane, BorderLayout.CENTER);
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		model.reload();
	}

	/**
	 * @param gameHandler
	 * @see org.mmarini.railways.swing.TrainTableModel#setGameHandler(org.mmarini.railways.model.GameHandler)
	 */
	public void setGameHandler(GameHandler gameHandler) {
		model.setGameHandler(gameHandler);
	}
}