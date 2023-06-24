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

import com.fasterxml.jackson.databind.JsonNode;
import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import org.mmarini.Tuple2;
import org.mmarini.railways2.model.StationStatus;
import org.mmarini.railways2.model.blocks.BlockStationBuilder;
import org.mmarini.railways2.model.blocks.StationDef;
import org.mmarini.railways2.model.geometry.StationMap;
import org.mmarini.swing.GridLayoutHelper;
import org.mmarini.yaml.schema.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static org.mmarini.railways2.swing.SwingUtils.createButton;
import static org.mmarini.railways2.swing.SwingUtils.formatMessage;
import static org.mmarini.yaml.Utils.fromResource;

/**
 * Shows the game options
 */
public class GameDialog extends JDialog {
    public static final int DEFAULT_GAME_LEVEL_INDEX = 1;
    public static final int DEFAULT_GAME_DURATION_INDEX = 1;
    public static final String CUSTOM_GAME_LEVEL = "GameDialog.gameLevel.custom";
    public static final String HARD_GAME_LEVEL = "GameDialog.gameLevel.hard";
    public static final String MEDIUM_GAME_LEVEL = "GameDialog.gameLevel.medium";
    public static final String EASY_GAME_LEVEL = "GameDialog.gameLevel.easy";
    public static final String CUSTOM_GAME_DURATION = "GameDialog.gameDuration.custom";
    public static final String LONG_GAME_DURATION = "GameDialog.gameDuration.long";
    public static final String MEDIUM_GAME_DURATION = "GameDialog.gameDuration.medium";
    public static final String SHORT_GAME_DURATION = "GameDialog.gameDuration.short";

    private static final List<String> STATION_RESOURCES = List.of(
            "/stations/downville.station.yml"
    );
    private static final List<String> GAME_DURATION_KEYS = List.of(
            SHORT_GAME_DURATION,
            MEDIUM_GAME_DURATION,
            LONG_GAME_DURATION,
            CUSTOM_GAME_DURATION
    );
    private static final List<String> GAME_LEVEL_KEYS = List.of(
            EASY_GAME_LEVEL,
            MEDIUM_GAME_LEVEL,
            HARD_GAME_LEVEL,
            CUSTOM_GAME_LEVEL
    );

    private static final Logger logger = LoggerFactory.getLogger(GameDialog.class);

    /**
     * Returns the list of valid station identifier and resource
     */
    private static List<Tuple2<String, String>> getStationsDef() {
        return STATION_RESOURCES.stream()
                .flatMap(resource ->
                        loadStation(resource, 10, 0.1, null)
                                .map(StationStatus::getStationMap)
                                .map(StationMap::getId)
                                .map(id -> Tuple2.of(id, resource))
                                .stream()
                )
                .collect(Collectors.toList());
    }

    /**
     * Returns the station status from resource
     *
     * @param resource     the resource name
     * @param gameDuration the game duration (s)
     * @param frequency    the train frequency (#/s)
     * @param random       the random number generator
     */
    static Optional<StationStatus> loadStation(String resource, double gameDuration, double frequency, Random random) {
        try {
            JsonNode json = fromResource(resource);
            StationDef def = StationDef.create(json, Locator.root());
            StationStatus result = new BlockStationBuilder(def, gameDuration, frequency, random)
                    .build();
            return Optional.of(result);
        } catch (Exception e) {
            logger.atError().setCause(e).log("Read resource {}", resource);
            return Optional.empty();
        }
    }

    private final List<Tuple2<String, String>> stations;
    private final DefaultListModel<String> stationModel;
    private final JList<String> stationList;
    private final DefaultListModel<String> gameDurationModel;
    private final JList<String> gameDurationList;
    private final JFormattedTextField gameDurationField;
    private final DefaultListModel<String> gameLevelModel;
    private final JList<String> gameLevelList;
    private final JFormattedTextField frequencyField;
    private final JTextField info;
    private final JButton okButton;
    private final JButton cancelButton;
    private boolean valid;

    /**
     *
     */
    public GameDialog() {
        stationModel = new DefaultListModel<>();
        stationList = new JList<>(stationModel);
        gameDurationModel = new DefaultListModel<>();
        gameDurationList = new JList<>(gameDurationModel);
        gameDurationField = new JFormattedTextField();
        gameLevelModel = new DefaultListModel<>();
        gameLevelList = new JList<>(gameLevelModel);
        frequencyField = new JFormattedTextField();
        this.stations = getStationsDef();
        info = new JTextField();
        this.okButton = createButton("GameDialog.okButton");
        this.cancelButton = createButton("GameDialog.cancelButton");
        this.gameDurationField.setValue(0d);
        frequencyField.setValue(0d);
        init();
        createEventFlows();
    }

