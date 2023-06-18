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
import org.mmarini.railways2.model.Train;
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.routes.*;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAUGE;

/**
 * Manages the interaction between user interface and model.
 * Handles the simulation events, the user interface events.
 */
public class UIController {
    public static final int DEFAULT_TAB_WITDH = 600;
    private static final String IMAGE_RESOURCE_NAME = "org/mmarini/railways2/swing/railways.png";
    private static final int FPS = 60;
    private static final Logger logger = LoggerFactory.getLogger(UIController.class);
    private static final double ROUTE_DISTANCE_THRESHOLD = TRACK_GAP * sqrt(2) / 2;

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
    private final StationScrollPanel stationScrollPanel;
    private final TrainPane trainPanel;
    private final JTabbedPane tabPanel;
    private final JSplitPane verticalSplit;
    private final JSplitPane horizontalSplit;
    private final SimulatorEngine<StationStatus, StationStatus> simulator;
    private final Random random;

    /**
     * Creates the user interface controller
     *
     * @throws IOException in case of error
     */
    public UIController() throws IOException {
        this.frame = new JFrame();
        this.mapPanel = new MapPanel();
        this.stationScrollPanel = new StationScrollPanel();
        this.trainPanel = new TrainPane();
        this.tabPanel = new JTabbedPane();
        this.verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPopupMenu popupMenu = new JPopupMenu();

        StationStatus initialSeed = createInitialSeed();
        this.simulator = SimulatorEngineImpl.create(initialSeed, this::stepUp, Function.identity())
                .setEventInterval(Duration.ofMillis(1000 / FPS))
                .setOnEvent(this::handleSimulationEvent)
                .setOnSpeed(this::handleSpeed);
        this.random = new Random();
        simulator.setSpeed(1);

        popupMenu.add(new JMenuItem("Entry"));

        initHorizontalSplit();
        initVerticalSplit();
        initTabbedPanel();
        initFrame();
        createSubscriptions();
        logger.atDebug().log("Created");
    }

    /**
     * Returns the popup menu to display
     *
     * @param event the map event
     */
    private JPopupMenu createPopUpMenu(MapEvent event) {
        // Creates train menu items
        JPopupMenu popupMenu = new JPopupMenu();
        event.getSelectedTrain().stream()
                .flatMap(this::createTrainMenu)
                .forEach(popupMenu::add);

        // Creates section menu item
        event.getSelectedSection(TRACK_GAUGE / 2)
                .map(section -> createSectionMenu(event, section))
                .ifPresent(popupMenu::add);

        // Creates route menu item
        event.getNearestRoute(ROUTE_DISTANCE_THRESHOLD)
                .flatMap(route -> createRouteMenu(event, route))
                .ifPresent(popupMenu::add);


        return popupMenu;
    }

    /**
     * Returns the route menu item
     *
     * @param event the map event
     * @param route the route
     */
    private Optional<JMenuItem> createRouteMenu(MapEvent event, Route route) {
        if (route instanceof Switch || route instanceof DoubleSlipSwitch) {
            String label = StationLabels.getLabel(event.getStationStatus().getStationMap().getId(), route.getId());
            JMenuItem menu = new JMenuItem(format(
                    Messages.getString("UIController.toggleSwitchMenuItem.name"),
                    label));
            menu.addActionListener(route instanceof Switch ?
                    ev -> toggleSwitch(route.getId()) :
                    ev -> toggleDoubleSlipSwitch(route.getId()));
            return Optional.of(menu);
        } else if (route instanceof Signal) {
            Signal signal = (Signal) route;
            return getNearestOppositeRouteEntry(route, event.getLocation())
                    .map(entryDir -> {
                        boolean lock = signal.isLocked(entryDir);
                        String label = StationLabels.getLabel(event.getStationStatus().getStationMap().getId(), route.getId());
                        JMenuItem menu = new JMenuItem(format(
                                lock ?
                                        Messages.getString("UIController.unlockSignalMenuItem.name") :
                                        Messages.getString("UIController.lockSignalMenuItem.name"),
                                label));
                        String edgeId = entryDir.getEdge().getId();
                        menu.addActionListener(lock
                                ? ev -> unlockSignal(route.getId(), edgeId)
                                : ev -> lockSignal(route.getId(), edgeId));
                        return menu;
                    });
        }
        return Optional.empty();
    }

