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

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.*;
import org.mmarini.railways2.model.geometry.Direction;
import org.mmarini.railways2.model.routes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAP;
import static org.mmarini.railways2.model.RailwayConstants.TRACK_GAUGE;
import static org.mmarini.railways2.model.SoundEvent.*;
import static org.mmarini.railways2.swing.SwingUtils.*;

/**
 * Manages the interaction between user interface and model.
 * Handles the simulation events, the user interface events.
 */
public class UIController {
    public static final int DEFAULT_TAB_WITDH = 600;
    public static final String DUMP_FILENAME = "dump.yml";
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

    private final SummaryPanel summaryPanel;
    private final JFrame frame;
    private final MapPanel mapPanel;
    private final StationScrollPanel stationScrollPanel;
    private final TrainPane trainPanel;
    private final JTabbedPane tabPanel;
    private final JSplitPane verticalSplit;
    private final JSplitPane horizontalSplit;
    private final SimulatorEngine<StationStatus, StationStatus> simulator;
    private final PerformancePanel performancePanel;
    private final Random random;
    private final HallOfFamePanel hallOfFamePanel;
    private final JMenuItem newGameMenu;
    private final JMenuItem exitMenu;
    private final JMenuItem lockMenu;
    private final JMenuItem stopTrainsMenu;
    private final JMenuItem userPreferencesMenu;
    private final JMenuItem aboutMenu;
    private final JMenuItem dumpMenu;
    private final JCheckBoxMenuItem muteMenu;
    private final JCheckBoxMenuItem autoLockMenu;
    private final JCheckBoxMenuItem pauseMenu;
    private final JButton newGameButton;
    private final JButton lockButton;
    private final JButton stopTrainsButton;
    private final JToggleButton pauseButton;
    private final JButton userPreferencesButton;
    private final JButton exitButton;
    private final JToggleButton muteButton;
    private final JToggleButton autoLockButton;
    private final GameDialog gameDialog;
    private final UserPrefPanel userPrefPanel;
    private final PublishProcessor<SoundEvent> events;
    private final SoundPlayer soundPlayer;
    private final Map<SoundEvent, Clip> clipByEvent;
    private Configuration configuration;
    private boolean autolock;
    private boolean layoutRequired;
    private boolean pause;
    private StationStatus status;

    /**
     * Creates the user interface controller
     */
    public UIController() {
        this.frame = new JFrame();
        this.mapPanel = new MapPanel();
        this.stationScrollPanel = new StationScrollPanel();
        this.trainPanel = new TrainPane();
        this.tabPanel = new JTabbedPane();
        this.verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.performancePanel = new PerformancePanel();
        this.summaryPanel = new SummaryPanel();
        this.hallOfFamePanel = new HallOfFamePanel();
        this.gameDialog = new GameDialog();
        userPrefPanel = new UserPrefPanel();
        this.configuration = Configuration.load();
        this.events = PublishProcessor.create();
        this.soundPlayer = new SoundPlayer();
        this.autolock = true;
        clipByEvent = Stream.of(
                        Tuple2.of(ARRIVING, Optional.ofNullable(soundPlayer.getArrivingClip())),
                        Tuple2.of(ARRIVED, Optional.ofNullable(soundPlayer.getArrivedClip())),
                        Tuple2.of(BRAKING, Optional.ofNullable(soundPlayer.getBrakingClip())),
                        Tuple2.of(LEAVING, Optional.ofNullable(soundPlayer.getLeavingClip())),
                        Tuple2.of(STOPPED, Optional.ofNullable(soundPlayer.getStoppedClip())),
                        Tuple2.of(SWITCH, Optional.ofNullable(soundPlayer.getSwitchClip())))
                .flatMap(t -> t._2.stream().map(t::setV2))
                .collect(Tuple2.toMap());


        this.newGameMenu = createMenuItem("UIController.newGameAction");
        this.exitMenu = createMenuItem("UIController.exitAction");
        this.muteMenu = createCheckBoxMenuItem("UIController.muteAction");
        this.lockMenu = createMenuItem("UIController.lockAction");
        this.stopTrainsMenu = createMenuItem("UIController.stopAction");
        this.autoLockMenu = createCheckBoxMenuItem("UIController.autoLockAction");
        this.userPreferencesMenu = createMenuItem("UIController.userPreferencesAction");
        this.aboutMenu = createMenuItem("UIController.aboutAction");
        this.dumpMenu = createMenuItem("UIController.dumpAction");
        this.pauseMenu = createCheckBoxMenuItem("UIController.pauseAction");

        this.newGameButton = createToolBarButton("UIController.newGameAction");
        this.muteButton = createToolBarToggleButton("UIController.muteAction");
        this.lockButton = createToolBarButton("UIController.lockAction");
        this.stopTrainsButton = createToolBarButton("UIController.stopAction");
        this.pauseButton = createToolBarToggleButton("UIController.pauseAction");
        this.autoLockButton = createToolBarToggleButton("UIController.autoLockAction");
        this.userPreferencesButton = createToolBarButton("UIController.userPreferencesAction");
        this.exitButton = createToolBarButton("UIController.exitAction");

        this.simulator = SimulatorEngineImpl.create(this::stepUp, Function.identity())
                .setEventInterval(Duration.ofMillis(1000 / FPS))
                .setOnEvent(this::handleSimulationEvent)
                .setOnSpeed(this::handleSpeed);
        this.random = new Random();

        simulator.setSpeed(1);
        summaryPanel.setBorder(BorderFactory.createEtchedBorder());
        hallOfFamePanel.setHallOfFame(configuration.getHallOfFame());

        initHorizontalSplit();
        initVerticalSplit();
        initTabbedPanel();
        initFrame();
        createFlows();
        logger.atDebug().log("Created");
    }