    /**
     * Appends the message to info text separated by ", "
     *
     * @param message the message
     */
    private void appendMessage(String message) {
        String text = info.getText();
        text += text.length() > 0
                ? ", " + message :
                message;
        info.setText(text);
    }

    /**
     * Appends message to info label
     *
     * @param key  the message key
     * @param args the arguments
     */
    private void appendMessageKey(String key, Object... args) {
        appendMessage(formatMessage(key, args));
    }

    /**
     * Centers the dialog
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
     * Clears the info label
     */
    private void clearInfo() {
        info.setText("");
    }

    /**
     * Returns the buttons panel
     */
    private JPanel createButtonsPanel() {
        return new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .add("+insets,2 at,0,0 nofill noweight",
                        okButton,
                        "+at,1,0",
                        cancelButton)
                .getContainer();
    }

    /**
     * Returns the content panel
     */
    protected JPanel createContent() {
        JPanel panel = new GridLayoutHelper<>(Messages.RESOURCE_BUNDLE, new JPanel())
                .add("+insets,2 hspan,3 center",
                        "GameDialog.stationList.title",
                        "+at,0,1 nospan fill weight,1,1 center",
                        new JScrollPane(stationList),
                        "+at,0,2 hspan,3 hfill center",
                        new JSeparator(),
                        "+at,0,3 hspan,3 nofill center",
                        "GameDialog.gameDuration.title",
                        "+at,0,4 nospan fill weight,1,1 center",
                        new JScrollPane(gameDurationList),
                        "+at,1,4 nospan nofill noweight se",
                        "GameDialog.gameDurationField.label.text",
                        "+at,2,4 nospan hfill noweight sw",
                        gameDurationField,
                        "+at,0,5 hspan,3 hfill noweight center",
                        new JSeparator(),
                        "+at,0,6 hspan,3 nofill noweight center",
                        "GameDialog.gameLevel.title",
                        "+at,0,7 nospan fill weight,1,1 center",
                        new JScrollPane(gameLevelList),
                        "+at,1,7 nospan nofill noweight se",
                        "GameDialog.trainFrequencyField.label.text",
                        "+at,2,7 nospan hfill weight,1,0 se",
                        frequencyField,
                        "+at,0,8 hspan,3 hfill noweight center",
                        new JSeparator(),
                        "+at,0,9 hspan,3 hfill weight,1,0 center",
                        info)
                .getContainer();
        panel.setBorder(BorderFactory.createEtchedBorder());
        return panel;
    }

