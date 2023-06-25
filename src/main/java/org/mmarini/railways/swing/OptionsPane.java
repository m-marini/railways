package org.mmarini.railways.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.mmarini.railways.model.GameHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author $Author: marco $
 * @version $Id: UserPrefPanel.java,v 1.3 2012/02/08 22:03:32 marco Exp $
 */
public class OptionsPane extends JPanel {
	public static final double NORMAL_SPEED = 1e-3;
	private static final long serialVersionUID = 1L;
	private static final int[] VOLUME_LABELS = { 0, -6, -12, -18, -24, -30,
			-36, -42, -48 };
	private static final int[] SPEED_LABELS = { 1, 5, 10 };

	private static Logger log = LoggerFactory.getLogger(OptionsPane.class);

	private JSlider speedSlider;
	private JSlider volumeSlider;
	private GameHandler gameHandler;

	/**
	 * 
	 */
	public OptionsPane() {
		speedSlider = new JSlider(SwingConstants.VERTICAL, 1, 10, 1);
		volumeSlider = new JSlider(SwingConstants.VERTICAL, -48, 0, 0);
		init();
	}

	/**
	 * 
	 */
	public void apply() {
		int value = volumeSlider.getValue();
		gameHandler.setGain(value);
		value = speedSlider.getValue();
		gameHandler.setTimeSpeed(NORMAL_SPEED * value);
	}

	/**
	 * @param fmt
	 * @param val
	 * @return
	 */
	private Dictionary<Integer, JLabel> createLabels(String fmt, int[] val) {
		Dictionary<Integer, JLabel> labels;
		labels = new Hashtable<Integer, JLabel>();
		for (int i = 0; i < val.length; ++i) {
			String text = MessageFormat.format(fmt, new Object[] { new Integer(
					val[i]) });
			labels.put(new Integer(val[i]), new JLabel(text));
		}
		return labels;
	}

	/**
	 * 
	 */
	private void init() {
		log.debug("init"); //$NON-NLS-1$
		JSlider slider = volumeSlider;
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(2);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		Dictionary<Integer, JLabel> labels = createLabels(
				Messages.getString("OptionsPane.volume.format.label"), VOLUME_LABELS); //$NON-NLS-1$
		slider.setLabelTable(labels);

		slider = speedSlider;
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		slider.setSnapToTicks(true);
		labels = createLabels(
				Messages.getString("OptionsPane.speed.format.label"), SPEED_LABELS); //$NON-NLS-1$
		slider.setLabelTable(labels);

		JComponent content = this;

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		content.setLayout(gbl);
		content.setBorder(BorderFactory.createEtchedBorder());

		Border empty = BorderFactory.createEmptyBorder(5, 15, 5, 15);
		JComponent comp;
		comp = speedSlider;
		comp.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(Messages
						.getString("OptionsPane.speedLabel")), empty)); //$NON-NLS-1$
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);

		comp = volumeSlider;
		comp.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder(Messages
						.getString("OptionsPane.volumeLabel")), empty)); //$NON-NLS-1$
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbl.setConstraints(comp, gbc);
		content.add(comp);
	}

	/**
	 * 
	 * 
	 */
	public void reload() {
		float val = gameHandler.getGain();
		volumeSlider.setValue(Math.round(val));
		double speed = gameHandler.getTimeSpeed();
		speedSlider.setValue((int) Math.round(speed / NORMAL_SPEED));
	}

	/**
	 * @param gameHandler
	 *            the gameHandler to set
	 */
	public void setGameHandler(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
}