    /**
     * Creates the subscriptions to event flowables
     */
    private void createFlows() {
        mapPanel.readMouseClick()
                .doOnNext(stationScrollPanel::scrollTo)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> simulator.isActive())
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON1)
                .doOnNext(this::handleLeftMouseMapEvent)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> simulator.isActive())
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON2)
                .doOnNext(this::handleCentralMouseMapEvent)
                .subscribe();
        stationScrollPanel.readMouseClick()
                .filter(event -> simulator.isActive())
                .filter(event -> event.getMouseEvent().getButton() == MouseEvent.BUTTON3)
                .doOnNext(this::handleRightMouseMapEvent)
                .subscribe();
        SwingObservable.actions(exitButton).mergeWith(SwingObservable.actions(exitMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleExitAction)
                .subscribe();
        SwingObservable.actions(aboutMenu)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleAboutAction)
                .subscribe();
        SwingObservable.actions(newGameButton).mergeWith(SwingObservable.actions(newGameMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleNewGameAction)
                .subscribe();
        SwingObservable.actions(lockButton).mergeWith(SwingObservable.actions(lockMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleLockAction)
                .subscribe();
        SwingObservable.actions(stopTrainsButton).mergeWith(SwingObservable.actions(stopTrainsMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleStopTrainAction)
                .subscribe();
        SwingObservable.actions(pauseButton).mergeWith(SwingObservable.actions(pauseMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handlePauseAction)
                .subscribe();
        SwingObservable.actions(dumpMenu)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleDumpAction)
                .subscribe();
        SwingObservable.actions(userPreferencesButton).mergeWith(SwingObservable.actions(userPreferencesMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleUserPreferencesAction)
                .subscribe();
        SwingObservable.actions(muteButton).mergeWith(SwingObservable.actions(muteMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleMuteAction)
                .subscribe();
        SwingObservable.actions(autoLockButton).mergeWith(SwingObservable.actions(autoLockMenu))
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleAutoLockAction)
                .subscribe();
        events.
                flatMap(this::toClip)
                .doOnNext(soundPlayer::play)
                .subscribe();


        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                simulator.shutdown();
                logger.info("Ended");
            }

            @Override
            public void windowOpened(WindowEvent e) {
                int hWidth = horizontalSplit.getWidth();
//                horizontalSplit.setDividerLocation(hWidth - DEFAULT_TAB_WITDH);
                horizontalSplit.setDividerLocation(0.5);
                verticalSplit.setDividerLocation(0.25);
            }
        });

    }

    /**
     * Returns the menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar result = new JMenuBar();

        /*
         * File menu
         */
        JMenu fileMenu = new JMenu(Messages.getString("UIController.fileMenu.name"));
        fileMenu.setMnemonic(Messages.getString("UIController.fileMenu.mnemonic").charAt(0));
        fileMenu.add(newGameMenu);
        fileMenu.add(new JPopupMenu.Separator());
        fileMenu.add(exitMenu);
        result.add(fileMenu);

        /*
         * Tools menu
         */
        JMenu toolsMenu = new JMenu(Messages.getString("UIController.toolsMenu.name"));
        toolsMenu.setMnemonic(Messages.getString("UIController.toolsMenu.mnemonic").charAt(0));
        toolsMenu.add(lockMenu);
        toolsMenu.add(stopTrainsMenu);
        toolsMenu.add(autoLockMenu);
        toolsMenu.add(pauseMenu);
        toolsMenu.add(new JPopupMenu.Separator());
        toolsMenu.add(muteMenu);
        toolsMenu.add(userPreferencesMenu);
        toolsMenu.add(dumpMenu);
        result.add(toolsMenu);

        /*
         * Help menu
         */
        JMenu helpMenu = new JMenu(Messages.getString("UIController.helpMenu.name"));
        helpMenu.setMnemonic(Messages.getString("UIController.helpMenu.mnemonic").charAt(0));
        helpMenu.add(aboutMenu);
        result.add(helpMenu);

        return result;
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
     * Returns the toolbar
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.add(newGameButton);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(lockButton);
        toolBar.add(stopTrainsButton);
        toolBar.add(autoLockButton);
        toolBar.add(pauseButton);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(muteButton);
        toolBar.add(userPreferencesButton);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(exitButton);
        return toolBar;
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
            JMenuItem menu = new JMenuItem(
                    format(Messages.getString("UIController.stopTrainMenuItem.name"), train.getId()));
            menu.addActionListener(ev -> stopTrain(train.getId()));
            builder.add(menu);
        }
        if (state.equals(Train.STATE_BRAKING) || state.equals(Train.STATE_WAITING_FOR_RUN)) {
            JMenuItem menu = new JMenuItem(
                    format(Messages.getString("UIController.startTrainMenuItem.name"), train.getId()));
            menu.addActionListener(ev -> startTrain(train.getId()));
            builder.add(menu);
        }
        if (state.equals(Train.STATE_WAITING_FOR_RUN)
                || state.equals(Train.STATE_WAITING_FOR_SIGNAL)) {
            JMenuItem menu = new JMenuItem(
                    format(Messages.getString("UIController.revertTrainMenuItem.name"), train.getId()));
            menu.addActionListener(ev -> revertTrain(train.getId()));
            builder.add(menu);
        }
        return builder.build();
    }

    /**
     * Returns the clip of a sound event
     *
     * @param soundEvent the sound event
     */
    private Optional<Clip> getClip(SoundEvent soundEvent) {
        return Optional.ofNullable(clipByEvent.get(soundEvent));
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
     * Handles about action
     *
     * @param actionEvent the action event
     */
    private void handleAboutAction(ActionEvent actionEvent) {
        String text = formatMessage("UIController.about.text",
                Messages.getString("Railways.name"),
                Messages.getString("Railways.version"),
                Messages.getString("Railways.author"));
        JOptionPane.showConfirmDialog(frame, text,
                Messages.getString("UIController.about.title"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles auto lock action
     *
     * @param actionEvent the action event
     */
    private void handleAutoLockAction(ActionEvent actionEvent) {
        logger.atDebug().log("auto lock action");
        autolock = !autolock;
        autoLockButton.setSelected(autolock);
        autoLockMenu.setSelected(autolock);
        autoLockMenu.setIcon(autoLockButton.getSelectedIcon());
        if (simulator.isActive()) {
            simulator.request(status -> status.setAutoLock(autolock));
        }
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
     * Handles the dump status action
     *
     * @param actionEvent the action event
     */
    private void handleDumpAction(ActionEvent actionEvent) {
        simulator.request(UnaryOperator.identity())
                .doOnSuccess(status -> status.dump(DUMP_FILENAME))
                .subscribe();
    }

    /**
     * Handles the exit action
     *
     * @param actionEvent the action event
     */
    private void handleExitAction(ActionEvent actionEvent) {
        frame.dispose();
    }

    /**
     * Handles the game finished
     *
     * @param stationStatus the last status
     */
    private void handleGameFinished(StationStatus stationStatus) {
        logger.atDebug().log("Game finished");
        setPause(false);
        pauseMenu.setEnabled(false);
        pauseButton.setEnabled(false);
        dumpMenu.setEnabled(false);
        ExtendedPerformance perf = stationStatus.getPerformance();
        summaryPanel.setPerformance(perf);
        boolean fame = configuration.isFame(perf);
        summaryPanel.setNameEditable(fame);
        showMessageKey("UIController.summaryDialog.title", summaryPanel);
        if (fame) {
            perf = perf.setPlayer(summaryPanel.getName());
            configuration = configuration.add(perf);
            hallOfFamePanel.setHallOfFame(configuration.getHallOfFame());
            HallOfFamePanel hallOfFamePanel = new HallOfFamePanel();
            hallOfFamePanel.setHallOfFame(configuration.getHallOfFame());
            showMessageKey("UIController.hallOfFameDialog.title", hallOfFamePanel);
        }
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
     * Handles lock action
     *
     * @param actionEvent the action event
     */
    private void handleLockAction(ActionEvent actionEvent) {
        simulator.request(status -> status.lockSignals());
    }

    /**
     * Handles mute action
     *
     * @param actionEvent the action event
     */
    private void handleMuteAction(ActionEvent actionEvent) {
        logger.atDebug().log("mute action");
        boolean mute = !configuration.getUserPreferences().isMute();
        configuration = configuration.setMute(mute);
        muteButton.setSelected(mute);
        muteMenu.setSelected(mute);
        soundPlayer.setMute(mute);
    }

    /**
     * Handles new game action
     *
     * @param actionEvent the action event
     */
    private void handleNewGameAction(ActionEvent actionEvent) {
        gameDialog.showDialog().ifPresent(options -> options.createStatus(random, events)
                .map(status1 -> status1.setAutoLock(autolock))
                .ifPresent(status1 -> {
                    this.layoutRequired = true;
                    if (simulator.isActive()) {
                        simulator.stop().doOnSuccess(seed -> simulator.start(status1))
                                .subscribe();
                    } else {
                        simulator.start(status1);
                    }
                    setPause(false);
                    pauseMenu.setEnabled(true);
                    pauseButton.setEnabled(true);
                    dumpMenu.setEnabled(true);
                }));
    }

    /**
     * Handles the stop game button
     *
     * @param actionEvent the action event
     */
    private void handlePauseAction(ActionEvent actionEvent) {
        setPause(!pause);
        if (pause) {
            simulator.stop()
                    .doOnSuccess(status -> this.status = status)
                    .subscribe();
        } else {
            simulator.start(status);
        }
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
        if (simulator.isActive()) {
            this.status = stationStatus;
            if (stationStatus.isGameFinished()) {
                simulator.stop().doOnSuccess(this::handleGameFinished)
                        .subscribe();
            } else {
                trainPanel.setStatus(stationStatus);
                stationScrollPanel.paintStation(stationStatus);
                mapPanel.paintStation(stationStatus);
                performancePanel.setPerformance(stationStatus.getPerformance());
            }
            if (layoutRequired) {
                layoutRequired = false;
                stationScrollPanel.doLayout();
            }
        }
    }

    /**
     * Handle the speed monitor event
     *
     * @param speed the actual simulation speed
     */
    private void handleSpeed(double speed) {
    }

    /**
     * Handles stop action
     *
     * @param actionEvent the action event
     */
    private void handleStopTrainAction(ActionEvent actionEvent) {
        simulator.request(status -> status.stopTrains());
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
     * Handles user preferences action
     *
     * @param actionEvent the action event
     */
    private void handleUserPreferencesAction(ActionEvent actionEvent) {
        logger.atDebug().log("user preferences action");
        userPrefPanel.setUserPreferences(configuration.getUserPreferences());
        if (showConfirmDialog("UIController.userPrefDialog.title", userPrefPanel)) {
            configuration = configuration.setGain(userPrefPanel.getVolume())
                    .setSimulationSpeed(userPrefPanel.getSpeed());
            UserPreferences userPreferences = configuration.getUserPreferences();
            soundPlayer.setGain((float) userPreferences.getGain());
            simulator.setSpeed(userPreferences.getSimulationSpeed());
        }
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
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setJMenuBar(createMenuBar());
        frame.setSize(screen);
        frame.setLocation(0, 0);

        Container content = frame.getContentPane();
        content.add(createToolBar(), BorderLayout.NORTH);
        content.add(verticalSplit, BorderLayout.CENTER);
        UserPreferences userPreferences = configuration.getUserPreferences();
        boolean mute = userPreferences.isMute();
        muteButton.setSelected(mute);
        muteMenu.setSelected(mute);
        autoLockButton.setSelected(autolock);
        autoLockMenu.setSelected(autolock);
        pauseMenu.setEnabled(false);
        pauseButton.setEnabled(false);
        dumpMenu.setEnabled(false);
        soundPlayer.setMute(mute);
        soundPlayer.setGain((float) userPreferences.getGain());
        simulator.setSpeed(userPreferences.getSimulationSpeed());
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
        addTabKey(tabPanel, "InfoPanel.trainPanel", trainPanel);
        addTabKey(tabPanel, "InfoPanel.performancePanel", performancePanel);
        addTabKey(tabPanel, "InfoPanel.hallOfFamePane", hallOfFamePanel);
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
    }

    /**
     * Set the stop game state
     *
     * @param pause true if game is stopped
     */
    private void setPause(boolean pause) {
        this.pause = pause;
        pauseMenu.setSelected(pause);
        pauseButton.setSelected(pause);
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
     * Returns the clip flowable from the sound event
     *
     * @param soundEvent the sound event
     */
    private Flowable<Clip> toClip(SoundEvent soundEvent) {
        return getClip(soundEvent).map(Flowable::just).orElse(Flowable.empty());
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