    /**
     * Creates the event flows
     */
    private void createEventFlows() {
        SwingObservable.listSelection(gameDurationList)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleGameDurationChanged)
                .subscribe();
        SwingObservable.listSelection(gameLevelList)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(this::handleGameLevelChanged)
                .subscribe();
        SwingObservable.actions(okButton)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(e -> {
                    if (validateInput()) {
                        dispose();
                    }
                })
                .subscribe();
        SwingObservable.actions(cancelButton)
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(e -> {
                    valid = false;
                    dispose();
                })
                .subscribe();
        SwingObservable.propertyChange(frequencyField, "value")
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(e -> validateInput())
                .subscribe();
        SwingObservable.propertyChange(gameDurationField, "value")
                .toFlowable(BackpressureStrategy.LATEST)
                .doOnNext(e -> validateInput())
                .subscribe();
    }

    /**
     * Returns the train frequency
     */
    private double getGameDuration() {
        return isCustomGameDuration()
                ? ((Number) gameDurationField.getValue()).doubleValue()
                : getGameDurationKey()
                .flatMap(key -> Messages.getStringOpt(key + ".value"))
                .map(Double::parseDouble)
                .orElseThrow();

    }

    /**
     * Returns the selected duration key
     */
    private Optional<String> getGameDurationKey() {
        int idx = gameDurationList.getSelectedIndex();
        return idx >= 0 ? Optional.of(GAME_DURATION_KEYS.get(idx)) : Optional.empty();
    }

    /**
     * Returns the selected level key
     */
    private Optional<String> getGameLevelKey() {
        int idx = gameLevelList.getSelectedIndex();
        return idx >= 0 ? Optional.of(GAME_LEVEL_KEYS.get(idx)) : Optional.empty();
    }

    /**
     * Returns the game options
     */
    public GameOptions getGameOptions() {
        String stationResource = getStationResource();
        double gameDuration = getGameDuration();
        double trainFrequency = getTrainFrequency();
        return new GameOptions(stationResource, gameDuration, trainFrequency);
    }

    /**
     * Returns the selected station key
     */
    private String getStationResource() {
        int idx = max(0, stationList.getSelectedIndex());
        return stations.get(idx)._2;
    }

    /**
     * Returns the train frequency
     */
    private double getTrainFrequency() {
        return isCustomGameLevel()
                ? ((Number) frequencyField.getValue()).doubleValue()
                : getGameLevelKey()
                .flatMap(key -> Messages.getStringOpt(key + ".value"))
                .map(Double::parseDouble)
                .orElseThrow();

    }

    /**
     * Handles the game duration list selection
     *
     * @param e the event
     */
    private void handleGameDurationChanged(ListSelectionEvent e) {
        logger.debug("handleGameLengthChanged");
        if (isCustomGameDuration()) {
            gameDurationField.setEditable(true);
            gameDurationField.setEnabled(true);
        } else {
            gameDurationField.setEditable(false);
            gameDurationField.setEnabled(false);
        }
        validateInput();
    }

    /**
     * Handles the game level list selection
     *
     * @param e the event
     */
    private void handleGameLevelChanged(ListSelectionEvent e) {
        logger.debug("handleGameLengthChanged");
        if (isCustomGameLevel()) {
            frequencyField.setEditable(true);
            frequencyField.setEnabled(true);
        } else {
            frequencyField.setEditable(false);
            frequencyField.setEnabled(false);
        }
        validateInput();
    }

    /**
     * Initializes the panel
     */
    protected void init() {
        // StationList init
        stationList.setModel(stationModel);
        stations.stream()
                .map(Tuple2::getV1)
                .forEach(stationModel::addElement);
        stationList.setSelectedIndex(0);

        // game duration init
        gameDurationList.setModel(gameDurationModel);
        GAME_DURATION_KEYS.stream()
                .map(Messages::getString)
                .forEach(gameDurationModel::addElement);
        gameDurationList.setSelectedIndex(DEFAULT_GAME_DURATION_INDEX);

        // game level init
        gameLevelList.setModel(gameLevelModel);
        GAME_LEVEL_KEYS.stream()
                .map(Messages::getString)
                .forEach(gameLevelModel::addElement);
        gameLevelList.setSelectedIndex(DEFAULT_GAME_LEVEL_INDEX);

        gameDurationField.setValue(0d);
        gameDurationField.setColumns(5);
        gameDurationField.setEditable(false);
        gameDurationField.setEnabled(false);

        frequencyField.setValue(0d);
        frequencyField.setColumns(8);
        frequencyField.setEditable(false);
        frequencyField.setEnabled(false);

        info.setForeground(Color.RED);
        info.setEditable(false);
        info.setHorizontalAlignment(JTextField.CENTER);

        setTitle(Messages.getString("GameDialog.title"));
        getContentPane().add(createContent(), BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
        centerDialog();
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Returns true if the custom duration is selected
     */
    private boolean isCustomGameDuration() {
        return getGameDurationKey().filter(CUSTOM_GAME_DURATION::equals)
                .isPresent();
    }

    /**
     * Returns true if the custom level is selected
     */
    private boolean isCustomGameLevel() {
        return getGameLevelKey()
                .filter(CUSTOM_GAME_LEVEL::equals)
                .isPresent();
    }

    /**
     * Returns the game option by opening the dialog
     */
    public Optional<GameOptions> showDialog() {
        valid = false;
        setVisible(true);
        return valid ? Optional.of(getGameOptions()) : Optional.empty();
    }

    /**
     * Returns the number of errors validating game duration
     */
    private int validateGameDuration() {
        int errors = 0;
        if (isCustomGameDuration()) {
            try {
                gameDurationField.commitEdit();
                double duration = ((Number) gameDurationField.getValue()).doubleValue();
                if (duration <= 0) {
                    errors++;
                    appendMessageKey("GameDialog.gameDurationField.invalidValueError",
                            duration);
                }
            } catch (ParseException e) {
                errors++;
                logger.atError().setCause(e).log("Validate game duration");
                appendMessageKey("GameDialog.gameDurationField.parseError",
                        e.getMessage());
            }
        }
        return errors;
    }

    /**
     * Returns true if the inputs are valid
     */
    public boolean validateInput() {
        clearInfo();
        valid = (validateTrainFrequency() +
                validateGameDuration()) == 0;
        okButton.setEnabled(valid);
        return valid;
    }

    /**
     * Returns the number of errors validating train frequency
     */
    private int validateTrainFrequency() {
        int errors = 0;
        if (isCustomGameLevel()) {
            try {
                frequencyField.commitEdit();
                double frequency = ((Number) frequencyField.getValue()).doubleValue();
                if (frequency <= 0) {
                    errors++;
                    appendMessageKey("GameDialog.frequencyField.invalidValueError",
                            frequency);
                }
            } catch (ParseException e) {
                errors++;
                logger.atError().setCause(e).log("Validate train frequency");
                appendMessageKey("GameDialog.frequencyField.parseError",
                        e.getMessage());
            }
        }
        return errors;
    }
}