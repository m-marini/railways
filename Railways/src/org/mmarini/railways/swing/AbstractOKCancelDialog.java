package org.mmarini.railways.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author US00852
 * @version $Id: AbstractOKCancelDialog.java,v 1.1.2.1 2006/08/25 21:01:32 marco
 *          Exp $
 */
public abstract class AbstractOKCancelDialog extends JDialog {
	private static final long serialVersionUID = -4452519314259357683L;

	private static Logger log = LoggerFactory
			.getLogger(AbstractOKCancelDialog.class);

	private boolean completed;
	private Action okAction;
	private Action cancelAction;

	/**
	 * 
	 * 
	 */
	public AbstractOKCancelDialog() {
		okAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				completed = true;
				dispose();
			}
		};
		cancelAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		setModal(true);
	}

	/**
	 * 
	 */
	private void centerDialog() {
		Dimension size = getPreferredSize();
		size = new Dimension(size.width + 40, size.height + 40);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(size);
		setLocation((screen.width - size.width) / 2,
				(screen.height - size.height) / 2);
		doLayout();
	}

	/**
	 * 
	 * @return
	 */
	protected abstract JComponent createDialogContent();

	/**
	 * @return Returns the okAction.
	 */
	protected Action getOkAction() {
		return okAction;
	}

	/**
	 * 
	 * 
	 */
	protected void init() {
		log.debug("init");
		setupAction(getOkAction(), "AbstractOKCancelDialog.okAction"); //$NON-NLS-1$
		setupAction(cancelAction, "AbstractOKCancelDialog.cancelAction"); //$NON-NLS-1$

		JPanel content = new JPanel();
		setContentPane(content);
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		content.setLayout(gbl);
		JComponent comp;

		comp = createDialogContent();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		JButton btn = new JButton(getOkAction());
		getRootPane().setDefaultButton(btn);
		comp = btn;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = new JButton(cancelAction);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent ev) {
				log.debug("windowActivated");
				completed = false;
			}

		});
		centerDialog();
	}

	/**
	 * @return Returns the completed.
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param action
	 * @param name
	 */
	protected void setupAction(Action action, String name) {
		String value = Messages.getString(name + ".name"); //$NON-NLS-1$ 
		action.putValue(Action.NAME, value);

		value = Messages.getString(name + ".mnemonic"); //$NON-NLS-1$ 
		action.putValue(Action.MNEMONIC_KEY, Integer.valueOf(value.charAt(0)));
	}

	/**
	 * 
	 * 
	 */
	public void updateUI() {
		log.debug("updateUI");
		getRootPane().updateUI();
		updateUIComponent(getGlassPane());
		updateUIComponent(getContentPane());
	}

	/**
	 * 
	 * @param comp
	 */
	private void updateUIComponent(Component comp) {
		if (comp != null && comp instanceof JComponent)
			((JComponent) comp).updateUI();
	}
}