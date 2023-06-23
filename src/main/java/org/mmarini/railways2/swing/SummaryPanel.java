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

import org.mmarini.railways2.model.ExtendedPerformance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Shows the performance at the end of the game
 */
public class SummaryPanel extends JPanel {
    public static final Insets SEPARATOR_INSETS = new Insets(2, 0, 2, 0);
    public static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);
    private static final long serialVersionUID = -5918932430408182511L;
    private static final Logger log = LoggerFactory.getLogger(SummaryPanel.class);

    private final PerformancePanel performancePanel;
    private final JTextField nameField;
    private final JLabel message;

    /**
     * Creates the summary panel
     */
    public SummaryPanel() {
        nameField = new JTextField();
        message = new JLabel();
        performancePanel = new PerformancePanel();
        createContent();
    }

    /**
     * Creates the content
     */
    private void createContent() {
        log.debug("init");
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

        comp = performancePanel;
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

        comp = new JLabel(Messages.getString("SummaryPanel.nameLabel.text")); //$NON-NLS-1$
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
     * Returns the gamer name
     */
    public String getName() {
        return nameField.getText();
    }

    /**
     * Sets the editable name field
     *
     * @param nameEditable true if name is editable
     */
    void setNameEditable(boolean nameEditable) {
        nameField.setEditable(nameEditable);
        nameField.setEnabled(nameEditable);
    }

    /**
     * Sets the performance
     *
     * @param performance the performance
     */
    public void setPerformance(ExtendedPerformance performance) {
        performancePanel.setPerformance(performance);
        message.setText(nameField.isEditable()
                ? Messages.getString("SummaryPanel.newEntry.message")
                : Messages.getString("SummaryPanel.endGame.message"));
    }
}