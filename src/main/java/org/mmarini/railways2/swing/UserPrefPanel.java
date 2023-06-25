/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.railways2.swing;

import org.mmarini.swing.GridLayoutHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.mmarini.railways2.swing.SwingUtils.formatMessage;

/**
 * Shows the user preferences
 */
public class UserPrefPanel extends JPanel {
    private static final int[] VOLUME_LABELS = {0, -6, -12, -18, -24, -30,
            -36, -42, -48};
    private static final int[] SPEED_LABELS = {1, 5, 10};

    private static final Logger logger = LoggerFactory.getLogger(UserPrefPanel.class);

    /**
     * Returns the label map table
     *
     * @param key    message key
     * @param values the values
     */
    private static Dictionary<Integer, JLabel> createLabels(String key, int... values) {
        Dictionary<Integer, JLabel> result = new Hashtable<>();
        Arrays.stream(values)
                .forEach(value ->
                        result.put(value, new JLabel(formatMessage(key, value))));
        return result;
    }

    private final JSlider speedSlider;
    private final JSlider volumeSlider;

    /**
     * Creates the user preferences panel
     */
    public UserPrefPanel() {
        speedSlider = new JSlider(SwingConstants.VERTICAL, 1, 10, 1);
        volumeSlider = new JSlider(SwingConstants.VERTICAL, -48, 0, 0);
        init();
    }

    /**
     * Returns the speed (x)
     */
    public double getSpeed() {
        return speedSlider.getValue();
    }

    /**
     * Returns the volume (db)
     */
    public double getVolume() {
        return volumeSlider.getValue();
    }

    /**
     *
     */
    private void init() {
        logger.debug("init");
        Border empty = BorderFactory.createEmptyBorder(5, 15, 5, 15);

        volumeSlider.setPaintLabels(true);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setMinorTickSpacing(2);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintTrack(true);
        volumeSlider.setLabelTable(createLabels("UserPrefPanel.volume.format.label", VOLUME_LABELS));
        volumeSlider.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder(Messages
                        .getString("UserPrefPanel.volumeLabel")), empty));

        speedSlider.setPaintLabels(true);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintTrack(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setLabelTable(createLabels("UserPrefPanel.speed.format.label", SPEED_LABELS));
        speedSlider.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder(Messages.getString("UserPrefPanel.speedLabel")), empty));

        new GridLayoutHelper<>(this).add(
                "+insets,2 hw,1 nofill center",
                speedSlider,
                "+at,1,0",
                volumeSlider);
    }

    /**
     * Sets the user preferences panel with the user preferences
     *
     * @param userPreferences the user preferences
     */
    public void setUserPreferences(UserPreferences userPreferences) {
        volumeSlider.setValue((int) Math.round(userPreferences.getGain()));
        speedSlider.setValue((int) Math.round(userPreferences.getSimulationSpeed()));
    }
}