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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.MessageFormat;

/**
 * Manages the interaction between user interface and model.
 * Handles the simulation events, the user interface events.
 */
public class UIController {
    public static final int DEFAULT_TAB_WITDH = 600;
    private static final String IMAGE_RESOURCE_NAME = "org/mmarini/railways2/swing/railways.png";

    /**
     * Adds a tab to the tabbed panel
     *
     * @param panel   the panel
     * @param key     the key of tab
     * @param content the content of tab
     */
    private static void addTabKey(JTabbedPane panel, String key, JComponent content) {
        panel.addTab(Messages.getString(key + ".title"),
                null,
                content,
                Messages.getString(key + ".tip"));
    }

    private final JFrame frame;
    private final MapPanel mapPanel;
    private final StationPanel stationPanel;
    private final TrainPane trainPanel;
    private final JTabbedPane tabPanel;
    private final JSplitPane verticalSplit;
    private final JSplitPane horizontalSplit;

    public UIController() {
        this.frame = new JFrame();
        this.mapPanel = new MapPanel();
        this.stationPanel = new StationPanel();
        this.trainPanel = new TrainPane();
        this.tabPanel = new JTabbedPane();
        this.verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        initHorizontalSplit();
        initVerticalSplit();
        initTabbedPanel();
        initFrame();
    }

    /**
     * Initialize the game frame
     */
    private void initFrame() {
        String title = Messages.getString("Frame.title");
        title = MessageFormat
                .format(title,
                        Messages.getString("Railways.name"),
                        Messages.getString("Railways.version"),
                        Messages.getString("Railways.author"));
        frame.setTitle(title);
        URL imgResource = Thread.currentThread().getContextClassLoader()
                .getResource(IMAGE_RESOURCE_NAME);
        if (imgResource != null) {
            ImageIcon imageIcon = new ImageIcon(imgResource);
            frame.setIconImage(imageIcon.getImage());
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screen);
        frame.setLocation(0, 0);

        Container content = frame.getContentPane();
        content.add(verticalSplit, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                int hWidth = horizontalSplit.getWidth();
                horizontalSplit.setDividerLocation(hWidth - DEFAULT_TAB_WITDH);
                verticalSplit.setDividerLocation(0.25);
            }
        });
    }

    /**
     * Initializes the horizontal split panel
     */
    private void initHorizontalSplit() {
        horizontalSplit.setOneTouchExpandable(true);
        horizontalSplit.setResizeWeight(1);
        horizontalSplit.setLeftComponent(mapPanel);
        horizontalSplit.setRightComponent(tabPanel);
    }

    /**
     * Creates the tab panel with the train panel, manager panel and hall of fame
     */
    private void initTabbedPanel() {
        addTabKey(tabPanel, "InfoPanel.trainPane", trainPanel);
        addTabKey(tabPanel, "InfoPanel.managerPane", new JPanel());
        addTabKey(tabPanel, "InfoPanel.hallOfFamePane", new JPanel());
    }

    /**
     * Initializes vertical split
     */
    private void initVerticalSplit() {
        verticalSplit.setOneTouchExpandable(true);
        verticalSplit.setResizeWeight(0);
        verticalSplit.setTopComponent(horizontalSplit);
        verticalSplit.setBottomComponent(new JScrollPane(stationPanel));
    }

    /**
     * Runs the app game
     */
    public void run() {
        frame.setVisible(true);
    }
}
