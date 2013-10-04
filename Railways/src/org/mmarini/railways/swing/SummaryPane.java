package org.mmarini.railways.swing;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.mmarini.railways.model.GameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author US00852
 * @version $Id: SummaryPane.java,v 1.3 2012/02/08 22:03:32 marco Exp $
 */
public class SummaryPane extends JPanel {
	public static final Insets SEPARATOR_INSETS = new Insets(2, 0, 2, 0);
	public static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);
	private static final long serialVersionUID = -5918932430408182511L;
	private static Logger log = LoggerFactory.getLogger(SummaryPane.class);

	private ManagerPane managerPane;
	private JTextField nameField;
	private GameHandler gameHandler;
	private JLabel message;

	/**
	 * 
	 * @throws HeadlessException
	 */
	public SummaryPane() {
		nameField = new JTextField();
		message = new JLabel();
		managerPane = new ManagerPane();
		createContent();
	}

	/**
	 * 
	 * 
	 */
	public void apply() {
		gameHandler.getManagerInfos().setName(nameField.getText());
	}

	/**
	 * 
	 * 
	 */
	private void createContent() {
		log.debug("init"); //$NON-NLS-1$
		setBorder(BorderFactory.createEtchedBorder());
		JTextField field = nameField;
		field.setColumns(20);

		Container content = this;
		GridBagLayout gbl = new GridBagLayout();
		content.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		JComponent comp;

		comp = message;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = DEFAULT_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JSeparator();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = SEPARATOR_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = managerPane;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = DEFAULT_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JSeparator();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = SEPARATOR_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JLabel(Messages.getString("SummaryPane.nameLabel.text")); //$NON-NLS-1$
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = DEFAULT_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = field;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = DEFAULT_INSETS;
		gbl.setConstraints(comp, gbc);
		content.add(comp);
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		managerPane.reload();
		boolean isEditable = gameHandler.isNewEntry();
		JTextField field = nameField;
		field.setEditable(isEditable);
		field.setEnabled(isEditable);
		String text;
		if (isEditable) {
			text = Messages.getString("SummaryPane.newEntry.message"); //$NON-NLS-1$
		} else {
			text = Messages.getString("SummaryPane.endGame.message"); //$NON-NLS-1$
		}
		message.setText(text);
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
		managerPane.setHandler(gameHandler);
		reload();
	}
}