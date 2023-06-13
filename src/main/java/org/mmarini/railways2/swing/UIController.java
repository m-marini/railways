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

import org.mmarini.Tuple2;
import org.mmarini.railways2.model.SimulatorEngine;
import org.mmarini.railways2.model.SimulatorEngineImpl;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

/**
 * Manages the interaction between user interface and model.
 * Handles the simulation events, the user interface events.
 */
public class UIController {
    public static final int DEFAULT_TAB_WITDH = 600;
    private static final String IMAGE_RESOURCE_NAME = "org/mmarini/railways2/swing/railways.png";
    private static final int FPS = 60;
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);

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

    /**
     * Returns the initial station status
     */
    private static StationStatus createInitialSeed() throws IOException {
        return new BlockStationBuilder(StationDef.create(
                Utils.fromResource("/stations/downville.station.yml"), Locator.root()), new Random()).build();
    }

    private final JFrame frame;
    private final MapPanel mapPanel;
    private final StationPanel stationPanel;
    private final TrainPane trainPanel;
    private final JTabbedPane tabPanel;
    private final JSplitPane verticalSplit;
    private final JSplitPane horizontalSplit;
    private final SimulatorEngine<StationStatus, StationStatus> simulator;
    private final Random random;

    public UIController() throws IOException {
        this.frame = new JFrame();
        this.mapPanel = new MapPanel();
        this.stationPanel = new StationPanel();
        this.trainPanel = new TrainPane();
        this.tabPanel = new JTabbedPane();
        this.verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        StationStatus initialSeed = createInitialSeed();
        this.simulator = SimulatorEngineImpl.create(initialSeed, this::stepUp, Function.identity())
                .setEventInterval(Duration.ofMillis(1000 / FPS))
                .setOnEvent(this::handleSimulationEvent)
                .setOnSpeed(this::handleSpeed);
        this.random = new Random();
        simulator.setSpeed(1);
        initHorizontalSplit();
        initVerticalSplit();
        initTabbedPanel();
        initFrame();
        createSubscriptions();
    }

    /**
     * Creates the subscriptions to event flowables
     */
    private void createSubscriptions() {
        mapPanel.readMouseClick()
                .doOnNext(pt -> logger.atDebug().log("Map point {}", pt))
                .subscribe();
    }

    /**
     * Handle the simulation event
     *
     * @param stationStatus the station status
     */
    private void handleSimulationEvent(StationStatus stationStatus) {
        trainPanel.setStatus(stationStatus);
        stationPanel.paintStation(stationStatus);
        mapPanel.paintStation(stationStatus);
    }

    /**
     * Handle the speed monitor event
     *
     * @param speed the actual simulation speed
     */
    private void handleSpeed(double speed) {
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
        simulator.setSpeed(1);
        simulator.start();
    }

    /**
     * Returns the next station status after the given time interval
     * and the actual simulated time interval
     *
     * @param status the initial status
     * @param dt     the time interval
     */
    private Tuple2<StationStatus, Double> stepUp(StationStatus status, double dt) {
        StationStatus next = status.tick(dt, random);
        return Tuple2.of(next, dt);
    }
}