    /**
     * Returns the section menu item
     *
     * @param event   the map event
     * @param section the selected section
     */
    private JMenuItem createSectionMenu(MapEvent event, Section section) {
        StationStatus stationStatus = event.getStationStatus();
        boolean sectionLocked = stationStatus.isSectionLocked(section.getEdges().get(0));
        String label = StationLabels.getLabel(event.getStationStatus().getStationMap().getId(), section.getId());
        JMenuItem sectionMenu = new JMenuItem(sectionLocked ?
                format(Messages.getString("UIController.unlockSectionMenuItem.name"),
                        label) :
                format(Messages.getString("UIController.lockSectionMenuItem.name"),
                        label));
        sectionMenu.addActionListener(ev -> {
            if (sectionLocked) {
                unlockSection(section.getId());
            } else {
                lockSection(section.getId());
            }
        });
        return sectionMenu;
    }

    /**
     * Creates the subscriptions to event flowables
     */
    private void createSubscriptions() {
        mapPanel.readMouseClick()
                .doOnNext(stationScrollPanel::scrollTo)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON1)
                .doOnNext(this::handleLeftMouseMapEvent)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON2)
                .doOnNext(this::handleCentralMouseMapEvent)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON3)
                .doOnNext(this::handleRightMouseMapEvent)
                .subscribe();
    }

    /**
     * Returns the menu entries for a train
     *
     * @param train the selected train
     */
    private Stream<JMenuItem> createTrainMenu(Train train) {
        Stream.Builder<JMenuItem> builder = Stream.builder();
        Train.State state = train.getState();
        if (state == Train.STATE_RUNNING || state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
            JMenuItem prova = new JMenuItem(
                    format(Messages.getString("UIController.stopTrainMenuItem.name"), train.getId()));
            prova.addActionListener(ev -> stopTrain(train.getId()));
            builder.add(prova);
        }
        if (state.equals(Train.STATE_BRAKING) || state.equals(Train.STATE_WAITING_FOR_RUN)) {
            JMenuItem prova = new JMenuItem(
                    format(Messages.getString("UIController.startTrainMenuItem.name"), train.getId()));
            prova.addActionListener(ev -> startTrain(train.getId()));
            builder.add(prova);
        }
        if (state.equals(Train.STATE_WAITING_FOR_RUN)
                || state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
            JMenuItem prova = new JMenuItem(
                    format(Messages.getString("UIController.revertTrainMenuItem.name"), train.getId()));
            prova.addActionListener(ev -> revertTrain(train.getId()));
            builder.add(prova);
        }
        return builder.build();
    }

    /**
     * Returns the nearest edge of the route
     *
     * @param route the route
     * @param point the reference point
     */
    private Optional<Direction> getNearestOppositeRouteEntry(Route route, Point2D point) {
        return route.getValidEntries().stream()
                .min(Comparator.comparingDouble(
                        dir -> dir.getEdge().getDistance(point)
                ))
                .flatMap(entry ->
                        route.getEntry(entry.opposite())
                );
    }

    /**
     * Handles central mouse map event from stationScrollPanel
     *
     * @param event the map event
     */
    private void handleCentralMouseMapEvent(MapEvent event) {
        Optional<Train> trainOpt = event.getSelectedTrain();
        Optional<Route> routeOpt = event.getNearestRoute(ROUTE_DISTANCE_THRESHOLD);
        Optional<Section> sectionOpt = event.getSelectedSection(TRACK_GAUGE / 2);
        trainOpt.ifPresentOrElse(
                this::handleTrainSelection2,
                () -> routeOpt.ifPresentOrElse(
                        route -> handleRouteSelection2(event, route),
                        () -> sectionOpt.ifPresent(
                                section -> handleSectionSelection2(event, section))));
    }

    /**
     * Handles left mouse map event from stationScrollPanel
     *
     * @param event the map event
     */
    private void handleLeftMouseMapEvent(MapEvent event) {
        // left button
        Optional<Route> routeOpt = event.getNearestRoute(ROUTE_DISTANCE_THRESHOLD);
        Optional<Section> sectionOpt = event.getSelectedSection(TRACK_GAUGE / 2);
        event.getSelectedTrain().ifPresentOrElse(
                this::handleTrainSelection,
                () -> routeOpt.ifPresentOrElse(
                        route -> handleRouteSelection(event, route),
                        () -> sectionOpt.ifPresent(
                                section -> handleSectionSelection(event, section))));
    }

    /**
     * Handles right mouse map event from stationScrollPanel
     *
     * @param event the map event
     */
    private void handleRightMouseMapEvent(MapEvent event) {
        //  right button = context popup menu
        JPopupMenu menu = createPopUpMenu(event);
        stationScrollPanel.showPopUp(menu, event.getNearestEdgeLocation().getLocation());
    }

    /**
     * Handles the left mouse button on route (signal unlock/switch toggle)
     *
     * @param event the map event
     * @param route the selected route
     */
    private void handleRouteSelection(MapEvent event, Route route) {
        if (route instanceof Switch) {
            toggleSwitch(route.getId());
        } else if (route instanceof DoubleSlipSwitch) {
            toggleDoubleSlipSwitch(route.getId());
        } else if (route instanceof Signal) {
            getNearestOppositeRouteEntry(route, event.getLocation())
                    .ifPresent(dir -> unlockSignal(route.getId(), dir.getEdge().getId()));
        }
    }

    /**
     * Handles the central mouse button on route (signal lock/switch toggle)
     *
     * @param event the map event
     * @param route the selected route
     */
    private void handleRouteSelection2(MapEvent event, Route route) {
        if (route instanceof Switch) {
            toggleSwitch(route.getId());
        } else if (route instanceof DoubleSlipSwitch) {
            toggleDoubleSlipSwitch(route.getId());
        } else if (route instanceof Signal) {
            getNearestOppositeRouteEntry(route, event.getLocation())
                    .ifPresent(entryDir -> lockSignal(route.getId(), entryDir.getEdge().getId()));
        }
    }

    /**
     * Handles left mouse button on section (unlock)
     *
     * @param event   the map event
     * @param section the selected section
     */
    private void handleSectionSelection(MapEvent event, Section section) {
        if (event.getStationStatus().isSectionLocked(section.getEdges().get(0))) {
            unlockSection(section.getId());
        }
    }

    /**
     * Handles central mouse button on section (lock)
     *
     * @param event   the map event
     * @param section the selected section
     */
    private void handleSectionSelection2(MapEvent event, Section section) {
        if (!event.getStationStatus().isSectionLocked(section.getEdges().get(0))) {
            lockSection(section.getId());
        }
    }

    /**
     * Handle the simulation event
     *
     * @param stationStatus the station status
     */
    private void handleSimulationEvent(StationStatus stationStatus) {
        trainPanel.setStatus(stationStatus);
        stationScrollPanel.paintStation(stationStatus);
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
     * Handles the mouse left button on train
     *
     * @param train the selected train
     */
    private void handleTrainSelection(Train train) {
        Train.State state = train.getState();
        if (state.equals(Train.STATE_RUNNING) || state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
            stopTrain(train.getId());
        } else if (state.equals(Train.STATE_BRAKING) || state.equals(Train.STATE_WAITING_FOR_RUN)) {
            startTrain(train.getId());
        }
    }

    /**
     * Handles the mouse central button on train
     *
     * @param train the selected train
     */
    private void handleTrainSelection2(Train train) {
        revertTrain(train.getId());
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
        verticalSplit.setBottomComponent(stationScrollPanel);
    }

    /**
     * Unlocks the section
     *
     * @param id the section identifier
     */

    private void lockSection(String id) {
        simulator.request(status -> status.lockSection(id));
    }

    /**
     * Locks the signal
     *
     * @param id     the signal identifier
     * @param edgeId the signal edge identifier
     */
    private void lockSignal(String id, String edgeId) {
        simulator.request(status -> status.lockSignal(id, edgeId));
    }

    /**
     * Reverts the train
     *
     * @param trainId the train identifier
     */
    private void revertTrain(String trainId) {
        simulator.request(status -> status.revertTrain(trainId));
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
     * Starts the train
     *
     * @param trainId the train identifier
     */
    private void startTrain(String trainId) {
        simulator.request(status -> status.startTrain(trainId));
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

    /**
     * Stops the train
     *
     * @param trainId the train identifier
     */
    private void stopTrain(String trainId) {
        simulator.request(status -> status.stopTrain(trainId));
    }

    /**
     * Toggles the double slip switch
     *
     * @param id the switch identifier
     */
    private void toggleDoubleSlipSwitch(String id) {
        simulator.request(status -> status.toggleDoubleSlipSwitch(id));
    }

    /**
     * Toggles switch
     *
     * @param id the switch identifier
     */
    private void toggleSwitch(String id) {
        simulator.request(status -> status.toggleSwitch(id));
    }

    /**
     * Unlocks the section
     *
     * @param id the section identifier
     */
    private void unlockSection(String id) {
        simulator.request(status -> status.unlockSection(id));
    }

    /**
     * Unlocks the signal
     *
     * @param id     the signal identifier
     * @param edgeId the signal edge identifier
     */
    private void unlockSignal(String id, String edgeId) {
        simulator.request(status -> status.unlockSignal(id, edgeId));
    }
}
